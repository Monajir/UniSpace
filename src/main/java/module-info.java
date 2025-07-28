module lab.visual.unispace {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens lab.visual.unispace to javafx.fxml;
    exports lab.visual.unispace;

    exports lab.visual.unispace.controllers;
    opens lab.visual.unispace.controllers to javafx.fxml;
}