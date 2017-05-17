package jp.kuwa72.manhole;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class Controller {

    private List<Cookie> cookies;

    private Parent parent;

    private Config config;

    @FXML public TextField subjectField;
    @FXML public ListView<String> subjectList;
    @FXML public ListView<Item> categoryList;
    @FXML public ListView<Item> systemList;
    @FXML public ListView<Item> projectList;
    @FXML public ListView<Item> paymentList;
    @FXML public Button button05;
    @FXML public Button button1;
    @FXML public Button button2;
    @FXML public Button button4;
    @FXML public DatePicker startDatePicker;



    @FXML public void initialize() {

        this.categoryList.setCellFactory(p -> new ItemListCell());
        this.systemList.setCellFactory(p -> new ItemListCell());
        this.projectList.setCellFactory(p -> new ItemListCell());
        this.paymentList.setCellFactory(p -> new ItemListCell());

        loadConfig();
        loadItems();
    }

    public void loadConfig() {
        this.config = (new ConfigurationStore()).load();
        this.subjectList.getItems().addAll(config.subjects);
    }

    public void login() {
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder().setFollowRedirect(true).build());
        try {

            Future<Response> f = asyncHttpClient.prepareGet(config.url + "/login")
                    .execute();

            Response r = f.get();
            //System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            Document doc = Jsoup.parse(r.getResponseBody(Charset.forName("UTF-8")));

            Elements authKeys = doc.getElementsByAttributeValue("name", "authenticity_token");

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

            //System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            this.cookies = r.getCookies();

        } catch (InterruptedException | ExecutionException e) {
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
        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient(
                new DefaultAsyncHttpClientConfig.Builder().setFollowRedirect(true).build());
        try {
            Future<Response> f = asyncHttpClient.prepareGet(config.url + "/projects/work_mng/issues/new")
                    .setCookies(cookies)
                    .execute();

            Response r = f.get();
            //System.out.println(r.getResponseBody(Charset.forName("UTF-8")));

            Document doc = Jsoup.parse(r.getResponseBody(Charset.forName("UTF-8")));

            config.category = collectItem(doc.getElementById("issue_category_id"));
            config.system = collectItem(doc.getElementById("issue_custom_field_values_32"));
            config.project = collectItem(doc.getElementById("issue_custom_field_values_57"));
            config.payment = collectItem(doc.getElementById("issue_custom_field_values_58"));

            this.categoryList.getItems().addAll(config.category);
            this.systemList.getItems().addAll(config.system);
            this.projectList.getItems().addAll(config.project);
            this.paymentList.getItems().addAll(config.payment);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    @FXML public void quit() throws IOException {
        config.subjects = subjectList.getItems();
        (new ConfigurationStore()).save(config);
        Platform.exit();
    }

    @FXML public void showConfigDialog(ActionEvent event) {
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

    public void inputSubject(ActionEvent event) throws IOException {
        subjectList.getItems().add(subjectField.getText());
        subjectField.clear();
        (new ConfigurationStore()).save(config);
    }

    public Issue collectIssue(int projectId, int autherId, String kousuu) {
        Issue issue = IssueFactory.create(projectId, subjectList.getSelectionModel().getSelectedItem());
        issue.setAuthorId(autherId); //XXX fixed
        issue.setCategory(IssueCategoryFactory.create(Integer.parseInt(categoryList.getSelectionModel().getSelectedItem().id)));
        issue.addCustomField(CustomFieldFactory.create(32, "システム分類", systemList.getSelectionModel().getSelectedItem().name));
        issue.addCustomField(CustomFieldFactory.create(52, "工数計", kousuu));
        issue.addCustomField(CustomFieldFactory.create(57, "顧客名／拡張PJ", projectList.getSelectionModel().getSelectedItem().name));
        issue.addCustomField(CustomFieldFactory.create(58, "請求", paymentList.getSelectionModel().getSelectedItem().name));

        issue.setStartDate(localDateTimeToDate(startDatePicker.getValue()));
        return issue;
    }

    public void createIssue(String kousuu) {
        RedmineManager rm = RedmineManagerFactory.createWithApiKey(config.url, config.apikey);
        IssueManager im = rm.getIssueManager();

        Issue issue = collectIssue(192,132, kousuu);

        try {
            Issue issue1 = im.createIssue(issue);
            System.out.println((new Gson()).toJson(issue1)); //XXX
        } catch (RedmineException e) {
            e.printStackTrace();
        }
    }
    public void post05(ActionEvent event) {
        createIssue("0.5");
    }

    public void post10(ActionEvent event) {
        createIssue("1.0");
    }

    public void post15(ActionEvent event) {
        createIssue("1.5");
    }

    public void post20(ActionEvent event) {
        createIssue("2.0");
    }

    public void post25(ActionEvent event) {
        createIssue("2.5");
    }

    public void post30(ActionEvent event) {
        createIssue("3.0");
    }

    public void post35(ActionEvent event) {
        createIssue("3.5");
    }

    public void post40(ActionEvent event) {
        createIssue("4.0");
    }

    public void post80(ActionEvent event) {
        createIssue("8.0");
    }

    /** java.time.LocalDateTime >> java.util.Date **/
    public Date localDateTimeToDate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate.atStartOfDay(), zone);

        Instant instant = zonedDateTime.toInstant();
        return Date.from(instant);
    }

    private class ItemListCell extends ListCell<Item> {
        @Override
        protected void updateItem(Item item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) setText(item.name);
        }
    }
}
