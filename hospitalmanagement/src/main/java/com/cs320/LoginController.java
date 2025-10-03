package com.cs320;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private ImageView logoImageView;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private void initialize() {
        // Logo yükleme
        // logoImageView.setImage(new
        // Image(getClass().getResourceAsStream("/com/cs320/logo.jpg")));

        // Enter tuşu ile login
        passwordField.setOnAction(e -> handleLogin());

        // Error label'ını başlangıçta gizle
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Kullanıcı adı ve şifre boş olamaz!");
            return;
        }

        User user = authenticateUser(username, password);
        if (user != null) {
            // Login başarılı
            showSuccess("Giriş başarılı! Hoş geldiniz " + user.getName());

            // Rol kontrolü yaparak uygun sayfaya yönlendir
            redirectToUserDashboard(user);
        } else {
            showError("Kullanıcı adı veya şifre hatalı!");
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Şifre Sıfırlama");
        alert.setHeaderText("Şifre Sıfırlama");
        alert.setContentText("Lütfen sistem yöneticinizle iletişime geçin.");
        alert.showAndWait();
    }

    private User authenticateUser(String username, String password) {
        String query = "SELECT u.*, r.name as role_name FROM User u " +
                "JOIN Role r ON u.role_id = r.role_id " +
                "WHERE u.email = ? AND u.password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Gerçek projede hash kullanılmalı

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getInt("role_id"),
                        rs.getString("role_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Veritabanı bağlantı hatası: " + e.getMessage());
        }
        return null;
    }

    private void redirectToUserDashboard(User user) {
        try {
            String fxmlFile = "";
            String title = "";

            switch (user.getRoleName().toLowerCase()) {
                case "admin":
                    fxmlFile = "admin-dashboard";
                    title = "Admin Dashboard";
                    break;
                case "manager":
                    fxmlFile = "manager-dashboard";
                    title = "Manager Dashboard";
                    break;
                case "doctor":
                    fxmlFile = "doctor-dashboard";
                    title = "Doctor Dashboard";
                    break;
                case "patient":
                    fxmlFile = "patient-dashboard";
                    title = "Patient Dashboard";
                    break;
                default:
                    showError("Geçersiz kullanıcı rolü!");
                    return;
            }

            // Yeni sahne oluştur
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile + ".fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Sayfa yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #dc3545;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #28a745;");
    }
}