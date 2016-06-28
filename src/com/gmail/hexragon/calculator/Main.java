package com.gmail.hexragon.calculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/*
 * Application load base.
 */
public class Main extends Application
{
    protected Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        stage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("calculatorGUI.fxml"));
        Parent root = loader.load();


        primaryStage.setTitle("Calculator");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);

        primaryStage.initStyle(StageStyle.UNDECORATED);

        Controller controller = loader.getController();
        controller.setup();

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
