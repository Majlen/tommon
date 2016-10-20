package tommon.listeners;

import tommon.managers.DBManager;
import tommon.managers.PluginsManager;
import tommon.plugins.PluginConfig;
import tommon.servlets.PluginAPIServlet;
import tommon.servlets.PluginMainServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public final class PluginsListener implements ServletContextListener {
    private ServletContext context = null;

    PluginConfig[] plugins = null;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        for (PluginConfig plugin: plugins) {
            plugin.getTimer().cancel();

            // Lets the timers cancel hopefully. Link below hasn't provided better solution.
            // https://mail-archives.apache.org/mod_mbox/tomcat-users/201107.mbox/%3C4E2CB8BC.6050202@tmbsw.com%3E
            Thread.yield();
        }

        plugins = null;
        context = null;
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        this.context = event.getServletContext();
        DBManager db = (DBManager)context.getAttribute("db");

        try {
            Properties properties = new Properties();
            properties.load(context.getResourceAsStream("/WEB-INF/tommon.properties"));
            String pluginsDir = properties.getProperty("plugins.directory");
            pluginsDir = pluginsDir.replace("${catalina.base}", System.getProperty("catalina.base"));
	        System.out.println(pluginsDir);

            plugins = PluginsManager.getPlugins(context.getClassLoader(), pluginsDir, db);
            for (PluginConfig plugin: plugins) {
                registerServlet(plugin);
            }
        } catch (IOException e) {
            System.out.println("ERROR: tommon.properties is non-existent or cannot be accessed");
            System.out.println(e.getMessage());
        }
        context.setAttribute("plugins", plugins);

        int oldest = Integer.MAX_VALUE;
        for (PluginConfig plugin: plugins) {
            int temp = Integer.MAX_VALUE;
            try {
                temp = db.getMinimumDate(plugin.getTable());
            } catch (SQLException e) {
                System.out.println();
            }
            if (temp < oldest) {
                oldest = temp;
            }
        }

        context.setAttribute("oldest", oldest);
    }

    private void registerServlet(PluginConfig config) {
        PluginMainServlet main = new PluginMainServlet();
        main.setConfig(config);

        PluginAPIServlet api = new PluginAPIServlet();
        api.setConfig(config);

        ServletRegistration.Dynamic dyn = context.addServlet("API-"+config.getName(), api);
        dyn.setLoadOnStartup(2);
        dyn.addMapping("/api/"+config.getName());

        dyn = context.addServlet(config.getName(), main);
        dyn.setLoadOnStartup(2);
        dyn.addMapping("/"+config.getName());
    }
}
