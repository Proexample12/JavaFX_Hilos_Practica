package ni.edu.uam.practicahilos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private ProcesadorController controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("procesador-view.fxml"));
        Scene scene = new Scene(loader.load(), 820, 560);

        controller = loader.getController();

        stage.setTitle("NicaData Solutions — Monitor de procesos");
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(500);
        stage.setOnCloseRequest(event -> limpiarTrabajadores());
        stage.show();
    }

    @Override
    public void stop() {
        limpiarTrabajadores();
    }

    private void limpiarTrabajadores() {
        if (controller != null) {
            controller.solicitarCierre();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
