package mka.chat.server;

import java.sql.*;

public class AuthServer {
    private static Connection connection;
    private static Statement statement;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users_db.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginPassword(String login, String password) {
        String query = String.format("select nickname from users where login = '%s' and password = '%s'", login, password);
        try {
            ResultSet result = statement.executeQuery(query);
            if (result.next()) {
                return result.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
