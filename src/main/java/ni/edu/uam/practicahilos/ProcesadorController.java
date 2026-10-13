package ni.edu.uam.practicahilos;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.util.Locale;

public class ProcesadorController {

    @FXML
    private Label estadoClientesLabel;
    @FXML
    private ProgressBar progresoClientesBar;
    @FXML
    private Label porcentajeClientesLabel;
    @FXML
    private Label contadorClientesLabel;
    @FXML
    private Button iniciarClientesButton;
    @FXML
    private Button detenerClientesButton;
    @FXML
    private Button reiniciarClientesButton;

    @FXML
    private Label estadoProductosLabel;
    @FXML
    private ProgressBar progresoProductosBar;
    @FXML
    private Label porcentajeProductosLabel;
    @FXML
    private Label contadorProductosLabel;
    @FXML
    private Button iniciarProductosButton;
    @FXML
    private Button detenerProductosButton;
    @FXML
    private Button reiniciarProductosButton;

    private ProcesoSimulado clientes;
    private ProcesoSimulado productos;

    @FXML
    private void initialize() {
        clientes = new ProcesoSimulado(
                "Clientes", 100, 100,
                estadoClientesLabel, progresoClientesBar, porcentajeClientesLabel, contadorClientesLabel,
                iniciarClientesButton, detenerClientesButton, reiniciarClientesButton
        );
        productos = new ProcesoSimulado(
                "Productos", 50, 150,
                estadoProductosLabel, progresoProductosBar, porcentajeProductosLabel, contadorProductosLabel,
                iniciarProductosButton, detenerProductosButton, reiniciarProductosButton
        );

        clientes.reiniciar();
        productos.reiniciar();
    }

    @FXML
    private void iniciarClientes() {
        clientes.iniciar();
    }

    @FXML
    private void detenerClientes() {
        clientes.detener();
    }

    @FXML
    private void reiniciarClientes() {
        clientes.reiniciar();
    }

    @FXML
    private void iniciarProductos() {
        productos.iniciar();
    }

    @FXML
    private void detenerProductos() {
        productos.detener();
    }

    @FXML
    private void reiniciarProductos() {
        productos.reiniciar();
    }

    public void solicitarCierre() {
        if (clientes != null) {
            clientes.solicitarCierre();
        }
        if (productos != null) {
            productos.solicitarCierre();
        }
    }

    private static final class ProcesoSimulado {
        private final String nombre;
        private final int total;
        private final int pausaMilisegundos;
        private final Label estadoLabel;
        private final ProgressBar progresoBar;
        private final Label porcentajeLabel;
        private final Label contadorLabel;
        private final Button iniciarButton;
        private final Button detenerButton;
        private final Button reiniciarButton;

        private Thread trabajador;
        private long ejecucionId;
        private boolean cerrando;
        private int procesadosActuales;

        private ProcesoSimulado(
                String nombre,
                int total,
                int pausaMilisegundos,
                Label estadoLabel,
                ProgressBar progresoBar,
                Label porcentajeLabel,
                Label contadorLabel,
                Button iniciarButton,
                Button detenerButton,
                Button reiniciarButton
        ) {
            this.nombre = nombre;
            this.total = total;
            this.pausaMilisegundos = pausaMilisegundos;
            this.estadoLabel = estadoLabel;
            this.progresoBar = progresoBar;
            this.porcentajeLabel = porcentajeLabel;
            this.contadorLabel = contadorLabel;
            this.iniciarButton = iniciarButton;
            this.detenerButton = detenerButton;
            this.reiniciarButton = reiniciarButton;
        }

        private void iniciar() {
            if (estaActivo()) {
                return;
            }

            cerrando = false;
            if (procesadosActuales >= total) {
                procesadosActuales = 0;
                actualizarProgreso(procesadosActuales);
            }
            long idActual = ++ejecucionId;
            estadoLabel.setText("Procesando registros simulados...");
            configurarBotones(true);

            // Runnable: contiene el trabajo pesado y las esperas fuera del hilo de JavaFX.
            Runnable trabajo = () -> ejecutarProceso(idActual);

            // Cada ejecución usa un Thread nuevo; un Thread terminado no puede reiniciarse.
            trabajador = new Thread(trabajo, "Proceso-" + nombre + "-" + idActual);
            trabajador.setDaemon(true);
            trabajador.start();
        }

        private void ejecutarProceso(long idActual) {
            boolean detenido = false;
            int procesados = procesadosActuales;

            try {
                for (int registro = procesados + 1; registro <= total; registro++) {
                    if (Thread.currentThread().isInterrupted()) {
                        detenido = true;
                        break;
                    }

                    Thread.sleep(pausaMilisegundos);
                    procesados = registro;
                    int avance = procesados;

                    // Platform.runLater evita modificar controles JavaFX desde el trabajador.
                    Platform.runLater(() -> {
                        if (esActual(idActual)) {
                            actualizarProgreso(avance);
                            estadoLabel.setText("Procesando registros simulados...");
                        }
                    });
                }
            } catch (InterruptedException exception) {
                detenido = true;
                Thread.currentThread().interrupt();
            } catch (RuntimeException exception) {
                detenido = true;
                enviarFinalizacion(idActual, "Error inesperado: " + exception.getMessage(), procesados);
                return;
            }

            if (Thread.currentThread().isInterrupted() && procesados < total) {
                detenido = true;
            }

            if (!detenido) {
                procesados = total;
            }

            String estadoFinal = detenido ? "Proceso detenido" : "Proceso finalizado";
            enviarFinalizacion(idActual, estadoFinal, procesados);
        }

        private void detener() {
            if (!estaActivo()) {
                return;
            }

            estadoLabel.setText("Solicitando detención...");
            trabajador.interrupt();
        }

        private void reiniciar() {
            if (estaActivo()) {
                return;
            }

            ejecucionId++;
            procesadosActuales = 0;
            actualizarProgreso(0);
            estadoLabel.setText("Esperando inicio");
            configurarBotones(false);
        }

        private void solicitarCierre() {
            cerrando = true;
            ejecucionId++;
            if (estaActivo()) {
                trabajador.interrupt();
            }
        }

        private void enviarFinalizacion(long idActual, String estadoFinal, int procesados) {
            Platform.runLater(() -> {
                if (!cerrando && esActual(idActual)) {
                    actualizarProgreso(procesados);
                    estadoLabel.setText(estadoFinal);
                    configurarBotones(false);
                }
            });
        }

        private boolean esActual(long idActual) {
            return idActual == ejecucionId && !cerrando;
        }

        private boolean estaActivo() {
            return trabajador != null && trabajador.isAlive();
        }

        private void actualizarProgreso(int procesados) {
            procesadosActuales = procesados;
            double progreso = procesados / (double) total;
            progresoBar.setProgress(progreso);
            porcentajeLabel.setText(String.format(Locale.US, "%.0f%%", progreso * 100));
            contadorLabel.setText(procesados + "/" + total);
        }

        private void configurarBotones(boolean procesando) {
            iniciarButton.setDisable(procesando);
            detenerButton.setDisable(!procesando);
            reiniciarButton.setDisable(procesando);
        }
    }
}
