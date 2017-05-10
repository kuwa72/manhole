package jp.kuwa72.manhole;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jp.kuwa72.manhole.bean.Config;
import jp.kuwa72.manhole.bean.Item;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Controller {

    private List<Cookie> cookies;

    private Parent parent;

    private Config config;

    @FXML public void initialize() {
        loadConfig();
    }

    public void loadConfig() {
        this.config = (new ConfigurationStore()).load();
    }

    public void login() {
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setFollowRedirect(true).build());
        try {

            Future<Response> f = asyncHttpClient.prepareGet(config.url + "/login")
                    .execute();

            Response r = f.get();
            //System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            Document doc = Jsoup.parse(r.getResponseBody(Charset.forName("UTF-8")));

            Elements authKeys = doc.getElementsByAttributeValue("name", "authenticity_token");
            System.out.println(authKeys);
            if (authKeys.size() != 1) {
                return; //no token
            }

            r = asyncHttpClient
                    .preparePost(config.url + "/login")
                    .addFormParam("authenticity_token", authKeys.first().attr("value"))
                    .addFormParam("username", config.userid)
                    .addFormParam("password", config.password)
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

    public List<Item> collectItem(Element select) {
        return select.children().stream().map(element -> {
            Item i = new Item();
            i.id = element.attr("value");
            i.name = element.text();
            return i;
        }).collect(Collectors.toList());
    }

    public void loadItems() {
        login();
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient(new DefaultAsyncHttpClientConfig.Builder().setFollowRedirect(true).build());
        try {

            Future<Response> f = asyncHttpClient.prepareGet(config.url + "/projects/work_mng/issues/new")
                    .setCookies(cookies)
                    .execute();

            Response r = f.get();
            System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            Document doc = Jsoup.parse(r.getResponseBody(Charset.forName("UTF-8")));

            config.category = collectItem(doc.getElementById("issue_category_id"));
            config.system = collectItem(doc.getElementById("issue_custom_field_values_32"));
            config.project = collectItem(doc.getElementById("issue_custom_field_values_57"));
            config.payment = collectItem(doc.getElementById("issue_custom_field_values_58"));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    @FXML public void quit() {
        Platform.exit();
    }

    public void config(ActionEvent event) {
        Parent root = null;
        FXMLLoader loader = null;
        try {
            loader = new FXMLLoader(getClass().getResource("config.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            (new Alert(Alert.AlertType.ERROR, e.getMessage())).showAndWait();
            return;
        }
        Stage configStage = new Stage();
        configStage.setScene(new Scene(root));
        configStage.initModality(Modality.APPLICATION_MODAL);
        configStage.initOwner(((MenuItem)event.getSource()).getParentPopup().getScene().getWindow());

        FXMLLoader finalLoader = loader;
        configStage.setOnCloseRequest(event1 -> {
            ((ConfigController) finalLoader.getController()).quit();
            loadConfig();
            loadItems();
            try {
                (new ConfigurationStore()).save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        configStage.showAndWait();

    }

    public void setParent(Parent rootPane) {
        this.parent = rootPane;
    }
}
