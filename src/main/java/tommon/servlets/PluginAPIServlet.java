package tommon.servlets;

import tommon.managers.DBManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Created by majlen on 20.7.16.
 */
public class PluginAPIServlet extends PluginServlet {
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        DBManager db = (DBManager) getServletContext().getAttribute("db");
        String first = request.getParameter("first");
        PrintWriter out = response.getWriter();

        if (first != null) {
            try {
                response.getWriter().print(db.getMinimumDate(config.getTable()));
            } catch (SQLException e) {
                response.getWriter().print(System.currentTimeMillis()/1000);
            }
        } else {
            int from = Integer.parseInt(request.getParameter("from"));
            int to = Integer.parseInt(request.getParameter("to"));

            response.setContentType("text/csv");

            try {
                db.printTableCSV(config.getTable(), config.getFields(), from, to, out);
            } catch (Exception e) {
                System.out.println("ERROR: Could not acces data in DB");
                System.out.println(e.getMessage());
                return;
            }
        }

        out.flush();
    }
}
