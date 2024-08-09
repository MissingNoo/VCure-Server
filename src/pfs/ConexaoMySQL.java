package pfs;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class ConexaoMySQL {
    public static String status = Server.getTimeStamp() + "[MySQL]  Not connected to database!";
    public static Connection connection;

    public ConexaoMySQL() {

    }

    public static java.sql.Connection getConexaoMySQL() {
        connection = null;
        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            Class.forName(driverName);

            String serverName = "localhost";
            String mydatabase = "VCure";
            String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
            String username = "root";
            String password = "";
            connection = DriverManager.getConnection(url, username, password);
            if (connection != null) {
                status = (Server.getTimeStamp() + "[MySQL] Connected to database!");
            } else {
                status = (Server.getTimeStamp() + "[MySQL] Can't connect to database");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            System.out.println(Server.getTimeStamp() + "[MySQL] Driver not Found!");
            return null;
        } catch (SQLException e) {
            System.out.println(Server.getTimeStamp() + "[MySQL] Can't connect to database.");
            return null;
        }
    }

    //Método que retorna o status da sua conexão//
    public static String statusConection() {
        return status;
    }

    //Método que fecha sua conexão//
    public static boolean FecharConexao() {
        try {
            ConexaoMySQL.getConexaoMySQL().close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    //Método que reinicia sua conexão//
    public static java.sql.Connection ReiniciarConexao() {
        FecharConexao();
        return ConexaoMySQL.getConexaoMySQL();
    }

    static Statement stmt = null;
    static ResultSet rs = null;

    public static String register(String usr) {
        String query = "INSERT INTO `players` (`id`, `nickname`) VALUES (NULL, '" + usr + "')";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
        System.out.println("Selecting");
        query = "SELECT * FROM players WHERE nickname = '" + usr + "'";
        String id = "-1";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                id = rs.getString("id").toString();
                System.out.println("User " + usr + " registered! id: " + id);
            }
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
        return id;
    }

    public static String login(int id) {
        String nickname = "";
        String query = "SELECT * FROM players WHERE id = '" + String.valueOf(id) + "'";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                nickname = rs.getString("nickname").toString();
            }
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
        return nickname;
    }

    public static String loadinfo(String usr, String date, String moment) {
        String result = "\"result\" : \"not found\",";
        String hour = "0";
        String minute = "0";
        String query = "SELECT * FROM `hours` WHERE `account` = " + usr + " AND `date` LIKE '" + date + "' AND `moment` = " + moment;
        //System.out.println("Q:" + query);
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                hour = rs.getString("hour").toString();
                minute = rs.getString("minute").toString();
                result = "\"result\" : \"found\", \"hour\" : \"" + hour + "\", \"minute\" : \"" + minute + "\",";
                //System.out.println(result);
            }
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
        return result;
    }

    public static void savehours(String usr, String hour, String minute, String date, String moment, User user) {
        String query = "INSERT INTO `hours` (`account`, `hour`, `minute`, `date`, `moment`) VALUES ('" + usr + "', '" + hour + "', '" + minute + "', '" + date +"', '" + moment+ "')";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(query);
            System.out.println("User " + usr + " registered hours!");
            Server.sendData(user, "{\"type\" : \"5\"}");
        }
        catch (SQLException ex){
            System.out.println(ex);
        }
    }
}