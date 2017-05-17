package jp.kuwa72.manhole;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
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

        primaryStage.setTitle("Manhole");
        primaryStage.setScene(new Scene(root, 300, 275));
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

