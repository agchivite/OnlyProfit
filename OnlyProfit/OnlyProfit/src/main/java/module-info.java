module dev.sbytmacke.onlyprofit {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.slf4j;
    requires org.mongodb.driver.core;
    requires com.google.gson;

    opens dev.sbytmacke.onlyprofit to javafx.fxml;
    exports dev.sbytmacke.onlyprofit;

    opens dev.sbytmacke.onlyprofit.controllers to javafx.fxml;
    exports dev.sbytmacke.onlyprofit.controllers;

    opens dev.sbytmacke.onlyprofit.dto to javafx.base;
    exports dev.sbytmacke.onlyprofit.dto;

    opens dev.sbytmacke.onlyprofit.models to javafx.base;
    exports dev.sbytmacke.onlyprofit.models;
}
