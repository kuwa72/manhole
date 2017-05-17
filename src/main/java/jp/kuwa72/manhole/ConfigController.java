package jp.kuwa72.manhole;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import jp.kuwa72.manhole.bean.Config;

import java.io.IOException;

public class ConfigController {

    private Config config;

    @FXML
    private TextField idField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField apikeyField;
    @FXML
    private TextField urlField;

    @FXML public void initialize() {
        String userProfile = System.getProperty("user.home");

        ConfigurationStore configurationStore = new ConfigurationStore();

        Config config = configurationStore.load();

        idField.setText(config.userid);
        passwordField.setText(config.password);
        apikeyField.setText(config.apikey);
        urlField.setText(config.url);
    }

    public void quit() {
        String userProfile = System.getProperty("user.home");


        ConfigurationStore configurationStore = new ConfigurationStore();

        Config config = configurationStore.load(); //default instance

        config.userid = idField.getText();
        config.password = passwordField.getText();
        config.apikey = apikeyField.getText();
        config.url = urlField.getText();

        try {
            configurationStore.save(config);
        } catch (IOException e) {
            e.printStackTrace();
            (new Alert(Alert.AlertType.ERROR, e.getMessage())).showAndWait()
                    .filter(response -> response == ButtonType.OK);
        }
    }
}
