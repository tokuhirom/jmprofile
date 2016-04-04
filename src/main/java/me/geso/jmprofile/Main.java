package me.geso.jmprofile;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Class.forName("com.mysql.jdbc.Driver").newInstance();

        URL resource = getClass().getClassLoader().getResource("jmprofile.fxml");
        if (resource == null) {
            throw new NullPointerException("Cannot load jmprofile.fxml");
        }
        Parent root = FXMLLoader.load(resource);
        primaryStage.setTitle("jmprofile - Real time mysql profiler");
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
