package ui.fx.tabs;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.House;
import model.RaportEnergie;
import repository.RaportEnergieRepository;
import service.EnergieService;
import ui.fx.AppContext;
import ui.fx.Dialogs;

import java.time.format.DateTimeFormatter;

public class EnergieTab {

    private final AppContext ctx = AppContext.getInstance();
    private final EnergieService energieService = ctx.getEnergieService();

    private final ObservableList<House> housesData = FXCollections.observableArrayList();
    private final ObservableList<RaportEnergie> rapoarteData = FXCollections.observableArrayList();

    private final ComboBox<House> houseCombo = new ComboBox<>(housesData);
    private final TableView<RaportEnergie> rapoarteTable = new TableView<>(rapoarteData);
    private final Label consumLabel = new Label("Consum total: -");

    private VBox view;

    public VBox getView() {
        if (view == null) build();
        return view;
    }

    private void build() {
        Label title = new Label("Consum energetic");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        houseCombo.setConverter(new javafx.util.StringConverter<>() {
            public String toString(House h) { return h == null ? "" : "[" + h.getId() + "] " + h.getAdresa(); }
            public House fromString(String s) { return null; }
        });

        Button calcBtn = new Button("Calculeaza consum");
        calcBtn.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");
        calcBtn.setOnAction(e -> {
            House h = houseCombo.getValue();
            if (h == null) { Dialogs.error("Atentie", "Selecteaza o casa."); return; }
            try {
                double c = energieService.calculateConsum(h);
                consumLabel.setText("Consum total pentru " + h.getAdresa() + ": " + c + " kWh");
            } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
        });

        Button raportBtn = new Button("Genereaza raport");
        raportBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        raportBtn.setOnAction(e -> {
            House h = houseCombo.getValue();
            if (h == null) { Dialogs.error("Atentie", "Selecteaza o casa."); return; }
            try {
                int id = RaportEnergieRepository.getInstance().nextId();
                energieService.generateRaportEnergie(id, h);
                refresh();
            } catch (Exception ex) { Dialogs.error("Eroare", ex.getMessage()); }
        });

        setupTable();

        HBox topRow = new HBox(10, new Label("Casa:"), houseCombo, calcBtn, raportBtn);
        view = new VBox(10, title, topRow, consumLabel,
                new Label("Rapoarte generate:"), rapoarteTable);
        view.setPadding(new Insets(15));
        VBox.setVgrow(rapoarteTable, Priority.ALWAYS);
    }

    private void setupTable() {
        TableColumn<RaportEnergie, Integer> id = new TableColumn<>("Id");
        id.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        TableColumn<RaportEnergie, String> casa = new TableColumn<>("Casa");
        casa.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCasa() != null ? c.getValue().getCasa().getAdresa() : "-"));
        TableColumn<RaportEnergie, Double> consum = new TableColumn<>("Consum (kWh)");
        consum.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotalConsum()).asObject());
        TableColumn<RaportEnergie, String> gen = new TableColumn<>("Generat");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        gen.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGenerat() != null ? c.getValue().getGenerat().format(fmt) : "-"));
        rapoarteTable.getColumns().addAll(id, casa, consum, gen);
        rapoarteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

        rapoarteData.clear();
        for (RaportEnergie r : energieService.getAllRapoarte()) {
            if (r.getCasa() != null && ctx.getCurrentUser() != null
                    && r.getCasa().getOwner() != null
                    && r.getCasa().getOwner().getId() == ctx.getCurrentUser().getId()) {
                rapoarteData.add(r);
            }
        }
    }
}
