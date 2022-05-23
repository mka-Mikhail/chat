module mka.chat.client {
    requires javafx.controls;
    requires javafx.fxml;

    opens mka.chat.client to javafx.fxml;
    exports mka.chat.client;
}