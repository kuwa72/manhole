package jp.kuwa72.manhole;

import com.google.common.io.Files;
import com.google.gson.Gson;
import jp.kuwa72.manhole.bean.Config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationStore {

    public String filename = "config.properties";

    public Config load() {

        Gson gson = new Gson();
        Config config = new Config();

        try {
            config = gson.fromJson(new FileReader(filename), Config.class);
        } catch (Exception e) {
            //XXX ???
            e.printStackTrace();
        }

        return config;
    }

    public void save(Config config) throws IOException {
        Gson gson = new Gson();
        String json = gson.toJson(config);
        Files.write(json.getBytes(), new File(filename));
    }
}
