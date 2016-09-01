package tommon.servlets;

import tommon.plugins.PluginConfig;

import javax.servlet.http.HttpServlet;

/**
 * Created by majlen on 20.7.16.
 */
public abstract class PluginServlet extends HttpServlet {
    PluginConfig config;

    public void setConfig(PluginConfig config) {
        this.config = config;
    }
}
