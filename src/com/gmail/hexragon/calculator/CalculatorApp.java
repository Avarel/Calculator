package com.gmail.hexragon.calculator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/*
 * Application load base.
 */
public class CalculatorApp extends Application
{
    protected Stage stage;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        this.stage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main.fxml"));

        AppController controller = new AppController(this.stage);
        loader.setController(controller);

        Parent root = loader.load();

        stage.setTitle("Calculator");
        stage.setScene(new Scene(root));
        stage.setResizable(false);

        controller.setup();

        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream("media/icon.png")));

        stage.show();

        System.out.println(Math.log(10)); //ln
        System.out.println(Math.log10(10)); //log
    }
}
