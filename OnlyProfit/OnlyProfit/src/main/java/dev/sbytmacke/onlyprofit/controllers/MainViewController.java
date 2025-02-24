package dev.sbytmacke.onlyprofit.controllers;

import dev.sbytmacke.onlyprofit.constants.PathConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.models.UserEntity;
import dev.sbytmacke.onlyprofit.routes.RoutesManager;
import dev.sbytmacke.onlyprofit.utils.DateFormatterUtils;
import dev.sbytmacke.onlyprofit.utils.GlobalStats;
import dev.sbytmacke.onlyprofit.utils.TimeUtils;
import dev.sbytmacke.onlyprofit.viewmodel.UserViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.control.Alert.AlertType.ERROR;

public class MainViewController {
    private static final int NUM_USERS_TO_SHOW_RANKING = 20;
    private static final int DECISION_STRICT_FILTER_RELIABLE = 60;
    private final String noDataTime = "--:--";
    public UserViewModel userViewModel;
    Logger logger = LoggerFactory.getLogger(getClass());
    @FXML
    private Button buttonMainMiniView;
    // Menu
    @FXML
    private MenuItem menuDeleteData;
    @FXML
    private MenuItem menuLeyenda;
    @FXML
    private MenuItem menuUpdateData;
    @FXML
    private MenuItem menuBackup;
    @FXML
    private MenuItem menuRanking;
    /* Create user */
    @FXML
    private Button buttonCleanSaveUsername;
    @FXML
    private TextField textFieldUser;
    private ContextMenu contextMenu;
    @FXML
    private DatePicker datePicker;
    @FXML
    private RadioButton radioButtonGood;
    @FXML
    private RadioButton radioButtonBad;
    @FXML
    private Button buttonCreateUser;
    @FXML
    private ComboBox<String> comboTime;
    /* Filter */
    @FXML
    private Button buttonClearFilters;
    @FXML
    private TextField textSearchUserFilter;
    @FXML
    private ComboBox<String> comboTimeFilter;
    @FXML
    private RadioButton radioButtonHideTime;
    @FXML
    private RadioButton radioButtonHideGreen;
    @FXML
    private RadioButton radioButtonHideOrange;
    @FXML
    private RadioButton radioButtonHideRed;
    @FXML
    private CheckBox starCheckBox;
    @FXML
    private RadioButton radioButtonNone;
    @FXML
    private RadioButton radioButtonMonday;
    @FXML
    private RadioButton radioButtonTuesday;
    @FXML
    private RadioButton radioButtonWednesday;
    @FXML
    private RadioButton radioButtonThursday;
    @FXML
    private RadioButton radioButtonFriday;
    @FXML
    private RadioButton radioButtonSaturday;
    @FXML
    private RadioButton radioButtonSunday;
    /* Table General */
    @FXML
    private TableView<UserDTO> tableUsers;
    @FXML
    private TableColumn<UserDTO, String> columnUsername;
    @FXML
    private TableColumn<UserDTO, String> columnSuccess;
    @FXML
    private TableColumn<UserDTO, String> columnTotalBets;
    /* Table Ranking Global */
    @FXML
    private TableView<UserDTO> tableUsersRanking;
    @FXML
    private TableColumn<UserDTO, String> columnUsernameRanking;
    @FXML
    private TableColumn<UserDTO, String> columnSuccessRanking;
    @FXML
    private TableColumn<UserDTO, String> columnTotalBetsRanking;
    // Global Result
    @FXML
    private Label textFinalResultDate;
    @FXML
    private Label textFinalResultTime;
    @FXML
    private Label textFinalResultPercentSuccess;
    @FXML
    private Label textFinalResultTotalBets;
    @FXML
    private ImageView imageRefresh;
    @FXML
    private PieChart pieChart;
    @FXML
    private Text noDataMessagePieChart;

    public TableView<UserDTO> getTableUsers() {
        return tableUsers;
    }

    public void init(UserViewModel userViewModel) {
        logger.info("Initializing MainViewController");
        this.userViewModel = userViewModel;
        initBindings();
        initDetails();
        initEvents();
        updatePieChart(FXCollections.observableArrayList());
    }

    private void updatePieChart(List<UserDTO> users) {
        pieChart.getData().clear();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (UserDTO user : users) {
            // Limitar la longitud del nombre, ya que pieChar no permite mucho espacio
            String displayName = user.getUsername().length() > 14
                    ? user.getUsername().substring(0, 10) + "..." // Mostrar solo los primeros 10 caracteres seguidos de "..."
                    : user.getUsername();

            PieChart.Data slice = new PieChart.Data(displayName, user.getTotalBets());
            pieChartData.add(slice);
        }

        if (pieChartData.isEmpty()) {
            noDataMessagePieChart.setVisible(true);
            noDataMessagePieChart.setManaged(true);
            pieChart.setVisible(false);
            pieChart.setManaged(false);
            noDataMessagePieChart.setVisible(true);
        } else {
            pieChart.setVisible(true);
            pieChart.setManaged(true);
            noDataMessagePieChart.setVisible(false);
            noDataMessagePieChart.setManaged(false);
            noDataMessagePieChart.setVisible(false);
            pieChart.setData(pieChartData);
        }

        // Cambiar el color del texto de las etiquetas
        pieChart.getStylesheets().add(Objects.requireNonNull(getClass().getResource(PathConstants.PIE_CHART_CSS.getPath())).toExternalForm());
        for (PieChart.Data data : pieChart.getData()) {
            data.getNode().getStyleClass().add("chart-pie-label");
        }
    }

    private void initBindings() {
        logger.info("Initializing Bindings");

        // Table Ranking Global
        updateRankingTable();

        columnUsernameRanking.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnSuccessRanking.setCellValueFactory(new PropertyValueFactory<>("percentReliable"));
        columnTotalBetsRanking.setCellValueFactory(new PropertyValueFactory<>("totalBets"));

        // ComboTime
        comboTime.setItems(FXCollections.observableArrayList(TimeUtils.getAllSliceHours()));
        comboTime.getSelectionModel().select(0);

        // ComboTimeFilter
        comboTimeFilter.setItems(FXCollections.observableArrayList(TimeUtils.getAllSliceHours()));
        comboTimeFilter.getSelectionModel().select(0);

        // Limpiamos la tabla
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnSuccess.setCellValueFactory(new PropertyValueFactory<>("percentReliable"));
        columnTotalBets.setCellValueFactory(new PropertyValueFactory<>("totalBets"));

        ToggleGroup toggleGroup = new ToggleGroup();
        //radioButtonGood.setToggleGroup(toggleGroup);
        //radioButtonBad.setToggleGroup(toggleGroup);

        ToggleGroup toggleGroupDaysOfWeek = new ToggleGroup();
        radioButtonNone.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonMonday.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonTuesday.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonWednesday.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonThursday.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonFriday.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonSaturday.setToggleGroup(toggleGroupDaysOfWeek);
        radioButtonSunday.setToggleGroup(toggleGroupDaysOfWeek);
    }

    private void initDetails() {
        radioButtonNone.setSelected(true);
        // tableUsers.setSelectionModel(null);
        // tableUsersRanking.setSelectionModel(null);

        centerAndFontTextTable();
        setColorsTable();

        comboTimeFilter.getStylesheets().add(Objects.requireNonNull(getClass().getResource(PathConstants.COMBO_BOX_CSS.getPath())).toExternalForm());
        comboTime.getStylesheets().add(Objects.requireNonNull(getClass().getResource(PathConstants.COMBO_BOX_CSS.getPath())).toExternalForm());
        tableUsers.getStylesheets().add(Objects.requireNonNull(getClass().getResource(PathConstants.TABLE_USERS_CSS.getPath())).toExternalForm());
        tableUsersRanking.getStylesheets().add(Objects.requireNonNull(getClass().getResource(PathConstants.TABLE_USERS_CSS.getPath())).toExternalForm());
        imageRefresh.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(PathConstants.REFRESH_ICON.getPath()))));
    }

    private void initEvents() {
        logger.info("Initializing Events");

        imageRefresh.setOnMouseEntered(event -> imageRefresh.setOpacity(0.5));
        imageRefresh.setOnMouseExited(event -> imageRefresh.setOpacity(1));
        imageRefresh.setOnMouseClicked(event -> {
            logger.info("Refreshing data");
            userViewModel.refreshData(userViewModel.getAll());
            //tableUsers.setItems(FXCollections.observableArrayList(userViewModel.getAll()));
            updateMainTable();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Actualización");
            alert.setHeaderText("Actualización ✅");
            alert.setContentText("Datos actualizados correctamente");
            alert.showAndWait();
        });

        // Evento para abrir la ventana modal al hacer clic en una fila de la tabla
        clickEvents();

        starCheckBox.setOnAction(event -> updateMainTable());

        radioButtonHideTime.setOnAction(event -> updateMainTable());

        menuBackup.setOnAction(event -> onBackupMenuAction());

        buttonCleanSaveUsername.setOnAction(event -> textFieldUser.setText(null));

        buttonClearFilters.setOnAction(event -> {
            textSearchUserFilter.setText("");
            comboTimeFilter.getSelectionModel().select(0);
            radioButtonHideTime.setSelected(false);
            radioButtonHideGreen.setSelected(false);
            radioButtonHideOrange.setSelected(false);
            radioButtonHideRed.setSelected(false);
            radioButtonMonday.setSelected(false);
            radioButtonTuesday.setSelected(false);
            radioButtonWednesday.setSelected(false);
            radioButtonThursday.setSelected(false);
            radioButtonFriday.setSelected(false);
            radioButtonSaturday.setSelected(false);
            radioButtonSunday.setSelected(false);
            updateMainTable();

            // PieChart
            noDataMessagePieChart.setVisible(true);
            noDataMessagePieChart.setManaged(true);
            pieChart.setVisible(false);
            pieChart.setManaged(false);
            noDataMessagePieChart.setVisible(true);
        });

        DateFormatterUtils dateFormatterUtils = new DateFormatterUtils();

        menuDeleteData.setOnAction(event -> onDataGestorMenuAction());
        menuLeyenda.setOnAction(event -> {
            try {
                onLeyendaMenuAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        menuUpdateData.setOnAction(event -> {
            try {
                onUpdateMenuAction();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        menuRanking.setOnAction(event -> onRankingMenuAction());

        buttonCreateUser.setOnAction(event -> {
            saveUser();

            // Actualizamos el ranking general
            updateRankingTable();
        });

        // Filters
        textSearchUserFilter.setOnKeyReleased(event -> updateMainTable());
        comboTimeFilter.getSelectionModel().selectedItemProperty().addListener(event -> updateMainTable());

        // Cambiamos también en el DatePicker al añadir
        DateTimeFormatter dateFormatterDatePicker = dateFormatterUtils.formatDate(datePicker);
        datePicker.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                LocalDate parsedDate = LocalDate.parse(newValue, dateFormatterDatePicker);
                datePicker.setValue(parsedDate); // Actualiza el DatePicker
            } catch (DateTimeParseException e) {
                logger.error("No Valid - DatePickerFilter: " + newValue);
            }
        });

        radioButtonHideGreen.setOnAction(event -> updateMainTable());
        radioButtonHideOrange.setOnAction(event -> updateMainTable());
        radioButtonHideRed.setOnAction(event -> updateMainTable());

        radioButtonNone.setOnAction(event -> updateMainTable());
        radioButtonMonday.setOnAction(event -> updateMainTable());
        radioButtonTuesday.setOnAction(event -> updateMainTable());
        radioButtonWednesday.setOnAction(event -> updateMainTable());
        radioButtonThursday.setOnAction(event -> updateMainTable());
        radioButtonFriday.setOnAction(event -> updateMainTable());
        radioButtonSaturday.setOnAction(event -> updateMainTable());
        radioButtonSunday.setOnAction(event -> updateMainTable());

        contextMenu = new ContextMenu();
        textFieldUser.textProperty().addListener((observable, oldValue, newValue) -> {
            contextMenu.getItems().clear();

            if (newValue != null) {
                List<String> listSuggestions = filterSuggestionsList(newValue.trim());

                // Agrega sugerencias al ContextMenu basadas en el valor del TextField
                for (String suggestion : listSuggestions) {
                    MenuItem menuItem = new MenuItem(suggestion);
                    //menuItem.setStyle("-fx-text-fill: white;"); // Cambia el color del texto a blanco
                    contextMenu.getItems().add(menuItem);
                }
            }

            contextMenu.show(textFieldUser, Side.BOTTOM, 0, 0);

            if (newValue == null || newValue.isEmpty()) {
                contextMenu.getItems().clear();
            }
        });

        textSearchUserFilter.textProperty().addListener((observable, oldValue, newValue) -> {
            contextMenu.getItems().clear();

            List<String> listSuggestions = filterSuggestionsList(newValue.trim());

            // Agrega sugerencias al ContextMenu basadas en el valor del TextField
            for (String suggestion : listSuggestions) {
                MenuItem menuItem = new MenuItem(suggestion);
                //menuItem.setStyle("-fx-text-fill: white;"); // Cambia el color del texto a blanco
                contextMenu.getItems().add(menuItem);
            }

            contextMenu.show(textSearchUserFilter, Side.BOTTOM, 0, 0);

            if (newValue == null || newValue.isEmpty()) {
                contextMenu.getItems().clear();
            }
        });

        contextMenu.setOnAction(event -> {
            MenuItem selectedItem = (MenuItem) event.getTarget();

            if (textFieldUser.isFocused()) {
                textFieldUser.setText(selectedItem.getText());
            } else if (textSearchUserFilter.isFocused()) {
                textSearchUserFilter.setText(selectedItem.getText());
                updateMainTable();
            }
            contextMenu.hide();
        });

        // Al final para que se actualice la tabla principal
        buttonMainMiniView.setOnAction(event -> {
            logger.info("Initializing MainMiniView");
            RoutesManager routesManager = new RoutesManager();
            try {
                routesManager.initMainMiniView(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void clickEvents(){
        tableUsers.setOnMouseClicked(event -> {
            RoutesManager routesManager = new RoutesManager();
            try {
                UserDTO selectedItem = tableUsers.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                selectedItem.setUsername(selectedItem.getUsername().replace("⭐ ", ""));
                routesManager.initUserDetailModal(selectedItem);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        /*tableUsersRanking.setOnMouseClicked(event -> {
            RoutesManager routesManager = new RoutesManager();
            try {
                UserDTO selectedItem = tableUsersRanking.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                UserDTO userDTOCopy = new UserDTO(selectedItem.getUsername(), selectedItem.getPercentReliable(), selectedItem.getTotalBets(), selectedItem.getTotalSuccess());
                selectedItem.setUsername(userDTOCopy.getUsername().replace("  🥇 ", ""));
                selectedItem.setUsername(userDTOCopy.getUsername().replace("  \uD83E\uDD48 ", ""));
                selectedItem.setUsername(userDTOCopy.getUsername().replace("  \uD83E\uDD49  ", ""));
                selectedItem.setUsername(userDTOCopy.getUsername().replace(".*\\.\\s+", ""));
                routesManager.initUserDetailModal(userDTOCopy);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });*/
    }

    private void updateRankingTable() {
        List<UserDTO> filterTopUsersReliable = filterStartsAndRakingUsersReliable(userViewModel.getAll());
        tableUsersRanking.setItems(FXCollections.observableArrayList(filterTopUsersReliable));
        orderByTotalSuccessBets(tableUsersRanking);
        listUsers(tableUsersRanking.getItems());
    }

    private void onRankingMenuAction() {
        logger.info("Initializing Ranking-Charts View");
        RoutesManager routesManager = new RoutesManager();
        try {
            routesManager.initChartsView(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void onBackupMenuAction() {
        logger.info("Initializing Backup");

        if (userViewModel.backupData().isEmpty()) {
            Alert alert = new Alert(ERROR);

            // Heredar el ícono de la ventana principal
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().addAll(RoutesManager.getMainStage().getIcons());

            logger.error("Backup failed");

            alert.setTitle("Backup");
            alert.setHeaderText("Backup");
            alert.setContentText("Error al realizar el backup");
            alert.showAndWait();
            return;
        }

        try {
            List<Document> documents = userViewModel.backupData(); // Obtiene la lista de documentos BSON

            // Para que quede legible
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            try (FileWriter fileWriter = new FileWriter("backup.json")) {
                fileWriter.write("[");

                for (int i = 0; i < documents.size(); i++) {
                    Document document = documents.get(i);

                    // Convierte el documento BSON a formato JSON
                    String json = gson.toJson(document);

                    // Escribe el documento JSON en el archivo
                    fileWriter.write(json);

                    // Si no es el último documento, agrega una coma
                    if (i < documents.size() - 1) {
                        fileWriter.write(",");
                    }
                }

                fileWriter.write("]");
            }

        } catch (Exception e) {
            showFailedBackup();
            return;
        }

        try {
            // Obtiene la ruta del script desde el archivo JAR
            String scriptPathInJar = PathConstants.SCRIPT_BACKUP.getPath();
            InputStream scriptInputStream = getClass().getResourceAsStream(scriptPathInJar);

            // Crea un archivo temporal para almacenar el script
            File tempScriptFile = File.createTempFile("backup", ".ps1");
            tempScriptFile.deleteOnExit();

            // Copia el contenido del script desde el recurso dentro del archivo JAR al archivo temporal
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempScriptFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = scriptInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

            // Obtiene la ruta del archivo temporal
            String scriptPath = tempScriptFile.getAbsolutePath();

            String command = "powershell.exe -ExecutionPolicy Bypass -File " + scriptPath;
            Process process = Runtime.getRuntime().exec(command);

            // Capturar la salida estándar y la de error
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String outputLine;
            StringBuilder gitOutput = new StringBuilder();

            // Leer y mostrar la salida estándar
            while ((outputLine = stdInput.readLine()) != null) {
                System.out.println(outputLine);
                gitOutput.append(outputLine).append("\n");
            }

            // Leer y mostrar la salida de error
            while ((outputLine = stdError.readLine()) != null) {
                System.err.println(outputLine);
                gitOutput.append(outputLine).append("\n");
            }

            // Esperar a que termine el proceso
            int exitCode = process.waitFor();

            // Analizar la salida de Git (gitOutput) para buscar cualquier mensaje de error adicional específico del comando Git
            if (gitOutput.toString().contains("fatal") || gitOutput.toString().contains("error")) {
                logger.error("Backup failed");
                showFailedBackup();
                return;
            }

            logger.info("Backup correctly done");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            // Heredar el ícono de la ventana principal
            Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().addAll(RoutesManager.getMainStage().getIcons());

            alert.setTitle("Backup");
            alert.setHeaderText("Backup ✅");
            alert.setContentText("Backup realizado correctamente");
            alert.showAndWait();

        } catch (Exception e) {
            logger.info("Backup fallido");
            showFailedBackup();
        }
    }

    private void showFailedBackup() {
        logger.error("Backup failed");

        Alert alert = new Alert(ERROR);

        // Heredar el ícono de la ventana principal
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().addAll(RoutesManager.getMainStage().getIcons());

        alert.setTitle("Backup");
        alert.setHeaderText("Backup");
        alert.setContentText("Error al realizar el backup");
        alert.showAndWait();
    }

    private void onUpdateMenuAction() throws IOException {
        logger.info("Initializing Update View");
        RoutesManager routesManager = new RoutesManager();
        routesManager.initUpdateView(this);
    }

    private List<UserDTO> listUsers(List<UserDTO> filterTopUsersReliable) {
        // Asigna al nombre de los usuarios su posición en el ranking
        for (int i = 0; i < filterTopUsersReliable.size(); i++) {
            UserDTO user = filterTopUsersReliable.get(i);
            if (i == 0) {
                user.setUsername("  🥇 " + user.getUsername());
            } else if (i == 1) {
                user.setUsername("  \uD83E\uDD48 " + user.getUsername());
            } else if (i == 2) {
                user.setUsername("  \uD83E\uDD49  " + user.getUsername());
            } else {
                user.setUsername("   " + (i + 1) + ".  " + user.getUsername());
            }
        }
        return filterTopUsersReliable;
    }

    public List<UserDTO> filterStartsAndRakingUsersReliable(List<UserDTO> usersToFilter) {
        return usersToFilter.stream()
                .filter(user -> user.getPercentReliable() >= DECISION_STRICT_FILTER_RELIABLE)
                .filter(user -> user.getTotalBets() >= GlobalStats.medianTotalBets)
                .limit(NUM_USERS_TO_SHOW_RANKING)
                .collect(Collectors.toList());
    }

    private void setColorsTable() {
        tableUsers.setRowFactory(tv -> new TableRow<>() {

            @Override
            protected void updateItem(UserDTO item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null) {
                    setStyle("");
                } else {
                    if (item.getTotalBets() < item.getAverageBets()) {
                        setStyle("-fx-background-color: #ff6161;");
                    } else if (item.getTotalBets() / 2  > item.getAverageBets()) {
                        setStyle("-fx-background-color: #53db78;");
                    }else if (item.getTotalBets() >= item.getAverageBets()) {
                        setStyle("-fx-background-color: orange;");
                    } else {
                        setStyle("-fx-background-color: #ffffff;");
                    }

                    // Filtro especial para los verdes que fallen la media
                    /*if (item.getPercentReliable() > GlobalStats.goodAverageAllUsersSuccessRate && item.getTotalBets() < GlobalStats.medianTotalBets) {
                        setStyle("-fx-background-color: orange;");
                    }*/
                }
            }
        });

        tableUsersRanking.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(UserDTO item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: #ffffff;");

                if (item != null) {
                    if (getIndex() == 0) {
                        setStyle("-fx-background-color: #efb810;");
                    } else if (getIndex() == 1) {
                        setStyle("-fx-background-color: #c9c9c9;");
                    } else if (getIndex() == 2) {
                        setStyle("-fx-background-color: #b38f34;");
                    }
                }
            }
        });
    }

    private void centerAndFontTextTable() {
        for (int i = 0; i < tableUsers.getColumns().size(); i++) {
            tableUsers.getColumns().get(i).setStyle("-fx-alignment: CENTER; -fx-font-family: 'Segoe UI Emoji'; -fx-font-size: 14px;");
        }
        tableUsers.getColumns().get(0).setStyle("-fx-alignment: CENTER; -fx-font-family: 'Segoe UI Emoji'; -fx-font-size: 15px; ");

        tableUsersRanking.getColumns().get(0).setStyle("-fx-font-family: 'Noto Color Emoji'; -fx-font-size: 14px; ");
        tableUsersRanking.getColumns().get(1).setStyle("-fx-alignment: CENTER;");
        tableUsersRanking.getColumns().get(2).setStyle("-fx-alignment: CENTER;");
    }

    private List<String> filterSuggestionsList(String input) {
        List<String> allUsernames = userViewModel.getAllUsernamesNoRepeat();
        List<String> filteredSuggestions = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            return filteredSuggestions;
        }

        // 1. REGEX
        // Crear una expresión regular para coincidir con el inicio del nombre de usuario
        String regex = "^" + input.toLowerCase() + ".*";
        for (String suggestion : allUsernames) {
            if (suggestion.toLowerCase().matches(regex)) {
                filteredSuggestions.add(suggestion);
            }
        }

        // 2. CONTAINS
    /*  for (String suggestion : allUsernames) {
            if (suggestion.toLowerCase().contains(input.toLowerCase())) {
                filteredSuggestions.add(suggestion);
            }
        }*/

        // Ordenar alfabéticamente las sugerencias
        filteredSuggestions.sort(String::compareToIgnoreCase);

        //suggestionListView.getItems().setAll(filteredSuggestions);
        return filteredSuggestions;
    }

    public void updateMainTable() {
        String newUsername = textSearchUserFilter.getText().toUpperCase();
        Integer newDateOfWeek = getNewDateOfWeek();
        String newTime;

        if (radioButtonHideTime.isSelected()) {
            newTime = noDataTime;
            comboTimeFilter.setDisable(true);
        } else {
            newTime = comboTimeFilter.getSelectionModel().getSelectedItem();
            comboTimeFilter.setDisable(false);
        }

        setGlobalResult(newTime, newDateOfWeek);

        Boolean onFilterByDate = onFilterDataTableByDate(newUsername, newTime, newDateOfWeek);
        Boolean onFilterByTime = onFilterDataTableByTime(newUsername, newTime, newDateOfWeek);
        Boolean onFilterByDateTime = onFilterDataTableByDateTime(newUsername, newTime, newDateOfWeek);
        Boolean onFilterByUserDate = onFilterDataTableByUserDate(newUsername, newTime, newDateOfWeek);
        Boolean onFilterByUserTime = onFilterDataTableByUserTime(newUsername, newTime, newDateOfWeek);
        Boolean onFilterByUserDateTime = onFilterDataTableByUserDateTime(newUsername, newTime, newDateOfWeek);

        orderByTotalSuccessBets(tableUsers);

        if (!onFilterByDate && !onFilterByTime && !onFilterByDateTime && !onFilterByUserDate && !onFilterByUserTime && !onFilterByUserDateTime) {
            tableUsers.getItems().clear();
        }

        List<UserDTO> filteredUsers = filterStartsAndRakingUsersReliable(tableUsers.getItems());
        //setStarTopUsers(filteredUsers);
        //updatePieChart(filteredUsers);

        if (starCheckBox.isSelected()) {
            tableUsers.setItems(FXCollections.observableArrayList(filteredUsers));
        } else {
            tableUsers.setItems(FXCollections.observableArrayList(tableUsers.getItems()));
        }
        
        setColorsTable();
    }

    public void setStarTopUsers(List<UserDTO> filteredUsers) {
        for (int i = filteredUsers.size() - 1; i >= 0; i--) {
            tableUsers.getItems().remove(filteredUsers.get(i));
            filteredUsers.get(i).setUsername("⭐ " + filteredUsers.get(i).getUsername());
            tableUsers.getItems().add(i, filteredUsers.get(i));
        }
    }

    public void orderByTotalSuccessBets(TableView<UserDTO> tableToSort) {
        // Ordenamos por el que tenga más aciertos de cómputo, ya que hemos eliminado aquellos que tienen muchos fallos previamente
        Comparator<UserDTO> customComparatorRanking = (user1, user2) -> {
            // Total de aciertos
            double totalSuccessUser1 = user1.getTotalBets() * user1.getPercentReliable() / 100;
            double totalSuccessUser2 = user2.getTotalBets() * user2.getPercentReliable() / 100;

            if (totalSuccessUser1 == totalSuccessUser2) {
                // Si el total de aciertos son iguales, ordénalos por percentSuccess
                return Double.compare(user2.getPercentReliable(), user1.getPercentReliable());
            } else {
                // Si los totalBets no son iguales, ordénalos por total de aciertos
                return Double.compare(totalSuccessUser2, totalSuccessUser1);
            }
        };
        tableToSort.getItems().sort(customComparatorRanking); // Aplicamos el comparador personalizado
    }

    public Integer getNewDateOfWeek() {
        Integer newDateOfWeek = null;

        if (radioButtonMonday.isSelected()) {
            newDateOfWeek = 1;
        }
        if (radioButtonTuesday.isSelected()) {
            newDateOfWeek = 2;
        }
        if (radioButtonWednesday.isSelected()) {
            newDateOfWeek = 3;
        }
        if (radioButtonThursday.isSelected()) {
            newDateOfWeek = 4;
        }
        if (radioButtonFriday.isSelected()) {
            newDateOfWeek = 5;
        }
        if (radioButtonSaturday.isSelected()) {
            newDateOfWeek = 6;
        }
        if (radioButtonSunday.isSelected()) {
            newDateOfWeek = 7;
        }
        return newDateOfWeek;
    }

    private void setGlobalResult(String newTime, Integer newDateOfWeek) {
        logger.info("Setting global result");

        setGlobalTime(newTime, noDataTime);
        setGlobalDate(newDateOfWeek);

        // Recoger todas las apuestas y hacer la media con hora y día de la semana
        if (!newTime.equals(noDataTime) && newDateOfWeek != null) {
            Integer totalBets = userViewModel.getGlobalTotalBetsByDateTime(newTime, newDateOfWeek);
            double percentSuccess = userViewModel.getGlobalPercentSuccessByDateTime(newTime, newDateOfWeek);
            setDetailsGlobalResult(totalBets, percentSuccess);
        } else if (!newTime.equals(noDataTime)) {
            // Si no solo con la hora
            Integer totalBets = userViewModel.getGlobalTotalBetsByTime(newTime);
            double percentSuccess = userViewModel.getGlobalPercentSuccessByTime(newTime);
            setDetailsGlobalResult(totalBets, percentSuccess);
        } else if (newDateOfWeek != null) {
            // Solo con la fecha (todos los datos)
            Integer totalBets = userViewModel.getGlobalTotalBetsByDate(newDateOfWeek);
            double percentSuccess = userViewModel.getGlobalPercentSuccessByDate(newDateOfWeek);
            setDetailsGlobalResult(totalBets, percentSuccess);
        } else {
            // Si no hay ningún filtro
            setDetailsGlobalResult(0, 0.0);
        }
    }

    private void setDetailsGlobalResult(Integer totalBets, double percentSuccess) {
        if (percentSuccess == 0.0) {
            textFinalResultPercentSuccess.setText("sin datos");
            textFinalResultPercentSuccess.setTextFill(Color.WHITE);
        }

        if (totalBets == 0) {
            textFinalResultTotalBets.setText("sin datos");
            textFinalResultTotalBets.setTextFill(Color.WHITE);
            return;
        }
        textFinalResultPercentSuccess.setText(percentSuccess + "%");
        if (percentSuccess <= GlobalStats.badAverageAllUsersSuccessRate) {
            textFinalResultPercentSuccess.setTextFill(Color.RED);
        } else if (percentSuccess <= GlobalStats.goodAverageAllUsersSuccessRate) {
            textFinalResultPercentSuccess.setTextFill(Color.ORANGE);
        } else if (percentSuccess > GlobalStats.goodAverageAllUsersSuccessRate) {
            textFinalResultPercentSuccess.setTextFill(Color.GREEN);
        } else {
            textFinalResultPercentSuccess.setTextFill(Color.WHITE);
        }
        textFinalResultTotalBets.setText(String.valueOf(totalBets));
    }

    private void setGlobalDate(Integer newDateOfWeek) {
        if (newDateOfWeek == null) {
            textFinalResultDate.setText("sin filtro");
        } else {
            Map<Integer, String> dayOfWeekMap = new HashMap<>();
            dayOfWeekMap.put(1, "Lunes");
            dayOfWeekMap.put(2, "Martes");
            dayOfWeekMap.put(3, "Miércoles");
            dayOfWeekMap.put(4, "Jueves");
            dayOfWeekMap.put(5, "Viernes");
            dayOfWeekMap.put(6, "Sábado");
            dayOfWeekMap.put(7, "Domingo");

            if (newDateOfWeek == 1) {
                textFinalResultDate.setText("Lunes");
            }
            if (newDateOfWeek == 2) {
                textFinalResultDate.setText("Martes");
            }
            if (newDateOfWeek == 3) {
                textFinalResultDate.setText("Miércoles");
            }
            if (newDateOfWeek == 4) {
                textFinalResultDate.setText("Jueves");
            }
            if (newDateOfWeek == 5) {
                textFinalResultDate.setText("Viernes");
            }
            if (newDateOfWeek == 6) {
                textFinalResultDate.setText("Sábado");
            }
            if (newDateOfWeek == 7) {
                textFinalResultDate.setText("Domingo");
            }
        }
        textFinalResultDate.setTextFill(Color.WHITE);
    }

    private void onLeyendaMenuAction() throws IOException {
        logger.info("Initializing leyenda view");
        RoutesManager routesManager = new RoutesManager();
        routesManager.initLeyendaView();
    }

    private void onDataGestorMenuAction() {
        logger.info("Initializing data gestor view");
        RoutesManager routesManager = new RoutesManager();
        try {
            routesManager.intiDataGestorView(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setGlobalTime(String newTime, String noDataTime) {
        if (newTime.equals(noDataTime)) {
            textFinalResultTime.setText("sin filtro");
        } else {
            textFinalResultTime.setText(newTime);
        }
        textFinalResultTime.setTextFill(Color.WHITE);
    }

    private void extractedUserByRadioButtonFilter(List<UserDTO> usersToShow) {
        if (radioButtonHideRed.isSelected()) {
            usersToShow.removeIf(user -> user.getTotalBets() < user.getAverageBets());
        }

        if (radioButtonHideGreen.isSelected()) {
            usersToShow.removeIf(user -> user.getTotalBets() / 2  > user.getAverageBets());
        }

        if (radioButtonHideOrange.isSelected()) {
            usersToShow.removeIf(user ->  user.getTotalBets() >= user.getAverageBets() && user.getTotalBets() / 2  <= user.getAverageBets() );
        }
    }

    private Boolean onFilterDataTableByDate(String newUsername, String newTime, Integer newDateOfWeek) {
        if (newUsername == null || newUsername.isEmpty() && newTime.equals(noDataTime) && newDateOfWeek != null) {
            logger.info("Filtering all by date");
            List<UserDTO> usersToShow = userViewModel.getAllByDate(newDateOfWeek);

            extractedUserByRadioButtonFilter(usersToShow);

            tableUsers.getItems().clear();
            tableUsers.setItems(FXCollections.observableArrayList(usersToShow));

            return true;
        }
        return false;
    }

    private Boolean onFilterDataTableByTime(String newUsername, String newTime, Integer newDate) {
        if (newUsername == null || newUsername.isEmpty() && !newTime.equals(noDataTime) && newDate == null) {
            logger.info("Filtering all by time");
            List<UserDTO> usersToShow = userViewModel.getAllByTime(newTime);

            extractedUserByRadioButtonFilter(usersToShow);

            tableUsers.getItems().clear();
            tableUsers.setItems(FXCollections.observableArrayList(usersToShow));
            return true;
        }
        return false;
    }

    private Boolean onFilterDataTableByDateTime(String newUsername, String newTime, Integer newDate) {
        if (newUsername == null || newUsername.isEmpty() && !newTime.equals(noDataTime) && newDate != null) {
            logger.info("Filtering all by date time");

            List<UserDTO> usersToShow = userViewModel.getAllByDateTime(newTime, newDate);

            extractedUserByRadioButtonFilter(usersToShow);

            tableUsers.getItems().clear();
            tableUsers.setItems(FXCollections.observableArrayList(usersToShow));
            return true;
        }
        return false;
    }

    private List<UserDTO> filterUsersByPartialUsername(List<UserDTO> users, String newUsername) {
        List<UserDTO> filteredUsers = new ArrayList<>();

        String regex = "^" + newUsername + ".*";

        for (UserDTO user : users) {
            if (user.getUsername().toUpperCase().matches(regex) || user.getUsername().equalsIgnoreCase(newUsername)) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }

    private Boolean onFilterDataTableByUserDate(String newUsername, String newTime, Integer newDate) {
        if (newUsername != null && !newUsername.isEmpty() && newTime.equals(noDataTime) && newDate != null) {
            logger.info("Filtering all by user & date");
            List<UserDTO> usersToShow = userViewModel.getAllByDate(newDate);

            extractedUserByRadioButtonFilter(usersToShow);

            // Filtrar la lista por los primeros caracteres del nombre de usuario
            usersToShow = filterUsersByPartialUsername(usersToShow, newUsername);

            tableUsers.getItems().clear();
            tableUsers.setItems(FXCollections.observableArrayList(usersToShow));

            return true;

        }
        return false;
    }

    private Boolean onFilterDataTableByUserTime(String newUsername, String newTime, Integer newDate) {
        if (newUsername != null && !newUsername.isEmpty() && !newTime.equals(noDataTime) && newDate == null) {
            logger.info("Filtering by user & time");

            List<UserDTO> usersToShow = userViewModel.getAllByTime(newTime);

            extractedUserByRadioButtonFilter(usersToShow);

            // Filtrar la lista por los primeros caracteres del nombre de usuario
            usersToShow = filterUsersByPartialUsername(usersToShow, newUsername);

            tableUsers.getItems().clear();
            tableUsers.setItems(FXCollections.observableArrayList(usersToShow));
            return true;
        }
        return false;
    }

    private Boolean onFilterDataTableByUserDateTime(String newUsername, String newTime, Integer newDate) {
        if (newUsername != null && !newUsername.isEmpty() && !newTime.equals(noDataTime) && newDate != null) {
            logger.info("Filtering by user & date time");

            List<UserDTO> usersToShow = userViewModel.getAllByDateTime(newTime, newDate);

            extractedUserByRadioButtonFilter(usersToShow);

            // Filtrar la lista por los primeros caracteres del nombre de usuario
            usersToShow = filterUsersByPartialUsername(usersToShow, newUsername);

            tableUsers.getItems().clear();
            tableUsers.setItems(FXCollections.observableArrayList(usersToShow));
            return true;
        }
        return false;
    }

    private void saveUser() {
        String titleError = "Error al guardar";
        String infoError = "Error saving user";

        // Validate User
        String userName = (textFieldUser.getText() != null && !textFieldUser.getText().isEmpty()) ? textFieldUser.getText() : null;
        LocalDate date = (datePicker.getValue() != null && !datePicker.getValue().toString().isEmpty()) ? datePicker.getValue() : null;
        String time = (comboTime.getValue() != null && !comboTime.getValue().isEmpty()) ? comboTime.getValue() : null;

        if (userName == null) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Nombre de usuario vacío");
            alert.setContentText("El nombre de usuario NO puede estar vacío");
            alert.showAndWait();
            return;
        }

        if (userName.contains("{") || userName.contains("}")) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Nombre de usuario incorrecto");
            alert.setContentText("El nombre de usuario NO puede contener los caracteres '{' o '}'");
            alert.showAndWait();
            return;
        }

        if (date == null) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Fecha incorrecta");
            alert.setContentText("La fecha es incorrecta, debe tener formato (yyyy-MM-dd)");
            alert.showAndWait();
            return;
        } else if (date.isAfter(LocalDate.now())) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Fecha incorrecta");
            alert.setContentText("La fecha NO puede ser más tarde del día presente");
            alert.showAndWait();
            return;
        }

        if (time == null || time.equals(noDataTime)) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Hora vacía");
            alert.setContentText("La hora NO puede estar vacía");
            alert.showAndWait();
            return;
        }

        // time es una string 00:01-01:00
        String[] parts = time.split("-");
        LocalTime inputTime = LocalTime.parse(parts[0]);
        LocalTime currentTime = LocalTime.now();

        if (inputTime.isAfter(currentTime) && (date.equals(LocalDate.now()))) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Hora incorrecta");
            alert.setContentText("La hora NO puede ser posterior a la actual de hoy");
            alert.showAndWait();
            return;
        }

        // En el momento uno esté seleccionado la condición no se cumple
        /*if (!radioButtonGood.isSelected() && !radioButtonBad.isSelected()) {
            logger.info(infoError);
            Alert alert = new Alert(ERROR);
            alert.setTitle(titleError);
            alert.setHeaderText("Fiabilidad vacía");
            alert.setContentText("La fiabilidad NO puede estar vacía");
            alert.showAndWait();
            return;
        }*/

        // User is valid, continue saving the user
        logger.info("Saving user");

        userViewModel.saveUser(new UserEntity(textFieldUser.getText().trim().toUpperCase(), datePicker.getValue(), comboTime.getValue(), true, 1));

        // Clean fields
        datePicker.setValue(null);
        comboTime.setValue(null);
        //radioButtonGood.setSelected(false);
        //radioButtonBad.setSelected(false);

        userViewModel.refreshData(userViewModel.getAll());
        updateMainTable();
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    public Integer getIndexComboTimeFilter() {
        return comboTimeFilter.getSelectionModel().getSelectedIndex();
    }

    public RadioButton getRadioButtonHideTime() {
        return radioButtonHideTime;
    }

    public CheckBox getStarCheckBox() {
        return starCheckBox;
    }

    public void clearTable() {
        tableUsers.getItems().clear();
        tableUsers.setItems(FXCollections.observableArrayList());
    }
}
