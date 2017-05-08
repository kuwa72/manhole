package jp.kuwa72.manhole;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        Controller controller = new Controller();
        controller.setParent(root);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}

