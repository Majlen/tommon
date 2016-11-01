package tommon.servlets;

import tommon.plugins.PluginConfig;

import javax.servlet.http.HttpServlet;

/**
 * HttpServlet extension with PluginConfig, so extending servlets can access it.
 * @author Milan Ševčík
 */
public abstract class PluginServlet extends HttpServlet {
	PluginConfig config;

	public void setConfig(PluginConfig config) {
		this.config = config;
	}
}
