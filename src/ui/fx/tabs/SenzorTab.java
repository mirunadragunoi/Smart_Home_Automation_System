package ui.fx.tabs;

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
import model.senzor.Senzor;
import model.senzor.SenzorFum;
import model.senzor.SenzorLumina;
import model.senzor.SenzorMiscare;
import model.senzor.SenzorTemperatura;
import repository.SenzorRepository;
import service.SenzorService;
import ui.fx.AppContext;
import ui.fx.Dialogs;

import java.util.Optional;

public class SenzorTab {

    private final AppContext ctx = AppContext.getInstance();
    private final SenzorService senzorService = ctx.getSenzorService();

    private final ObservableList<House> housesData = FXCollections.observableArrayList();
    private final ObservableList<Room> roomsData = FXCollections.observableArrayList();
    private final ObservableList<Senzor> senzoriData = FXCollections.observableArrayList();

    private final ComboBox<House> houseCombo = new ComboBox<>(housesData);
    private final ComboBox<Room> roomCombo = new ComboBox<>(roomsData);
    private final TableView<Senzor> senzoriTable = new TableView<>(senzoriData);

    private VBox view;

    public VBox getView() {
        if (view == null) build();
        return view;
    }

    private void build() {
        Label title = new Label("Senzori");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        configureCombos();
        setupTable();

        Button addBtn = new Button("+ Adauga senzor");
        addBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        addBtn.setOnAction(e -> handleAdd());

        Button readBtn = new Button("Citeste valoare");
        readBtn.setOnAction(e -> handleRead());

        Button setBtn = new Button("Seteaza valoare");
        setBtn.setOnAction(e -> handleSet());

        Button simulateBtn = new Button("Simuleaza");
        simulateBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        simulateBtn.setOnAction(e -> handleSimulate());

        HBox actions = new HBox(10, addBtn, readBtn, setBtn, simulateBtn);

        HBox filters = new HBox(10,
                new Label("Casa:"), houseCombo,
                new Label("Camera:"), roomCombo);

        view = new VBox(10, title, filters, senzoriTable, actions);
        view.setPadding(new Insets(15));
        VBox.setVgrow(senzoriTable, Priority.ALWAYS);
    }

    private void configureCombos() {
        houseCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(House h) { return h == null ? "" : "[" + h.getId() + "] " + h.getAdresa(); }
            public House fromString(String s) { return null; }
        });
        roomCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Room r) { return r == null ? "" : "[" + r.getId() + "] " + r.getNume(); }
            public Room fromString(String s) { return null; }
        });
        houseCombo.valueProperty().addListener((o, a, h) -> {
            roomsData.clear();
            if (h != null) roomsData.addAll(h.getRooms());
            if (!roomsData.isEmpty()) roomCombo.getSelectionModel().select(0);
            else refreshSenzori();
        });
        roomCombo.valueProperty().addListener((o, a, b) -> refreshSenzori());
    }

    private void setupTable() {
        TableColumn<Senzor, Integer> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Senzor, String> colTip = new TableColumn<>("Tip");
        colTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClass().getSimpleName()));

        TableColumn<Senzor, String> colNume = new TableColumn<>("Nume");
        colNume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNume()));

        TableColumn<Senzor, Double> colVal = new TableColumn<>("Valoare");
        colVal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getValoare()).asObject());

        senzoriTable.getColumns().addAll(colId, colTip, colNume, colVal);
        senzoriTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refresh() {
        House selected = houseCombo.getValue();
        housesData.clear();
        for (House h : ctx.getHouseService().getAllHouses()) {
            if (ctx.getCurrentUser() != null && h.getOwner() != null
                    && h.getOwner().getId() == ctx.getCurrentUser().getId()) {
                housesData.add(h);
            }
        }
        if (selected != null && housesData.contains(selected)) houseCombo.setValue(selected);
        else if (!housesData.isEmpty()) houseCombo.getSelectionModel().select(0);
        else { roomsData.clear(); senzoriData.clear(); }
    }

    private void refreshSenzori() {
        senzoriData.clear();
        Room room = roomCombo.getValue();
        if (room != null) senzoriData.addAll(room.getSenzori());
    }

    private void handleAdd() {
        Room room = roomCombo.getValue();
        if (room == null) { Dialogs.error("Atentie", "Selecteaza o camera."); return; }

        Dialog<Senzor> dialog = new Dialog<>();
        dialog.setTitle("Adauga senzor");
        dialog.setHeaderText("Senzor nou in: " + room.getNume());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> tipCombo = new ComboBox<>();
        tipCombo.getItems().addAll("Temperatura", "Lumina", "Fum", "Miscare");
        tipCombo.setValue("Temperatura");

        TextField numeField = new TextField();
        numeField.setPromptText("ex: Senzor Living");
        TextField valoareField = new TextField("0");
        valoareField.setPromptText("Valoare initiala");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Tip:"), tipCombo);
        grid.addRow(1, new Label("Nume:"), numeField);
        grid.addRow(2, new Label("Valoare:"), valoareField);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                int id = SenzorRepository.getInstance().nextId();
                double valoare = Double.parseDouble(valoareField.getText());
                String nume = numeField.getText();
                return switch (tipCombo.getValue()) {
                    case "Temperatura" -> new SenzorTemperatura(id, nume, valoare, room, valoare);
                    case "Lumina"      -> new SenzorLumina(id, nume, valoare, room, valoare);
                    case "Fum"         -> new SenzorFum(id, nume, valoare, room, valoare > 0);
                    case "Miscare"     -> new SenzorMiscare(id, nume, valoare, room, valoare > 0);
                    default -> null;
                };
            } catch (Exception e) {
                Dialogs.error("Eroare input", e.getMessage());
                return null;
            }
        });

        Optional<Senzor> result = dialog.showAndWait();
        result.ifPresent(s -> {
            try {
                senzorService.addSenzor(room, s);
                refreshSenzori();
            } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
        });
    }

    private void handleRead() {
        Senzor s = senzoriTable.getSelectionModel().getSelectedItem();
        if (s == null) { Dialogs.error("Atentie", "Selecteaza un senzor."); return; }
        double v = senzorService.readSenzor(s);
        Dialogs.info("Citire", s.getNume() + " = " + v);
    }

    private void handleSet() {
        Senzor s = senzoriTable.getSelectionModel().getSelectedItem();
        if (s == null) { Dialogs.error("Atentie", "Selecteaza un senzor."); return; }
        TextInputDialog dialog = new TextInputDialog(String.valueOf(s.getValoare()));
        dialog.setTitle("Set valoare");
        dialog.setHeaderText("Noua valoare pentru " + s.getNume());
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try {
            senzorService.updateSenzorValue(s, Double.parseDouble(result.get()));
            refreshSenzori();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void handleSimulate() {
        Senzor s = senzoriTable.getSelectionModel().getSelectedItem();
        if (s == null) { Dialogs.error("Atentie", "Selecteaza un senzor."); return; }

        Dialog<double[]> dialog = new Dialog<>();
        dialog.setTitle("Simuleaza senzor");
        dialog.setHeaderText("Interval pentru " + s.getNume());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField minF = new TextField("0");
        TextField maxF = new TextField("30");
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Min:"), minF);
        grid.addRow(1, new Label("Max:"), maxF);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(bt -> bt == ButtonType.OK
                ? new double[]{Double.parseDouble(minF.getText()), Double.parseDouble(maxF.getText())}
                : null);

        Optional<double[]> result = dialog.showAndWait();
        result.ifPresent(range -> {
            try {
                senzorService.simulateSenzorValue(s, range[0], range[1]);
                refreshSenzori();
            } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
        });
    }
}
