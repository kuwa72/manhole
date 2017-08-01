package jp.kuwa72.manhole;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jp.kuwa72.manhole.bean.Config;

import java.io.IOException;
import java.net.URL;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = getClass().getResource("Main.class");
        System.out.println(url.toString());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/jp/kuwa72/manhole/sample.fxml"));
        Parent root = loader.load();
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        Controller controller = loader.getController();

        primaryStage.setOnCloseRequest(event -> {
            try {
                controller.quit();
            } catch (IOException e) {
                //XXX
                e.printStackTrace();
            }
        });

        Config config = (new ConfigurationStore()).load();

        Scene scene = new Scene(root, config.width, config.height);

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
                controller.width = newSceneWidth.intValue();
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                controller.height = newSceneHeight.intValue();
            }
        });

        primaryStage.setTitle("Manhole");
        primaryStage.setScene(scene);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}

