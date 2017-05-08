package jp.kuwa72.manhole;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Controller {

    private Optional<JBrowserDriver> driver;

    private List<Cookie> cookies;

    private Parent parent;

    private String url;

    @FXML public void initialize() {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("config.properties"));

            url = prop.getProperty("url");
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void login() {
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        try {

            Future<Response> f = asyncHttpClient.prepareGet(url + "/login").execute();

            Response r = f.get();
            //System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            Document doc = Jsoup.parse(r.getResponseBody(Charset.forName("UTF-8")));

            Elements tags = doc.getElementsByTag("input");
            Elements authKeys = doc.getElementsByAttributeValue("name", "authenticity_token");
            if (authKeys.size() != 1) {
                return; //no token
            }

            r = asyncHttpClient
                    .preparePost(url + "/login")
                    .addFormParam("authenticity_token", authKeys.first().attr("value"))
                    .addFormParam("username", "")
                    .addFormParam("password", "")
                    .setCookies(r.getCookies())
                    .execute()
                    .get();

            System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            this.cookies = r.getCookies();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @FXML public void hasLoginSession() {

    }

    @FXML public void loadConfig() {
        Config conf = ConfigFactory.load();
    }

    @FXML public void quit() {
        System.exit(0);
    }

    public void config(ActionEvent event) {
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("config.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage configStage = new Stage();
        configStage.setScene(new Scene(root));
        configStage.initModality(Modality.APPLICATION_MODAL);
        configStage.initOwner(((MenuItem)event.getSource()).getParentPopup().getScene().getWindow());

        FXMLLoader finalLoader = loader;
        configStage.setOnCloseRequest(event1 -> {
            ((ConfigController) finalLoader.getController()).quit();
        });
        configStage.showAndWait();

    }

    public void setParent(Parent rootPane) {
        this.parent = rootPane;
    }
}
