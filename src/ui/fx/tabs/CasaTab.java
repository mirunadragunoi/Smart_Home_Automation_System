package ui.fx.tabs;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.House;
import model.Room;
import model.User;
import repository.HouseRepository;
import repository.RoomRepository;
import service.HouseService;
import ui.fx.AppContext;
import ui.fx.Dialogs;

import java.util.List;
import java.util.Optional;

public class CasaTab {

    private final AppContext ctx = AppContext.getInstance();
    private final HouseService houseService = ctx.getHouseService();

    private final ObservableList<House> housesData = FXCollections.observableArrayList();
    private final ObservableList<Room> roomsData = FXCollections.observableArrayList();

    private final TableView<House> housesTable = new TableView<>(housesData);
    private final TableView<Room> roomsTable = new TableView<>(roomsData);

    private VBox view;

    public VBox getView() {
        if (view == null) {
            build();
        }
        return view;
    }

    private void build() {
        Label title = new Label("Casele tale");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        setupHousesTable();
        setupRoomsTable();

        Button addHouseBtn = new Button("+ Adauga casa");
        addHouseBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        addHouseBtn.setOnAction(e -> handleAddHouse());

        Button deleteHouseBtn = new Button("Sterge casa selectata");
        deleteHouseBtn.setOnAction(e -> handleDeleteHouse());

        HBox houseActions = new HBox(10, addHouseBtn, deleteHouseBtn);

        Label roomsLabel = new Label("Camerele casei selectate");
        roomsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        Button addRoomBtn = new Button("+ Adauga camera");
        addRoomBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        addRoomBtn.setOnAction(e -> handleAddRoom());

        Button deleteRoomBtn = new Button("Sterge camera selectata");
        deleteRoomBtn.setOnAction(e -> handleDeleteRoom());

        HBox roomActions = new HBox(10, addRoomBtn, deleteRoomBtn);

        housesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldH, newH) -> refreshRooms());

        view = new VBox(10, title, housesTable, houseActions, roomsLabel, roomsTable, roomActions);
        view.setPadding(new Insets(15));
        VBox.setVgrow(housesTable, Priority.ALWAYS);
        VBox.setVgrow(roomsTable, Priority.ALWAYS);
    }

    private void setupHousesTable() {
        TableColumn<House, Integer> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<House, String> colAdresa = new TableColumn<>("Adresa");
        colAdresa.setCellValueFactory(new PropertyValueFactory<>("adresa"));
        colAdresa.setPrefWidth(380);

        TableColumn<House, Integer> colCamere = new TableColumn<>("Numar camere");
        colCamere.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getRooms().size()).asObject());

        housesTable.getColumns().addAll(colId, colAdresa, colCamere);
        housesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupRoomsTable() {
        TableColumn<Room, Integer> colId = new TableColumn<>("Id");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Room, String> colNume = new TableColumn<>("Nume");
        colNume.setCellValueFactory(new PropertyValueFactory<>("nume"));

        TableColumn<Room, String> colType = new TableColumn<>("Tip");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Room, Integer> colDevices = new TableColumn<>("Device-uri");
        colDevices.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDevices().size()).asObject());

        TableColumn<Room, Integer> colSenzori = new TableColumn<>("Senzori");
        colSenzori.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getSenzori().size()).asObject());

        roomsTable.getColumns().addAll(colId, colNume, colType, colDevices, colSenzori);
        roomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void refresh() {
        User currentUser = ctx.getCurrentUser();
        List<House> all = houseService.getAllHouses();
        housesData.clear();
        for (House h : all) {
            if (currentUser != null && h.getOwner() != null && h.getOwner().getId() == currentUser.getId()) {
                housesData.add(h);
            }
        }
        if (!housesData.isEmpty()) {
            housesTable.getSelectionModel().select(0);
        }
        refreshRooms();
    }

    private void refreshRooms() {
        House selected = housesTable.getSelectionModel().getSelectedItem();
        ctx.setCurrentHouse(selected);
        roomsData.clear();
        if (selected != null) {
            roomsData.addAll(selected.getRooms());
        }
    }

    private void handleAddHouse() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adauga casa");
        dialog.setHeaderText("Adresa casei noi");
        dialog.setContentText("Adresa:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        String adresa = result.get().trim();
        if (adresa.isEmpty()) {
            Dialogs.error("Eroare", "Adresa nu poate fi goala.");
            return;
        }
        try {
            int id = HouseRepository.getInstance().nextId();
            houseService.createHouse(id, adresa, ctx.getCurrentUser());
            refresh();
        } catch (Exception ex) {
            Dialogs.error("Eroare", ex.getMessage());
        }
    }

    private void handleDeleteHouse() {
        House selected = housesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Atentie", "Selecteaza o casa.");
            return;
        }
        if (!Dialogs.confirm("Confirmare", "Sterg casa '" + selected.getAdresa() + "' si toate camerele ei?")) {
            return;
        }
        try {
            for (Room r : new java.util.ArrayList<>(selected.getRooms())) {
                houseService.removeRoom(selected, r);
            }
            HouseRepository.getInstance().deleteById(selected.getId());
            refresh();
        } catch (Exception ex) {
            Dialogs.error("Eroare", ex.getMessage());
        }
    }

    private void handleAddRoom() {
        House selected = housesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Atentie", "Selecteaza intai o casa.");
            return;
        }

        javafx.scene.control.Dialog<javafx.util.Pair<String, String>> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Adauga camera");
        dialog.setHeaderText("Camera noua in: " + selected.getAdresa());

        TextField numeField = new TextField();
        numeField.setPromptText("ex: Dormitor");
        javafx.scene.control.ComboBox<String> typeCombo = new javafx.scene.control.ComboBox<>();
        typeCombo.getItems().addAll("Living", "Dormitor", "Bucatarie", "Baie", "Birou", "Hol", "Pivnita", "Garaj");
        typeCombo.setValue("Living");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(15));
        grid.add(new Label("Nume:"), 0, 0); grid.add(numeField, 1, 0);
        grid.add(new Label("Tip:"), 0, 1);  grid.add(typeCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dialog.setResultConverter(bt -> bt == javafx.scene.control.ButtonType.OK
                ? new javafx.util.Pair<>(numeField.getText(), typeCombo.getValue())
                : null);

        Optional<javafx.util.Pair<String, String>> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try {
            int id = RoomRepository.getInstance().nextId();
            houseService.addRoom(selected, id, result.get().getKey(), result.get().getValue());
            refreshRooms();
            housesTable.refresh();
        } catch (Exception ex) {
            Dialogs.error("Eroare", ex.getMessage());
        }
    }

    private void handleDeleteRoom() {
        House houseSelected = housesTable.getSelectionModel().getSelectedItem();
        Room roomSelected = roomsTable.getSelectionModel().getSelectedItem();
        if (houseSelected == null || roomSelected == null) {
            Dialogs.error("Atentie", "Selecteaza o camera.");
            return;
        }
        if (!Dialogs.confirm("Confirmare", "Sterg camera '" + roomSelected.getNume() + "'?")) {
            return;
        }
        try {
            houseService.removeRoom(houseSelected, roomSelected);
            refreshRooms();
            housesTable.refresh();
        } catch (Exception ex) {
            Dialogs.error("Eroare", ex.getMessage());
        }
    }
}
