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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.House;
import model.Room;
import model.device.Camera;
import model.device.Device;
import model.device.DoorLock;
import model.device.Lumina;
import model.device.Termostat;
import repository.DeviceRepository;
import service.DeviceService;
import ui.fx.AppContext;
import ui.fx.Dialogs;

import java.util.Optional;

public class DeviceTab {

    private final AppContext ctx = AppContext.getInstance();
    private final DeviceService deviceService = ctx.getDeviceService();

    private final ObservableList<House> housesData = FXCollections.observableArrayList();
    private final ObservableList<Room> roomsData = FXCollections.observableArrayList();
    private final ObservableList<Device> devicesData = FXCollections.observableArrayList();

    private final ComboBox<House> houseCombo = new ComboBox<>(housesData);
    private final ComboBox<Room> roomCombo = new ComboBox<>(roomsData);
    private final TableView<Device> devicesTable = new TableView<>(devicesData);
    private final CheckBox sortByConsum = new CheckBox("Sorteaza dupa consum");

    private VBox view;

    public VBox getView() {
        if (view == null) {
            build();
        }
        return view;
    }

    private void build() {
        Label title = new Label("Device-uri");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        configureCombos();
        setupTable();

        Button addBtn = new Button("+ Adauga device");
        addBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        addBtn.setOnAction(e -> handleAdd());

        Button removeBtn = new Button("Sterge");
        removeBtn.setOnAction(e -> handleRemove());

        Button toggleBtn = new Button("Toggle ON/OFF");
        toggleBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");
        toggleBtn.setOnAction(e -> handleToggle());

        Button moveBtn = new Button("Muta in alta camera");
        moveBtn.setOnAction(e -> handleMove());

        HBox actions = new HBox(10, addBtn, removeBtn, toggleBtn, moveBtn);

        HBox filters = new HBox(10,
                new Label("Casa:"), houseCombo,
                new Label("Camera:"), roomCombo,
                sortByConsum);

        sortByConsum.selectedProperty().addListener((o, a, b) -> refreshDevices());

        view = new VBox(10, title, filters, devicesTable, actions);
        view.setPadding(new Insets(15));
        VBox.setVgrow(devicesTable, Priority.ALWAYS);
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
            else refreshDevices();
        });
        roomCombo.valueProperty().addListener((o, a, b) -> refreshDevices());
    }

    private void setupTable() {
        TableColumn<Device, Integer> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Device, String> colTip = new TableColumn<>("Tip");
        colTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClass().getSimpleName()));

        TableColumn<Device, String> colNume = new TableColumn<>("Nume");
        colNume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNume()));

        TableColumn<Device, Boolean> colStatus = new TableColumn<>("Pornit");
        colStatus.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().getStatus()));

        TableColumn<Device, Double> colPutere = new TableColumn<>("Putere (kWh)");
        colPutere.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPutereConsumata()).asObject());

        TableColumn<Device, String> colDetalii = new TableColumn<>("Detalii");
        colDetalii.setCellValueFactory(c -> new SimpleStringProperty(detaliiDevice(c.getValue())));
        colDetalii.setPrefWidth(280);

        devicesTable.getColumns().addAll(colId, colTip, colNume, colStatus, colPutere, colDetalii);
        devicesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private String detaliiDevice(Device d) {
        if (d instanceof Lumina l) return "Luminozitate: " + l.getLuminozitate() + "%, culoare: " + l.getColor();
        if (d instanceof Termostat t) return "Temp: " + t.getTemperatura() + "°C / target: " + t.getTargetTemperatura() + "°C";
        if (d instanceof Camera c) return "Rezolutie: " + c.getRezolutie() + "p, recording: " + c.isRecording();
        if (d instanceof DoorLock dl) return "Locked: " + dl.isLocked() + ", cod: " + dl.getCodAcces();
        return "";
    }

    public void refresh() {
        House selectedHouse = houseCombo.getValue();
        housesData.clear();
        for (House h : ctx.getHouseService().getAllHouses()) {
            if (ctx.getCurrentUser() != null && h.getOwner() != null
                    && h.getOwner().getId() == ctx.getCurrentUser().getId()) {
                housesData.add(h);
            }
        }
        if (selectedHouse != null && housesData.contains(selectedHouse)) {
            houseCombo.setValue(selectedHouse);
        } else if (!housesData.isEmpty()) {
            houseCombo.getSelectionModel().select(0);
        } else {
            roomsData.clear();
            devicesData.clear();
        }
    }

    private void refreshDevices() {
        Room room = roomCombo.getValue();
        devicesData.clear();
        if (room == null) return;
        if (sortByConsum.isSelected()) {
            devicesData.addAll(deviceService.getDevicesSortedByConsum(room));
        } else {
            devicesData.addAll(room.getDevices());
        }
    }

    private void handleAdd() {
        Room room = roomCombo.getValue();
        if (room == null) {
            Dialogs.error("Atentie", "Selecteaza o camera.");
            return;
        }

        Dialog<Device> dialog = new Dialog<>();
        dialog.setTitle("Adauga device");
        dialog.setHeaderText("Device nou in camera: " + room.getNume());
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> tipCombo = new ComboBox<>();
        tipCombo.getItems().addAll("Lumina", "Termostat", "Camera", "DoorLock");
        tipCombo.setValue("Lumina");

        TextField numeField = new TextField();
        numeField.setPromptText("Nume");
        TextField putereField = new TextField("50");
        putereField.setPromptText("Putere consumata");

        TextField extra1 = new TextField();
        TextField extra2 = new TextField();
        Label extra1Label = new Label();
        Label extra2Label = new Label();

        tipCombo.valueProperty().addListener((o, a, tip) -> {
            switch (tip) {
                case "Lumina" -> {
                    extra1Label.setText("Luminozitate (0-100):"); extra1.setText("70");
                    extra2Label.setText("Culoare:"); extra2.setText("alb");
                }
                case "Termostat" -> {
                    extra1Label.setText("Temp curenta (°C):"); extra1.setText("21");
                    extra2Label.setText("Temp tinta (°C):");   extra2.setText("22");
                }
                case "Camera" -> {
                    extra1Label.setText("Rezolutie (min 240):"); extra1.setText("1080");
                    extra2Label.setText("Recording (true/false):"); extra2.setText("false");
                }
                case "DoorLock" -> {
                    extra1Label.setText("Locked (true/false):"); extra1.setText("true");
                    extra2Label.setText("Cod acces (min 4 car):"); extra2.setText("1234");
                }
            }
        });
        tipCombo.setValue("Lumina");
        extra1Label.setText("Luminozitate (0-100):"); extra1.setText("70");
        extra2Label.setText("Culoare:"); extra2.setText("alb");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Tip:"), tipCombo);
        grid.addRow(1, new Label("Nume:"), numeField);
        grid.addRow(2, new Label("Putere:"), putereField);
        grid.addRow(3, extra1Label, extra1);
        grid.addRow(4, extra2Label, extra2);
        pane.setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            try {
                int id = DeviceRepository.getInstance().nextId();
                String nume = numeField.getText();
                double putere = Double.parseDouble(putereField.getText());
                return switch (tipCombo.getValue()) {
                    case "Lumina" -> new Lumina(id, nume, false, putere, room,
                            Integer.parseInt(extra1.getText()), extra2.getText());
                    case "Termostat" -> new Termostat(id, nume, false, putere, room,
                            Double.parseDouble(extra1.getText()), Double.parseDouble(extra2.getText()));
                    case "Camera" -> new Camera(id, nume, false, putere, room,
                            Boolean.parseBoolean(extra2.getText()), Integer.parseInt(extra1.getText()));
                    case "DoorLock" -> new DoorLock(id, nume, false, putere, room,
                            Boolean.parseBoolean(extra1.getText()), extra2.getText());
                    default -> null;
                };
            } catch (Exception e) {
                Dialogs.error("Eroare input", e.getMessage());
                return null;
            }
        });

        Optional<Device> result = dialog.showAndWait();
        result.ifPresent(d -> {
            try {
                deviceService.addDevice(room, d);
                refreshDevices();
            } catch (Exception ex) {
                Dialogs.error("Eroare", ex.getMessage());
            }
        });
    }

    private void handleRemove() {
        Device d = devicesTable.getSelectionModel().getSelectedItem();
        Room room = roomCombo.getValue();
        if (d == null || room == null) { Dialogs.error("Atentie", "Selecteaza un device."); return; }
        if (!Dialogs.confirm("Confirmare", "Sterg device-ul '" + d.getNume() + "'?")) return;
        try {
            deviceService.removeDevice(room, d);
            refreshDevices();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void handleToggle() {
        Device d = devicesTable.getSelectionModel().getSelectedItem();
        if (d == null) { Dialogs.error("Atentie", "Selecteaza un device."); return; }
        try {
            if (d.getStatus()) deviceService.turnOffDevice(d);
            else deviceService.turnOnDevice(d);
            refreshDevices();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private void handleMove() {
        Device d = devicesTable.getSelectionModel().getSelectedItem();
        Room from = roomCombo.getValue();
        House house = houseCombo.getValue();
        if (d == null || from == null || house == null) {
            Dialogs.error("Atentie", "Selecteaza un device.");
            return;
        }

        ChoiceWrapper<Room> target = pickRoom(house, from);
        if (target == null) return;
        try {
            deviceService.moveDevice(d, from, target.value);
            refreshDevices();
        } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
    }

    private ChoiceWrapper<Room> pickRoom(House house, Room exclude) {
        ComboBox<Room> combo = new ComboBox<>();
        combo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Room r) { return r == null ? "" : "[" + r.getId() + "] " + r.getNume(); }
            public Room fromString(String s) { return null; }
        });
        for (Room r : house.getRooms()) {
            if (!r.equals(exclude)) combo.getItems().add(r);
        }
        if (combo.getItems().isEmpty()) {
            Dialogs.error("Atentie", "Nu exista alta camera in care sa mut device-ul.");
            return null;
        }
        combo.getSelectionModel().select(0);

        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Muta device");
        dialog.setHeaderText("Alege camera destinatie:");
        dialog.getDialogPane().setContent(new VBox(10, combo));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == ButtonType.OK ? combo.getValue() : null);

        Optional<Room> result = dialog.showAndWait();
        return result.map(ChoiceWrapper::new).orElse(null);
    }

    private static class ChoiceWrapper<T> {
        final T value;
        ChoiceWrapper(T value) { this.value = value; }
    }
}
