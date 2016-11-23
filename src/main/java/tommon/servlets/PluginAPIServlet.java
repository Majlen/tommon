package tommon.servlets;

import tommon.managers.DBManager;

import javax.json.Json;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.Instant;

/**
 * API servlet of plugins. Sends requested columns from a requested time period in form of CSV table.
 * @author Milan Ševčík
 */
public class PluginAPIServlet extends PluginServlet {
	private static final long serialVersionUID = 1L;
	private Instant from;
	private Instant to;
	private DBManager db;

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		db = (DBManager) getServletContext().getAttribute("db");
		String first = request.getParameter("first");

		if (first != null) {
			try {
				response.getWriter().print(db.getMinimumDate(config.getTable()));
			} catch (SQLException e) {
				response.getWriter().print(Instant.now().toEpochMilli());
			}
		} else {
			PrintWriter out = response.getWriter();
			from = Instant.ofEpochMilli(Long.parseLong(request.getParameter("from")));
			to = Instant.ofEpochMilli(Long.parseLong(request.getParameter("to")));
			String type = request.getParameter("mimetype");

			switch (type) {
				case "csv":
					response.setContentType("text/csv");
					printCSV(out);
					break;
				case "json":
				default:
					response.setContentType("application/json");
					printJSON(out);
					break;
			}
			out.flush();
		}
	}

	private void printCSV(PrintWriter writer) {
		try {
			db.printTableCSV(config.getTable(), config.getFields(), from, to, writer);
		} catch (SQLException e) {
			System.out.println("ERROR: Could not access data in DB");
			System.out.println(e.getMessage());
		}
	}

	private void printJSON(PrintWriter writer) {
		try {
			JsonWriter jsonWriter = Json.createWriter(writer);
			jsonWriter.writeObject(db.getJsonFromColumns(config.getTable(), config.getFields(), from, to));
			jsonWriter.close();
		} catch (SQLException e) {
			System.out.println("ERROR: Could not access data in DB");
			System.out.println(e.getMessage());
			writer.print("{}");
		}
	}

}
