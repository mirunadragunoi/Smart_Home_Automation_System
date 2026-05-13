package ui.fx.tabs;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.House;
import model.Room;
import model.automatizare.Actiune;
import model.automatizare.Conditie;
import model.automatizare.RegulaAutomatizare;
import model.device.Device;
import model.senzor.Senzor;
import repository.RegulaAutomatizareRepository;
import service.AutomationService;
import ui.fx.AppContext;
import ui.fx.Dialogs;

import java.util.Optional;

public class AutomationTab {

    private final AppContext ctx = AppContext.getInstance();
    private final AutomationService automationService = ctx.getAutomationService();

    private final ObservableList<RegulaAutomatizare> reguliData = FXCollections.observableArrayList();
    private final ObservableList<Conditie> conditiiData = FXCollections.observableArrayList();
    private final ObservableList<Actiune> actiuniData = FXCollections.observableArrayList();

    private final TableView<RegulaAutomatizare> reguliTable = new TableView<>(reguliData);
    private final TableView<Conditie> conditiiTable = new TableView<>(conditiiData);
    private final TableView<Actiune> actiuniTable = new TableView<>(actiuniData);

    private VBox view;

    public VBox getView() {
        if (view == null) build();
        return view;
    }

    private void build() {
        Label title = new Label("Reguli de automatizare");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        setupReguliTable();
        setupConditiiTable();
        setupActiuniTable();

        Button addRuleBtn = new Button("+ Regula noua");
        addRuleBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        addRuleBtn.setOnAction(e -> handleAddRule());

        Button deleteRuleBtn = new Button("Sterge regula");
        deleteRuleBtn.setOnAction(e -> handleDeleteRule());

        Button activateBtn = new Button("Activeaza");
        activateBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        activateBtn.setOnAction(e -> setActiv(true));

        Button deactivateBtn = new Button("Dezactiveaza");
        deactivateBtn.setOnAction(e -> setActiv(false));

        Button addCondBtn = new Button("+ Conditie");
        addCondBtn.setOnAction(e -> handleAddConditie());

        Button addActBtn = new Button("+ Actiune");
        addActBtn.setOnAction(e -> handleAddActiune());

        Button executeBtn = new Button("Executa toate regulile");
        executeBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        executeBtn.setOnAction(e -> {
            try {
                automationService.executeRules();
                Dialogs.info("OK", "Reguli executate. Vezi consola pentru detalii.");
            } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
        });

        HBox topActions = new HBox(10, addRuleBtn, deleteRuleBtn, activateBtn, deactivateBtn, executeBtn);
        HBox detailActions = new HBox(10, addCondBtn, addActBtn);

        Label condLabel = new Label("Conditii (regula selectata):");
        condLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        Label actLabel = new Label("Actiuni (regula selectata):");
        actLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        reguliTable.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> refreshDetails());

        view = new VBox(10, title, reguliTable, topActions,
                condLabel, conditiiTable,
                actLabel, actiuniTable,
                detailActions);
        view.setPadding(new Insets(15));
        VBox.setVgrow(reguliTable, Priority.ALWAYS);
    }

    private void setupReguliTable() {
        TableColumn<RegulaAutomatizare, Integer> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<RegulaAutomatizare, String> colNume = new TableColumn<>("Nume");
        colNume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNume()));
        TableColumn<RegulaAutomatizare, Boolean> colActiv = new TableColumn<>("Activa");
        colActiv.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActiv()));
        TableColumn<RegulaAutomatizare, Integer> colCond = new TableColumn<>("Conditii");
        colCond.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getConditii().size()).asObject());
        TableColumn<RegulaAutomatizare, Integer> colAct = new TableColumn<>("Actiuni");
        colAct.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getActiuni().size()).asObject());
        reguliTable.getColumns().addAll(colId, colNume, colActiv, colCond, colAct);
        reguliTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupConditiiTable() {
        TableColumn<Conditie, Integer> id = new TableColumn<>("Id");
        id.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<Conditie, String> sens = new TableColumn<>("Senzor");
        sens.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSenzor().getNume()));
        TableColumn<Conditie, String> op = new TableColumn<>("Operator");
        op.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOperator()));
        TableColumn<Conditie, Double> val = new TableColumn<>("Valoare prag");
        val.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getValoare()).asObject());
        conditiiTable.getColumns().addAll(id, sens, op, val);
        conditiiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActiuniTable() {
        TableColumn<Actiune, Integer> id = new TableColumn<>("Id");
        id.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<Actiune, String> dev = new TableColumn<>("Device");
        dev.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDevice().getNume()));
        TableColumn<Actiune, String> cmd = new TableColumn<>("Comanda");
        cmd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComanda()));
        TableColumn<Actiune, Double> val = new TableColumn<>("Valoare");
        val.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getValoare()).asObject());
        actiuniTable.getColumns().addAll(id, dev, cmd, val);
        actiuniTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refresh() {
        reguliData.clear();
        reguliData.addAll(automationService.getAllRules());
        refreshDetails();
    }

    private void refreshDetails() {
        conditiiData.clear();
        actiuniData.clear();
        RegulaAutomatizare r = reguliTable.getSelectionModel().getSelectedItem();
        if (r != null) {
            conditiiData.addAll(r.getConditii());
            actiuniData.addAll(r.getActiuni());
        }
    }

    private void handleAddRule() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Regula noua");
        dialog.setHeaderText("Nume regula:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) return;
        try {
            int id = RegulaAutomatizareRepository.getInstance().nextId();
            automationService.createRule(id, result.get().trim());
            refresh();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void handleDeleteRule() {
        RegulaAutomatizare r = reguliTable.getSelectionModel().getSelectedItem();
        if (r == null) { Dialogs.error("Atentie", "Selecteaza o regula."); return; }
        if (!Dialogs.confirm("Confirmare", "Sterg regula '" + r.getNume() + "'?")) return;
        try {
            automationService.deleteRule(r.getId());
            refresh();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void setActiv(boolean activ) {
        RegulaAutomatizare r = reguliTable.getSelectionModel().getSelectedItem();
        if (r == null) { Dialogs.error("Atentie", "Selecteaza o regula."); return; }
        try {
            if (activ) automationService.activareRule(r);
            else automationService.dezactivareRule(r);
            refresh();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void handleAddConditie() {
        RegulaAutomatizare r = reguliTable.getSelectionModel().getSelectedItem();
        if (r == null) { Dialogs.error("Atentie", "Selecteaza o regula."); return; }

        ObservableList<Senzor> senzori = FXCollections.observableArrayList(ctx.getSenzorService().getAllSenzori());
        if (senzori.isEmpty()) { Dialogs.error("Atentie", "Adauga intai un senzor."); return; }

        ComboBox<Senzor> senzorCombo = new ComboBox<>(senzori);
        senzorCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Senzor s) { return s == null ? "" : "[" + s.getId() + "] " + s.getNume(); }
            public Senzor fromString(String x) { return null; }
        });
        senzorCombo.getSelectionModel().select(0);

        ComboBox<String> opCombo = new ComboBox<>(FXCollections.observableArrayList(">", "<", ">=", "<=", "=="));
        opCombo.setValue(">");
        TextField valField = new TextField("0");

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Conditie noua");
        dialog.setHeaderText("Conditie pentru regula: " + r.getNume());
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Senzor:"), senzorCombo);
        grid.addRow(1, new Label("Operator:"), opCombo);
        grid.addRow(2, new Label("Valoare:"), valField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK);

        Optional<Boolean> ok = dialog.showAndWait();
        if (ok.isEmpty() || !ok.get()) return;
        try {
            int condId = RegulaAutomatizareRepository.getInstance().nextConditieId();
            automationService.addConditie(r, condId, senzorCombo.getValue(), opCombo.getValue(),
                    Double.parseDouble(valField.getText()));
            refresh();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void handleAddActiune() {
        RegulaAutomatizare r = reguliTable.getSelectionModel().getSelectedItem();
        if (r == null) { Dialogs.error("Atentie", "Selecteaza o regula."); return; }

        ObservableList<Device> devices = FXCollections.observableArrayList(ctx.getDeviceService().getAllDevices());
        if (devices.isEmpty()) { Dialogs.error("Atentie", "Adauga intai un device."); return; }

        ComboBox<Device> deviceCombo = new ComboBox<>(devices);
        deviceCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Device d) { return d == null ? "" : "[" + d.getId() + "] " + d.getNume() + " (" + d.getClass().getSimpleName() + ")"; }
            public Device fromString(String x) { return null; }
        });
        deviceCombo.getSelectionModel().select(0);

        ComboBox<String> cmdCombo = new ComboBox<>(FXCollections.observableArrayList(
                "turnOn", "turnOff", "setTemperature", "setLuminozitate", "lock", "unlock"));
        cmdCombo.setValue("turnOn");
        TextField valField = new TextField("0");

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Actiune noua");
        dialog.setHeaderText("Actiune pentru regula: " + r.getNume());
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Device:"), deviceCombo);
        grid.addRow(1, new Label("Comanda:"), cmdCombo);
        grid.addRow(2, new Label("Valoare:"), valField);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK);

        Optional<Boolean> ok = dialog.showAndWait();
        if (ok.isEmpty() || !ok.get()) return;
        try {
            int actId = RegulaAutomatizareRepository.getInstance().nextActiuneId();
            automationService.addActiune(r, actId, deviceCombo.getValue(), cmdCombo.getValue(),
                    Double.parseDouble(valField.getText()));
            refresh();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }
}
