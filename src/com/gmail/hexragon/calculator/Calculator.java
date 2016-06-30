package com.gmail.hexragon.calculator;

/*
 * Lower-level handling of numbers, emulating a calculator.
 */
public class Calculator
{
    private Double holdingNumber;
    private Double currentNumber;
    private OperatorState state;

    public Double getCurrentNumber()
    {
        return currentNumber;
    }

    public Double getHoldingNumber()
    {
        return holdingNumber;
    }

    public void setHoldingNumber(Double holdingNumber)
    {
        this.holdingNumber = holdingNumber;
    }

    public OperatorState getState()
    {
        return state;
    }

    public void setState(OperatorState state)
    {
        this.state = state;
    }

    public Double consume(double number)
    {
        switch (state)
        {
            case ADD:
                currentNumber = holdingNumber + number;
                break;
            case SUBTRACT:
                currentNumber = holdingNumber - number;
                break;
            case MULTIPLY:
                currentNumber = holdingNumber * number;
                break;
            case DIVIDE:
                currentNumber = holdingNumber / number;
                break;
            case EXPONENTIAL:
                currentNumber = Math.pow(holdingNumber, number);
                break;
            case MODULUS:
                currentNumber = holdingNumber % number;
                break;
            case ENTEREXPONENTIAL:
                currentNumber = holdingNumber * Math.pow(10, number);
                break;
        }
        return currentNumber;
    }

    public enum OperatorState
    {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        EXPONENTIAL,
        MODULUS,
        ENTEREXPONENTIAL
    }
}
