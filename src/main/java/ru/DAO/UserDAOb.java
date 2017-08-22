package ru.DAO;

import java.sql.*;

public class UserDAOb {

    private void testDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/contactdb";
            String login = "postgres";
            String password = "";
            Connection con = DriverManager.getConnection(url, login, password);
            try {
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM JC_CONTACT");
                while (rs.next()) {
                    String str = rs.getString("contact_id") + ":" + rs.getString(2) +" " + rs.getString(3) ;
                    System.out.println("Contact:" + str);
                }
                rs.close();
                stmt.close();
            } finally {
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection connection (){
        Connection con;
        try {
            Class.forName("org.postgresql.Driver");
            String url = "jdbc:postgresql://localhost:5432/Model";
            String login = "postgres";
            String password = "";
            con = DriverManager.getConnection(url, login, password);
        }catch (Exception e){
            return null;
        }
        return con;
    }

    public String exQuery (Connection connection, String sqlString){

        if (sqlString == null || sqlString.trim().isEmpty()) {
            System.out.println("Null query");
            return "Null query";
        }

        if (connection != null) {
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sqlString);

            } catch (SQLException e) {
                return "Query execution";
            }
        }
        return "Query execution";
    }

    public String createTable(Connection connection){
        if (connection != null) {
            Statement stmt = null;
            try {
                stmt = connection.createStatement();
                String sql = new String("CREATE TABLE public.\"PBE2.5\" (" +
                        ") WITH (\n" +
                        "  OIDS=FALSE\n" +
                        ");\n" +
                        "ALTER TABLE public.\"PBE2.5\"\n" +
                        "  OWNER TO postgres;"
//                "(ID char NOT NULL," +
//                "Name text)"
                );
                stmt.execute(sql);
                connection.close();

            } catch (SQLException e) {
                return "Таблица не создана";
            }
        }

        return "Таблица создана";
    }



}