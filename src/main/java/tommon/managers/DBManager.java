package tommon.managers;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.PrintWriter;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementation of StorageManager, which stores all the values in database.
 * @author Milan Ševčík
 */
public final class DBManager implements StorageManager {
	final private int schema_version = 2;
	private Connection sqlcon = null;
	private Driver driver;

	/**
	 * Constructor, connecting to the database.
	 *
	 * @param dburl JDBC URL of the database
	 * @param driver name of the JDBC driver
	 * @throws ClassNotFoundException thrown when the requested JDBC driver could not be found
	 * @throws SQLException thrown when the driver cannot connect to database
	 */
	public DBManager(String dburl, String driver) throws ClassNotFoundException, SQLException {
		Class.forName(driver);
		sqlcon = DriverManager.getConnection(dburl);
		this.driver = DriverManager.getDriver(dburl);
		update();
	}

	/**
	 * Closes database connection.
	 * @throws SQLException thrown when there are issues closing the connection
	 */
	public void close() throws SQLException {
		sqlcon.close();
		DriverManager.deregisterDriver(driver);
	}

	private void update() throws SQLException {
		ResultSet rs = sqlcon.createStatement().executeQuery("PRAGMA user_version");
		//TODO: in the future should check for the actual version
		if (rs.getInt("user_version") < schema_version) {
			rs.close();

			rs = sqlcon.createStatement().executeQuery("SELECT name FROM sqlite_master WHERE type='table'");
			List<String> tables = new LinkedList<>();
			while (rs.next()) {
				tables.add(rs.getString("name"));
			}
			rs.close();

			sqlcon.setAutoCommit(false);
			PreparedStatement s = sqlcon.prepareStatement("UPDATE ? SET date = date * 1000");
			for (String table: tables) {
				s.setString(1, table);
				s.executeUpdate();
			}
			PreparedStatement version = sqlcon.prepareStatement("PRAGMA user_version = ?");
			version.setInt(1, schema_version);
			version.executeUpdate();

			sqlcon.commit();
			sqlcon.setAutoCommit(true);
			s.close();
			version.close();
		} else {
			rs.close();
		}
	}

	protected void finalize() throws Throwable {
		try {
			System.out.println("WARN: Application did not close DBManager");
			close();
		} finally {
			super.finalize();
		}
	}

	/**
	 * Adds table to the database
	 * @param table name of the table
	 * @param columns names of columns
	 * @throws SQLException thrown when there is a syntax error in SQL
	 */
	public void addTable(String table, String[] columns) throws SQLException {
		String cols = deArray(columns);
		PreparedStatement s = sqlcon.prepareStatement("CREATE TABLE IF NOT EXISTS ? (date INTEGER" + cols + ")");
		s.setString(1, table);
		s.executeUpdate();
		s.close();
	}

	/**
	 * Adds row of values to the database
	 * @param table name of the table
	 * @param columns name of columns
	 * @param values values to be stored
	 * @throws SQLException thrown when there is a syntax error in SQL
	 */
	public void addRow(String table, String[] columns, String[] values) throws SQLException {
		String colsconv = deArray(columns);
		String valsconv = deArray(values);

		String cols = "(date" + colsconv + ")";
		String vals = "(" + Instant.now().toEpochMilli() + valsconv + ")";

		PreparedStatement s = sqlcon.prepareStatement("INSERT INTO ? " + cols + " VALUES " + vals + ";");
		s.setString(1, table);
		s.executeUpdate();
		s.close();
	}

	/**
	 * Prints the table ins CSV format by provided PrintWriter
	 * @param table name of the table
	 * @param columns name of columns
	 * @param from date to begin with as seconds timestamp
	 * @param to date to end with as seconds timestamp
	 * @param print PrintWriter to print the table with
	 * @throws SQLException thrown when there is a syntax error in SQL
	 */
	public void printTableCSV(String table, String[] columns, Instant from, Instant to, PrintWriter print) throws SQLException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).withZone(ZoneId.systemDefault());
		String cols = deArray(columns);

		PreparedStatement s = sqlcon.prepareStatement("SELECT date" + cols + " FROM ? WHERE date BETWEEN ? AND ?;");
		s.setString(1, table);
		s.setTimestamp(2, Timestamp.from(from));
		s.setTimestamp(3, Timestamp.from(to));
		ResultSet rs = s.executeQuery();

		print.println("date" + cols);

		while (rs.next()) {
			LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();
			print.print(date.format(dtf) + ",");

			for (int i = 0; i < columns.length; i++) {
				print.print(rs.getString(columns[i]));
				if (i == columns.length - 1) {
					print.print("\n");
				} else {
					print.print(",");
				}
			}
		}
		rs.close();
		rs.close();
	}

	/**
	 * Gets the oldest date in table
	 * @param table name of the table
	 * @return the oldest date in table
	 * @throws SQLException thrown when there is a syntax error in SQL
	 */
	public int getMinimumDate(String table) throws SQLException {
		PreparedStatement s = sqlcon.prepareStatement("SELECT date FROM ? ORDER BY ROWID ASC LIMIT 1;");
		s.setString(1, table);
		ResultSet rs = s.executeQuery();
		int out = rs.getInt("date");

		rs.close();
		s.close();
		return out;
	}

	private static String deArray(String[] arr) {
		String out = "";
		for (String item : arr) {
			out += ", " + item;
		}
		return out;
	}

	public Map<Instant, String> getColumn(String table, String column, Instant from, Instant to) throws SQLException {
		PreparedStatement s = sqlcon.prepareStatement("SELECT date, ? FROM ? WHERE date BETWEEN ? AND ?;");
		s.setString(1, column);
		s.setString(2, table);
		s.setTimestamp(3, Timestamp.from(from));
		s.setTimestamp(4, Timestamp.from(to));
		ResultSet rs = s.executeQuery();

		Map<Instant, String> out = new HashMap<>();
		while (rs.next()) {
			out.put(rs.getTimestamp("date").toInstant(), rs.getString(column));
		}
		rs.close();
		s.close();
		return out;
	}

	public Map<Instant, Map<String, String>> getColumns(String table, String[] columns, Instant from, Instant to) throws SQLException {
		String cols = deArray(columns);

		PreparedStatement s = sqlcon.prepareStatement("SELECT date" + cols + " FROM ? WHERE date BETWEEN ? AND ?;");
		s.setString(1, table);
		s.setTimestamp(2, Timestamp.from(from));
		s.setTimestamp(3, Timestamp.from(to));
		ResultSet rs = s.executeQuery();

		Map<Instant, Map<String, String>> out = new HashMap<>();
		while (rs.next()) {
			Map<String, String> outCols = new HashMap<>();
			for (String column: columns) {
				outCols.put(column, rs.getString(column));
			}
			out.put(rs.getTimestamp("date").toInstant(), outCols);
		}
		rs.close();
		s.close();
		return out;
	}

	public JsonObject getJsonFromColumns(String table, String[] columns, Instant from, Instant to) throws SQLException {
		Map<Instant, Map<String, String>> map = getColumns(table, columns, from, to);
		JsonObjectBuilder builder = Json.createObjectBuilder();
		JsonArrayBuilder outer = Json.createArrayBuilder();

		for (Instant i: map.keySet()) {
			JsonArrayBuilder array = Json.createArrayBuilder();

			array.add(i.toEpochMilli());
			for (String column: map.get(i).keySet()) {
				array.add(map.get(i).get(column));
			}

			outer.add(array);
		}

		Map.Entry<Instant, Map<String, String>> entry = map.entrySet().iterator().next();
		JsonArrayBuilder headers = Json.createArrayBuilder();

		headers.add("date");
		for (String column: entry.getValue().keySet()) {
			headers.add(column);
		}

		builder.add("values", outer);
		builder.add("headers", headers);
		return builder.build();
	}
}
