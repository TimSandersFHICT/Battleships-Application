package seabattlelogic;

import seabattlegui.SquareState;

public class Cell {

    private SquareState squareState;

    public Cell() {
        this.squareState = SquareState.WATER;
    }

    public SquareState getSquareState() {
        return squareState;
    }

    public void setSquareState(SquareState squareState) {
        this.squareState = squareState;
    }


}
