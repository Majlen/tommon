package tommon.listeners;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Connection;
import java.util.Properties;

public final class DBListener implements ServletContextListener {
    private ServletContext context = null;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        this.context = null;
        try {
            tommon.managers.DBManager.DBDisconnect();
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        this.context = event.getServletContext();
        Properties properties = new Properties();
        String dburl = null;

        try {
            properties.load(context.getResourceAsStream("/WEB-INF/tommon.properties"));
            dburl = properties.getProperty("dburl");
            dburl = dburl.replace("${catalina.base}", System.getProperty("catalina.base"));
        } catch (Exception e) {
            System.out.println("ERROR: tommon.properties is non-existent or cannot be accessed");
            System.out.println(e.getMessage());
        }

        try {
            Connection connection = tommon.managers.DBManager.DBConnect(dburl ,"org.sqlite.JDBC");
            context.setAttribute("db", connection);
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
        }

    }

}
