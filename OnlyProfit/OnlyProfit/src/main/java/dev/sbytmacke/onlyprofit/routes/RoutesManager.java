package dev.sbytmacke.onlyprofit.routes;

import dev.sbytmacke.onlyprofit.AppMain;
import dev.sbytmacke.onlyprofit.constants.AppConstants;
import dev.sbytmacke.onlyprofit.constants.PathConstants;
import dev.sbytmacke.onlyprofit.controllers.*;
import dev.sbytmacke.onlyprofit.dto.UserDTO;
import dev.sbytmacke.onlyprofit.repositories.UserRepositoryImpl;
import dev.sbytmacke.onlyprofit.services.database.DatabaseManagerImpl;
import dev.sbytmacke.onlyprofit.viewmodel.UserViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class RoutesManager {

    private static Stage _mainStage;
    private static Stage _activeStage;
    private static Scene _activeScene;
    private final String pathMainPng = PathConstants.MAIN_ICON.getPath();
    Logger logger = LoggerFactory.getLogger(getClass());
    UserViewModel userViewModel = new UserViewModel(new UserRepositoryImpl(new DatabaseManagerImpl())); // Acoplamiento

    public static Stage getMainStage() {
        return _mainStage;
    }

    public void initUserDetailModal(UserDTO selectedItem) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("user-detail-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        UserDetailController controller = fxmlLoader.getController();
        controller.init(userViewModel, selectedItem);

        // Crear y mostrar la escena modal
        Stage modalStage = new Stage();
        modalStage.setTitle("Detalle de usuario");
        modalStage.setResizable(true);
        modalStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        modalStage.setScene(scene);
        modalStage.initOwner(_mainStage);
        modalStage.initModality(Modality.NONE); // Para poder interactuar con la pantalla principal
        modalStage.show();
    }

    public void initMainView(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();

        MainViewController controller = fxmlLoader.getController(); // Obtenemos el controlador
        controller.init(userViewModel); // Inyección de dependencias desde el DatabaseManager hasta el Controller

        Scene scene = new Scene(root, 1500, Control.USE_COMPUTED_SIZE);
        stage.setResizable(true);
        stage.setTitle(AppConstants.TITLE.getString());
        // Agregar un icono a la ventana
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        _mainStage = stage;
        _activeStage = stage;
        _activeScene = scene;

        stage.setScene(scene);

        // Maximizar la ventana al tamaño de la pantalla
        stage.setMaximized(true);

        stage.show();
    }

    public void intiDataGestorView(MainViewController mainViewController) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("data-gestor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        DataGestorViewController controller = fxmlLoader.getController(); // Obtenemos el controlador
        controller.setMainController(mainViewController); // Para poder acceder a la función de actualizar las tablas
        controller.init(userViewModel); // Inyección de dependencias desde el DatabaseManager hasta el Controller

        Stage stage = new Stage();
        stage.setTitle("Visualizador de datos");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        stage.setScene(scene);
        stage.initOwner(_mainStage);
        stage.initModality(Modality.NONE); // Para poder interactuar con la pantalla principal

        stage.show();
    }

    public void initLeyendaView() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("leyenda-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = new Stage();
        stage.setTitle("Leyenda");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        stage.setScene(scene);
        stage.initOwner(_mainStage);
        stage.initModality(Modality.NONE); // Para poder interactuar con la pantalla principal
        stage.show();
    }

    public void initMainMiniView(MainViewController mainViewController) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("main-mini-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        MainMiniViewController controller = fxmlLoader.getController(); // Obtenemos el controlador
        controller.init(mainViewController);

        Stage stage = new Stage();
        stage.setTitle("Mini-view");
        stage.setResizable(true);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        // EventHandler para el evento de cierre de la ventana
        stage.setOnCloseRequest(event -> {
            // Limpiamos la tabla main-view
            mainViewController.clearTable();
            mainViewController.updateMainTable();
            _mainStage.show();
        });

        stage.setScene(scene);
        stage.initOwner(_mainStage);

        _mainStage.hide();
        stage.show();
    }

    public void initUpdateView(MainViewController mainViewController) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("update-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        UpdateViewController controller = fxmlLoader.getController();
        controller.init(mainViewController);

        Stage stage = new Stage();
        stage.setTitle("Actualizar datos");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        stage.setScene(scene);
        stage.initOwner(_mainStage);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.show();
    }

    public void initChartsView(MainViewController mainViewController) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(AppMain.class.getResource("charts-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        ChartsViewController controller = fxmlLoader.getController();
        controller.init(mainViewController);

        Stage stage = new Stage();
        stage.setTitle("Ranking");
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource(pathMainPng)).toExternalForm()));

        stage.setScene(scene);
        stage.initOwner(_mainStage);
        stage.initModality(Modality.NONE);

        stage.show();
    }

    public Stage getActiveStage() {
        return _activeStage;
    }
}
