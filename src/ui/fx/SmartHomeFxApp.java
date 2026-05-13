package ui.fx;

import config.DatabaseConfig;
import javafx.application.Application;
import javafx.stage.Stage;

public class SmartHomeFxApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        new LoginWindow(primaryStage).show();
    }

    @Override
    public void stop() {
        DatabaseConfig.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
