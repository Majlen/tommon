package tommon.servlets;

import tommon.plugins.PluginConfig;

import javax.servlet.http.HttpServlet;

public abstract class PluginServlet extends HttpServlet {
	PluginConfig config;

	public void setConfig(PluginConfig config) {
		this.config = config;
	}
}
