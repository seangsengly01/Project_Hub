module org.example.to_do_list_management {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;
    requires org.apache.poi.ooxml;

    opens org.example.project_hub to javafx.fxml;
    exports org.example.project_hub;
}