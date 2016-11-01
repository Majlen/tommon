package tommon.servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Main servlet of plugins. Sends the JSP template with URL of API and value names.
 */
public class PluginMainServlet extends PluginServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		request.setAttribute("reqName", config.getName());
		request.setAttribute("reqStates", config.getFields());

		getServletContext().getNamedDispatcher("graphTemplate").forward(request, response);
	}
}
