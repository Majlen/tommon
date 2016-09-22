package tommon.managers;

import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;

public final class DBManager implements StorageManager {
    private Connection sqlcon = null;
    private Driver driver;

    public DBManager(String dburl, String driver) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        sqlcon = DriverManager.getConnection(dburl);
        this.driver = DriverManager.getDriver(dburl);
    }

    public void close() throws SQLException {
        sqlcon.close();
        DriverManager.deregisterDriver(driver);
    }

    protected void finalize() throws Throwable {
        try {
            System.out.println("WARN: Application did not close DBManager");
            close();
        } finally {
            super.finalize();
        }
    }

    public void addTable(String table, String[] columns) throws SQLException {
        String cols = deArray(columns);
        Statement s = sqlcon.createStatement();
        s.executeUpdate("CREATE TABLE IF NOT EXISTS "+ table+" (date INTEGER"+cols+")");
    }

    public void addRow(String table, String[] columns, String[] values) throws SQLException {
        String colsconv = deArray(columns);
        String valsconv = deArray(values);

        String cols = "(date"+colsconv+")";
        String vals = "("+System.currentTimeMillis()/1000+valsconv+")";

        Statement s = sqlcon.createStatement();
        s.executeUpdate("INSERT INTO "+table+" "+cols+" VALUES "+vals+";");

    }

    public void printTableCSV(String table, String[] columns, int from, int to, PrintWriter print) throws SQLException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String cols = deArray(columns);

        Statement s = sqlcon.createStatement();
        ResultSet rs = s.executeQuery("SELECT date" + cols + " FROM " + table + " WHERE date BETWEEN " + from + " AND " + to + ";");

        print.println("date" + cols);

        while (rs.next()) {
            print.print(df.format(new java.util.Date(rs.getInt("date") * 1000L)) + ",");

            for (int i = 0; i < columns.length; i++) {
                print.print(rs.getString(columns[i]));
                if (i == columns.length - 1) {
                    print.print("\n");
                } else {
                    print.print(",");
                }
            }
        }
    }

    public int getMinimumDate(String table) throws SQLException {
        Statement s = sqlcon.createStatement();
        ResultSet rs = s.executeQuery("SELECT min(date) AS date FROM " + table + ";");
        return rs.getInt("date");
    }

    private static String deArray(String[] arr) {
        String out = "";
        for (String item : arr) {
            out += ", " + item;
        }
        return out;
    }
}
