module com.example.kayswheelsrental {
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires org.kordamp.ikonli.javafx;

    opens com.example.kayswheelsrental to javafx.fxml;
    exports com.example.kayswheelsrental;
}