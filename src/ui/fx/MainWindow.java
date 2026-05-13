package ui.fx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.House;
import model.Room;
import model.User;
import service.AutomationService;
import service.DeviceService;
import service.EnergieService;
import service.HouseService;
import service.SenzorService;
import ui.fx.tabs.AutomationTab;
import ui.fx.tabs.CasaTab;
import ui.fx.tabs.DeviceTab;
import ui.fx.tabs.EnergieTab;
import ui.fx.tabs.SenzorTab;

public class MainWindow {

    private final Stage stage;
    private final AppContext ctx = AppContext.getInstance();

    public MainWindow(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        loadDataForCurrentUser();

        BorderPane root = new BorderPane();
        root.setTop(buildHeader());
        root.setCenter(buildTabs());

        Scene scene = new Scene(root, 1180, 760);
        stage.setScene(scene);
        stage.setTitle("Smart Home - Dashboard");
        stage.centerOnScreen();
    }

    private HBox buildHeader() {
        User user = ctx.getCurrentUser();

        Label appName = new Label("Smart Home");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        appName.setStyle("-fx-text-fill: white;");

        Label welcome = new Label("Salut, " + (user != null ? user.getNume() : "user")
                + "  (" + (user != null ? user.getEmail() : "-") + ")");
        welcome.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: white; -fx-text-fill: #1976d2; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> handleLogout());

        HBox header = new HBox(15, appName, welcome, spacer, logoutBtn);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1976d2;");
        return header;
    }

    private TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        CasaTab casaTab = new CasaTab();
        DeviceTab deviceTab = new DeviceTab();
        SenzorTab senzorTab = new SenzorTab();
        AutomationTab automationTab = new AutomationTab();
        EnergieTab energieTab = new EnergieTab();

        Tab t1 = new Tab("Case & Camere", casaTab.getView());
        Tab t2 = new Tab("Device-uri", deviceTab.getView());
        Tab t3 = new Tab("Senzori", senzorTab.getView());
        Tab t4 = new Tab("Automatizari", automationTab.getView());
        Tab t5 = new Tab("Energie", energieTab.getView());

        tabs.getTabs().addAll(t1, t2, t3, t4, t5);

        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == t1) casaTab.refresh();
            else if (newTab == t2) deviceTab.refresh();
            else if (newTab == t3) senzorTab.refresh();
            else if (newTab == t4) automationTab.refresh();
            else if (newTab == t5) energieTab.refresh();
        });

        casaTab.refresh();
        return tabs;
    }

    private void loadDataForCurrentUser() {
        HouseService houseService = ctx.getHouseService();
        DeviceService deviceService = ctx.getDeviceService();
        SenzorService senzorService = ctx.getSenzorService();
        AutomationService automationService = ctx.getAutomationService();
        EnergieService energieService = ctx.getEnergieService();

        houseService.loadFromDatabase();

        java.util.List<Room> allRooms = new java.util.ArrayList<>();
        for (House h : houseService.getAllHouses()) {
            allRooms.addAll(h.getRooms());
        }

        deviceService.loadFromDatabase(allRooms);
        senzorService.loadFromDatabase(allRooms);
        automationService.loadFromDatabase();
        energieService.loadFromDatabase();
    }

    private void handleLogout() {
        if (!Dialogs.confirm("Logout", "Sigur vrei sa te deloghezi?")) {
            return;
        }
        ctx.logout();
        new LoginWindow(stage).show();
    }
}
