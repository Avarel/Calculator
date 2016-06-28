package com.gmail.hexragon.calculator;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Random;

/*
 * Higher level handling of forming numbers and functions.
 */
public class Controller
{
    public JFXTextField positiveNegativeIndicator;
    public JFXTextField holdingNumberField;
    public JFXDrawer drawer;
    private Calculator calculator;
    private Stage stage;

    @FXML
    public GridPane root;

    public JFXTextField numberField;
    public AnchorPane titleBar;

    private double xOffset = 0;
    private double yOffset = 0;

    DecimalFormat displayFormat = new DecimalFormat("#,###.#########");

    private boolean decimalSwitch;
    private boolean newNumberSwitch;

    private boolean previousArithmeticSignClick;

    public void setup()
    {
        calculator = new Calculator();
        stage = (Stage) root.getScene().getWindow();

        titleBar.setOnMousePressed(event ->
        {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event ->
        {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        //numberField.focusedProperty().addListener(observable -> root.requestFocus());

        allClearClick();

        displayFormat.setDecimalSeparatorAlwaysShown(false);
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

            if (target.equals("π"))
            {
                setNumberField(Math.PI);
                return;
            }

            if (target.equals("0") && numberField.getText().contains("."))
            {
                /* Cancel the replacement of the zero in ".0" */
                if (decimalSwitch)
                {
                    decimalSwitch = false;
                    return;
                }

                /* Add on to the decimal */
                setNumberField(displayFormat.parse(numberField.getText() + target).doubleValue());
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
        if (calculator.getHoldingNumber() == null || calculator.state == null) return;
        try
        {
            holdingNumberField.setText(holdingNumberField.getText() + numberField.getText());

            calculator.consume(displayFormat.parse(numberField.getText()).doubleValue());

            calculator.setHoldingNumber(null);

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

            if (calculator.state != null)
            switch (calculator.state)
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
            }

            holdingNumberField.setText(displayFormat.format(num)+operator);
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

        String[] array = new String[]{"Ready to calculate!", "Calculator ready to go!", "Lock and loaded!", "Ready to ace that thing?", "Made with love!", "Let's practice medi... math!", "Be gentle with those clicks!", "Wow, this calculator talks?!", "You can count on me!", "Calc you later!"};
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
                    holdingNumberField.setText("√("+numberField.getText()+")");
                    setNumberField(Math.sqrt(displayFormat.parse(numberField.getText()).doubleValue()));
                    break;
                case "%":
                    holdingNumberField.setText(numberField.getText()+" / 100");
                    setNumberField(displayFormat.parse(numberField.getText()).doubleValue()/100d);
                    break;
                case "1/x":
                    holdingNumberField.setText("1 / "+numberField.getText());
                    setNumberField(1d/displayFormat.parse(numberField.getText()).doubleValue());
                    break;
            }

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }


    }

    public void backspaceClick()
    {
        try
        {
            if (newNumberSwitch) return;

            final String txt = String.valueOf(displayFormat.parse(numberField.getText()).doubleValue());
            String finalStr = txt;

            System.out.println(txt.substring(txt.length()-2));

            if (txt.length() == 3) /* Single digit like 5.0 or 7.0 have 3 characters as a Double*/
            {
                setNumberField(0);
                return;
            }

            if (txt.indexOf(".") != txt.length() - 2)
            {
                System.out.println(txt);
                System.out.println(txt.indexOf("."));
                finalStr = finalStr.substring(0, finalStr.length() - 1);
            }
            else if (decimalSwitch || txt.substring(txt.length()-2).matches(".[1-9]"))
            {
                finalStr = finalStr.substring(0, finalStr.length() - 2);
                decimalSwitch = false;
            }
            else
            {
                finalStr = finalStr.substring(0, finalStr.length() - 3);
            }

            if (calculator.getHoldingNumber() == null) setHoldingNumberField(Double.parseDouble(finalStr));
            setNumberField(Double.parseDouble(finalStr));

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
