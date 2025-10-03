package com.cs320;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_management_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Al1071145301"; // MySQL şifrenizi buraya yazın

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Veritabanı bağlantısı başarılı!"); // Debug için
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver bulunamadı: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Veritabanı bağlantısı kapatıldı.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}