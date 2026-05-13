package ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.User;
import repository.UserRepository;

import java.util.Optional;

public class LoginWindow {

    private final Stage stage;
    private final UserRepository userRepository = UserRepository.getInstance();

    public LoginWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        Label title = new Label("Smart Home Login");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label subtitle = new Label("Bine ai venit! Autentifica-te ca sa accesezi casa ta inteligenta.");
        subtitle.setStyle("-fx-text-fill: #555;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Parola");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #c62828;");

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");

        Hyperlink registerLink = new Hyperlink("Nu ai cont? Inregistreaza-te");

        loginButton.setOnAction(e -> handleLogin(emailField.getText(), passwordField.getText(), statusLabel));
        registerLink.setOnAction(e -> new RegisterWindow(stage, emailField).show());

        VBox card = new VBox(12,
                title,
                subtitle,
                new Label("Email"), emailField,
                new Label("Parola"), passwordField,
                statusLabel,
                loginButton,
                registerLink
        );
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 4);");
        card.setMaxWidth(380);

        HBox root = new HBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #64b5f6, #1976d2);");
        root.setPadding(new Insets(40));

        Scene scene = new Scene(root, 600, 520);
        stage.setScene(scene);
        stage.setTitle("Smart Home - Login");
        stage.show();
    }

    private void handleLogin(String email, String password, Label statusLabel) {
        statusLabel.setText("");
        if (email == null || email.trim().isEmpty()) {
            statusLabel.setText("Introdu email-ul.");
            return;
        }
        if (password == null || password.isEmpty()) {
            statusLabel.setText("Introdu parola.");
            return;
        }

        try {
            Optional<User> userOpt = userRepository.findByEmail(email.trim());
            if (userOpt.isEmpty()) {
                statusLabel.setText("Email-ul nu este inregistrat.");
                return;
            }
            User user = userOpt.get();
            if (!user.getPassword().equals(password)) {
                statusLabel.setText("Parola este incorecta.");
                return;
            }

            AppContext ctx = AppContext.getInstance();
            ctx.setCurrentUser(user);
            new MainWindow(stage).show();
        } catch (Exception ex) {
            statusLabel.setText("Eroare: " + ex.getMessage());
        }
    }
}
