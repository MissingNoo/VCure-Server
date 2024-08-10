package pfs;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
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

    public static void submit_score(User user, JSONObject scoredata) {
        try {
            String query = "INSERT INTO leaderboards (playerid, score, build, time)"
                    + "VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setInt (1, user.playerid);
            preparedStmt.setString (2, scoredata.getBigDecimal("score").toString());
            preparedStmt.setString (3, scoredata.getString("build"));
            preparedStmt.setString (4, scoredata.getString("date"));
            preparedStmt.execute();
            System.out.println("New score received from " + user.playername);
        }
        catch (SQLException err) {
            System.err.println("Got an exception!");
            System.err.println(err.getMessage());
        }
    }
}