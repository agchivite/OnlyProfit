module dev.sbytmacke.javafx {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.sbytmacke.javafx to javafx.fxml;
    exports dev.sbytmacke.javafx;
}