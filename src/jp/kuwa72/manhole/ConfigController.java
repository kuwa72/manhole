package jp.kuwa72.manhole;

import com.google.common.io.Files;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import jp.kuwa72.manhole.bean.Config;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigController {

    private Config config;

    @FXML
    private TextField idField;

    @FXML
    private TextField passwordField;

    @FXML public void initialize() {
        String userProfile = System.getProperty("user.home");

        Gson gson = new Gson();
        try {
            config = gson.fromJson(new FileReader(FilenameUtils.concat(userProfile, "manhole.json")), Config.class);
        } catch (FileNotFoundException e) {
            config = new Config();
            e.printStackTrace();
            return;
        }

        idField.setText(config.id);
        passwordField.setText(config.password);
    }

    public void quit() {
        String userProfile = System.getProperty("user.home");
        config.id = idField.getText();
        config.password = passwordField.getText();

        Gson gson = new Gson();
        String json = gson.toJson(config);
        try {
            Files.write(json.getBytes(), new File(FilenameUtils.concat(userProfile, "manhole.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
