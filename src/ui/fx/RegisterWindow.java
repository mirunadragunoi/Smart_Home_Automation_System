package ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import repository.UserRepository;

public class RegisterWindow {

    private final Stage owner;
    private final TextField emailFieldFromLogin;
    private final UserRepository userRepository = UserRepository.getInstance();

    public RegisterWindow(Stage owner, TextField emailFieldFromLogin) {
        this.owner = owner;
        this.emailFieldFromLogin = emailFieldFromLogin;
    }

    public void show() {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Smart Home - Inregistrare");

        Label title = new Label("Cont nou");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        TextField numeField = new TextField();
        numeField.setPromptText("Nume complet");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Parola (minim 6 caractere)");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirma parola");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #c62828;");

        Button registerButton = new Button("Creeaza cont");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
        registerButton.setDefaultButton(true);

        registerButton.setOnAction(e -> {
            String nume = numeField.getText() == null ? "" : numeField.getText().trim();
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (nume.isEmpty() || email.isEmpty() || password == null || password.isEmpty()) {
                statusLabel.setText("Completeaza toate campurile.");
                return;
            }
            if (password.length() < 6) {
                statusLabel.setText("Parola trebuie sa aiba minim 6 caractere.");
                return;
            }
            if (!password.equals(confirm)) {
                statusLabel.setText("Parolele nu coincid.");
                return;
            }

            try {
                if (userRepository.findByEmail(email).isPresent()) {
                    statusLabel.setText("Email-ul este deja inregistrat.");
                    return;
                }
                int id = userRepository.nextId();
                User user = new User(id, nume, email, password);
                userRepository.save(user);

                Dialogs.info("Cont creat", "Contul a fost creat cu succes. Te poti loga acum.");
                if (emailFieldFromLogin != null) {
                    emailFieldFromLogin.setText(email);
                }
                stage.close();
            } catch (Exception ex) {
                statusLabel.setText("Eroare: " + ex.getMessage());
            }
        });

        VBox card = new VBox(10,
                title,
                new Label("Nume"), numeField,
                new Label("Email"), emailField,
                new Label("Parola"), passwordField,
                new Label("Confirma parola"), confirmField,
                statusLabel,
                registerButton
        );
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 4);");
        card.setMaxWidth(380);

        HBox root = new HBox(card);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #81c784, #2e7d32);");
        root.setPadding(new Insets(30));

        stage.setScene(new Scene(root, 520, 560));
        stage.showAndWait();
    }
}
