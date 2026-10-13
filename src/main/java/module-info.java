module org.example.javafx_hilos_practica {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafx_hilos_practica to javafx.fxml;
    exports org.example.javafx_hilos_practica;
}