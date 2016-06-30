package com.gmail.hexragon.calculator;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Random;

/*
 * Higher level handling of forming numbers and functions.
 */
public class AppController
{
    private final Stage stage;

    @FXML
    protected JFXTextField positiveNegativeIndicator;

    @FXML
    protected JFXTextField holdingNumberField;

    @FXML
    protected JFXDrawer drawer;

    @FXML
    protected GridPane root;

    @FXML
    protected JFXTextField numberField;

    @FXML
    protected AnchorPane titleBar;

    DecimalFormat displayFormat = new DecimalFormat("#,###.#########");

    /*
     * The calculation takes place in this instance.
     * The AppController provides a layer of higher-level
     * handling such as forming and formatting numbers,
     * handling functions and displaying GUI components.
     */
    private Calculator calculator;

    private double xOffset = 0;
    private double yOffset = 0;

    private boolean decimalSwitch;
    private boolean newNumberSwitch;
    private boolean previousArithmeticSignClick;

    protected AppController(Stage stage)
    {
        this.stage = stage;
    }

    public void setup()
    {
        calculator = new Calculator();

        titleBar.setOnMousePressed(event ->
        {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        final Stage finalStage = stage;
        titleBar.setOnMouseDragged(event ->
        {
            finalStage.setX(event.getScreenX() - xOffset);
            finalStage.setY(event.getScreenY() - yOffset);
        });

        allClearClick();

        displayFormat.setDecimalSeparatorAlwaysShown(false);


        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/extras.fxml"));

            loader.setController(this);

            GridPane drawerPane = loader.load();

            drawer.setSidePane(drawerPane);
            drawer.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Reads the number/symbol on the button to insert a digit.
     */
    public void numberClick(ActionEvent actionEvent)
    {
        previousArithmeticSignClick = false;

        if (newNumberSwitch)
        {
            numberField.setText("0");
            newNumberSwitch = false;
            positiveNegativeIndicator.clear();
        }
        try
        {
            String target = ((Button) actionEvent.getSource()).getText();

            if (target.equals("0") && numberField.getText().contains("."))
            {
                /* Cancel the replacement of the zero in ".0" */
                if (decimalSwitch)
                {
                    decimalSwitch = false;
                    return;
                }

                /* Add a zero to the end of the decimal without formatting (formatting will delete the 0) */
                numberField.setText(numberField.getText() + 0);
            }
            else
            {
                /* Recently clicked the [.] button and it added ".0", clicking the next number will replace the zero.*/
                if (decimalSwitch)
                {
                    setNumberField(displayFormat.parse(numberField.getText().substring(0, numberField.getText().length() - 1) + target).doubleValue());
                    decimalSwitch = false;
                }

                /* Read button to add number to the field. */
                else
                {
                    setNumberField(displayFormat.parse(numberField.getText() + target).doubleValue());
                }
            }

            /* Update the mini-field */
            if (calculator.getHoldingNumber() != null) setHoldingNumberField(calculator.getHoldingNumber());
            else setHoldingNumberField(displayFormat.parse(numberField.getText()).doubleValue());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public void decimalClick()
    {
        if (!numberField.getText().contains("."))
        {
            numberField.setText(numberField.getText() + ".0");
            decimalSwitch = true; /* Trigger the signal to replace the zero in placeholder ".0" added to the number.  */
        }
    }

    public void arithmeticSignClick(ActionEvent actionEvent)
    {
        boolean recentlyConsumed = false;

        /*
         * Compute previous calculations if holding a number and operator is set before switching to another operation.
         */
        if (!previousArithmeticSignClick && calculator.getHoldingNumber() != null)
        {
            try
            {
                calculator.consume(displayFormat.parse(numberField.getText()).doubleValue());

                calculator.setHoldingNumber(calculator.getCurrentNumber());

                setNumberField(calculator.getCurrentNumber());

                holdingNumberField.setText(holdingNumberField.getText() + numberField.getText());

                recentlyConsumed = true;
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        switch (((Button) actionEvent.getSource()).getText())
        {
            case "+":
                calculator.setState(Calculator.OperatorState.ADD);
                break;
            case "–":
                calculator.setState(Calculator.OperatorState.SUBTRACT);
                break;
            case "*":
                calculator.setState(Calculator.OperatorState.MULTIPLY);
                break;
            case "/":
                calculator.setState(Calculator.OperatorState.DIVIDE);
                break;
            case "^":
                calculator.setState(Calculator.OperatorState.EXPONENTIAL);
                break;
            case "mod":
                calculator.setState(Calculator.OperatorState.MODULUS);
                break;
            case "EE":
                calculator.setState(Calculator.OperatorState.ENTEREXPONENTIAL);
                break;
            default:
                calculator.setState(Calculator.OperatorState.ADD);
                break;
        }

        newNumberSwitch = true;
        previousArithmeticSignClick = true;

        try
        {
            if (positiveNegativeIndicator.getText().isEmpty())
            {
                calculator.setHoldingNumber(displayFormat.parse(numberField.getText()).doubleValue());
            }
            else
            {
                calculator.setHoldingNumber(-displayFormat.parse(numberField.getText()).doubleValue());
            }

            /* Update the mini-field */
            if (!recentlyConsumed)
            {
                if (calculator.getHoldingNumber() != null) setHoldingNumberField(calculator.getHoldingNumber());
                else setHoldingNumberField(displayFormat.parse(numberField.getText()).doubleValue());
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public void positiveNegativeClick()
    {
        if (positiveNegativeIndicator.getText().isEmpty())
        {
            positiveNegativeIndicator.setText("-");
        }
        else
        {
            positiveNegativeIndicator.clear();
        }
    }

    public void equalClick()
    {
        if (calculator.getHoldingNumber() == null || calculator.getState() == null) return;
        try
        {
            holdingNumberField.setText(holdingNumberField.getText() + numberField.getText());

            calculator.consume(displayFormat.parse(numberField.getText()).doubleValue());

            calculator.setHoldingNumber(null);
            calculator.setState(null);

            setNumberField(calculator.getCurrentNumber());

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        newNumberSwitch = true;
    }

    public void setNumberField(double num)
    {
        double displayValue = Math.abs(num);
        boolean isNegative = num < 0;

        numberField.setText(displayFormat.format(displayValue));

        if (isNegative)
        {
            positiveNegativeIndicator.setText("-");
        }
        else
        {
            positiveNegativeIndicator.clear();
        }
    }

    /*
     * Formats the top left mini-field to keep track of calculations.
     */
    public void setHoldingNumberField(double num)
    {
        if (num == 0)
        {
            holdingNumberField.clear();
        }
        else
        {
            String operator = "";

            if (calculator.getState() != null)
                switch (calculator.getState())
                {
                    case ADD:
                        operator = " + ";
                        break;
                    case SUBTRACT:
                        operator = " - ";
                        break;
                    case MULTIPLY:
                        operator = " * ";
                        break;
                    case DIVIDE:
                        operator = " / ";
                        break;
                    case EXPONENTIAL:
                        operator = " ^ ";
                        break;
                    case MODULUS:
                        operator = " mod ";
                        break;
                    case ENTEREXPONENTIAL:
                        operator = "E";
                }

            holdingNumberField.setText(displayFormat.format(num) + operator);
        }
    }

    /*
     * Resets state of calculator object.
     */
    public void allClearClick()
    {
        calculator.setHoldingNumber(null);
        calculator.setState(null);
        setNumberField(0);

        /* Useless greeting texts. */
        String[] array = new String[]{"Ready to calculate!", "Calculator ready to go!", "Lock and loaded!", "Ready to ace that thing?", "Made with love!", "Let's do some math!", "Be gentle with those clicks!", "Wow, this calculator talks?!", "You can count on me!", "Calc you later!"};
        holdingNumberField.setText(array[new Random().nextInt(array.length)]);
    }

    /*
     * These immediately modify a number without further input.
     */
    public void functionClick(ActionEvent actionEvent)
        {
        try
        {
            switch (((Button) actionEvent.getSource()).getText())
            {
                case "√":
                    holdingNumberField.setText("√(" + numberField.getText() + ")");
                    setNumberField(Math.sqrt(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "%":
                    holdingNumberField.setText(numberField.getText() + " / 100");
                    setNumberField(displayFormat.parse(numberField.getText()).doubleValue() / 100d);
                    break;
                case "1/x":
                    holdingNumberField.setText("1 / " + numberField.getText());
                    setNumberField(1d / displayFormat.parse(numberField.getText()).doubleValue());
                    break;
                case "sin":
                    holdingNumberField.setText("sin(" + numberField.getText() + ")");
                    setNumberField(Math.sin(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "cos":
                    holdingNumberField.setText("cos(" + numberField.getText() + ")");
                    setNumberField(Math.cos(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "tan":
                    holdingNumberField.setText("tan(" + numberField.getText() + ")");
                    setNumberField(Math.tan(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "sin-1":
                    holdingNumberField.setText("sin-1(" + numberField.getText() + ")");
                    setNumberField(Math.asin(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "cos-1":
                    holdingNumberField.setText("cos-1(" + numberField.getText() + ")");
                    setNumberField(Math.acos(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "tan-1":
                    holdingNumberField.setText("tan-1(" + numberField.getText() + ")");
                    setNumberField(Math.atan(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "log":
                    holdingNumberField.setText("log(" + numberField.getText() + ")");
                    setNumberField(Math.log10(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "ln":
                    holdingNumberField.setText("ln(" + numberField.getText() + ")");
                    setNumberField(Math.log(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "x!":
                    holdingNumberField.setText(numberField.getText() + "!");
                    setNumberField(factorial(displayFormat.parse(numberField.getText()).longValue()));
                    break;

            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    long factorial(long number) {
        if (number <= 1)
            return 1;
        else
            return number * factorial(number - 1);
    }

    public void backspaceClick()
    {
        try
        {
            if (newNumberSwitch) return;

            final String txt = String.valueOf(displayFormat.parse(numberField.getText()).doubleValue());
            String finalStr = txt;

            if (txt.length() == 3) /* Single digit like 5.0 or 7.0 have 3 characters as a Double*/
            {
                if (txt.substring(txt.length() - 2).matches(".[1-9]"))
                {
                    finalStr = finalStr.substring(0, finalStr.length() - 2);
                }
                else
                {
                    setNumberField(0);
                    return;
                }
            }
            else
            {
                if (txt.indexOf(".") != txt.length() - 2)
                {
                    finalStr = finalStr.substring(0, finalStr.length() - 1);
                }
                else if (decimalSwitch || txt.substring(txt.length() - 2).matches(".[1-9]"))
                {
                    finalStr = finalStr.substring(0, finalStr.length() - 2);
                    decimalSwitch = false;
                }
                else
                {
                    finalStr = finalStr.substring(0, finalStr.length() - 3);
                }
            }

            if (calculator.getHoldingNumber() == null) setHoldingNumberField(Double.parseDouble(finalStr));
            setNumberField(Double.parseDouble(finalStr));

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public void drawerToggle()
    {
        if (drawer.isHidden())
        {
            drawer.open();
        }
        else
        {
            drawer.close();
        }
    }

    public void sourceClick()
    {
        try
        {
            java.awt.Desktop.getDesktop().browse(new URI("https://github.com/Hexragon/Calculator"));
        }
        catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Sets the current number to a preset number,
     * no matter what the currently displayed number is.
     */
    public void presetNumberClick(ActionEvent actionEvent)
    {
        if (newNumberSwitch)
        {
            numberField.setText("0");
            newNumberSwitch = false;
            positiveNegativeIndicator.clear();
        }

        String target = ((Button) actionEvent.getSource()).getText();

        switch (target)
        {
            case "π":
                setNumberField(Math.PI);
                break;
            case "e":
                setNumberField(Math.E);
                break;
            case "Rand":
                setNumberField(Math.random());
                break;
        }

        try
        {
            setHoldingNumberField(displayFormat.parse(numberField.getText()).doubleValue());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public void closeClick()
    {
        stage.close();
    }

    public void minimizeClick()
    {
        stage.setIconified(true);
    }
}
