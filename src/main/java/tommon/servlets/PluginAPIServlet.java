package tommon.servlets;

import tommon.managers.DBManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by majlen on 20.7.16.
 */
public class PluginAPIServlet extends PluginServlet {
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        int from = Integer.parseInt(request.getParameter("from"));
        int to = Integer.parseInt(request.getParameter("to"));

        response.setContentType("text/csv");
        PrintWriter out = response.getWriter();

        DBManager.printTableCSV(config.getTable(), config.getFields(), from, to, out);

        out.flush();    }
}
