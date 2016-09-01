package tommon.listeners;

import tommon.annotations.JMXObject;
import tommon.annotations.KeyValueObject;
import tommon.managers.JMXManager;
import tommon.managers.KeyValueManager;
import tommon.plugins.PluginConfig;
import tommon.plugins.timers.Timers;
import tommon.servlets.PluginAPIServlet;
import tommon.servlets.PluginMainServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Arrays;

public final class PluginsListener implements ServletContextListener {
    private ServletContext context = null;

    ArrayList<Timers> timers = new ArrayList<Timers>();
    ArrayList<String> names = new ArrayList<String>();

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        for (Timers timer: timers) {
            timer.cancel();

            // Lets the timers cancel hopefully. Link below hasn't provided better solution.
            // https://mail-archives.apache.org/mod_mbox/tomcat-users/201107.mbox/%3C4E2CB8BC.6050202@tmbsw.com%3E
            Thread.yield();
        }

        timers.clear();

        context = null;
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        this.context = event.getServletContext();

        try {
            Properties properties = new Properties();
            properties.load(context.getResourceAsStream("/WEB-INF/tommon.properties"));
            String pluginsDir = properties.getProperty("plugins.directory");
            pluginsDir = pluginsDir.replace("${catalina.base}", System.getProperty("catalina.base"));
	        System.out.println(pluginsDir);

            File dir = new File(pluginsDir);
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    ClassLoader cl = URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, context.getClassLoader());

                    Properties pluginProperties = new Properties();
                    pluginProperties.load(cl.getResourceAsStream("META-INF/plugins.properties"));
                    String[] plugins = pluginProperties.getProperty("plugins").split(",");
		            System.out.println(Arrays.toString(plugins));

                    for (String plugin: plugins) {
                        try {
                            Class clazz = Class.forName(plugin, true, cl);

                            Annotation[] anns = clazz.getAnnotations();
			                System.out.println(Arrays.toString(anns));
                            for (Annotation ann : anns) {
                                PluginConfig config = null;
                                if (ann.annotationType().equals(JMXObject.class)) {
                                    config = JMXManager.loadPlugin(plugin, clazz, context);
                                    timers.add(config.getTimer());
                                } else if (ann.annotationType().equals(KeyValueObject.class)) {
                                    config = KeyValueManager.loadPlugin(plugin, clazz, context);
                                    timers.add(config.getTimer());
                                } else {
                                    System.out.println("Unknown plugin annotation "+ann.toString());
                                }

                                if (config != null) {
                                    registerServlet(config);
                                }
                            }
                        } catch (Exception e) {
                            java.lang.System.out.println("Unable to load plugin "+plugin);
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: tommon.properties is non-existent or cannot be accessed");
            System.out.println(e.getMessage());
        }
        context.setAttribute("pluginNames", names);
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

        names.add(config.getName());
    }
}
