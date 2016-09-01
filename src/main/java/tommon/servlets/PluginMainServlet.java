package tommon.servlets;

import tommon.managers.DBManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Created by majlen on 20.7.16.
 */
public class PluginMainServlet extends PluginServlet {
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        request.setAttribute("reqName", config.getName());
        request.setAttribute("reqMsg", DBManager.DBgetMessage(config.getTable()));
        request.setAttribute("reqStates", config.getFields());

        getServletContext().getNamedDispatcher("graphTemplate").forward(request, response);    }
}
