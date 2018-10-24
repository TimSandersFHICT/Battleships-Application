package seabattlelogic;

import com.sun.org.apache.xpath.internal.operations.Bool;
import seabattlegui.ShipType;
import seabattlegui.SquareState;

import java.util.ArrayList;

public class Ship {

    public ArrayList<Cell> cells;
    private ShipType shipType;
    private boolean sunk;
    private Grid grid;

    public Ship(ShipType shipType, Grid grid) {
        this.shipType = shipType;
        this.cells = new ArrayList<>();
        this.grid = grid;
    }

    public ShipType getShipType() {
        return shipType;
    }

    /**
     * Checks if all cells of the ship are hit
     * @return true if ship has sunk
     */
    public boolean checkIfSunk() {
        boolean sunk = true;
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).getSquareState() == SquareState.SHIP) {
                sunk = false;
            }
        }
        if (sunk) {
            for (int i = 0; i < cells.size(); i++) {
                cells.get(i).setSquareState(SquareState.SHIPSUNK);
            }
        }
        this.sunk = sunk;
        return sunk;
    }

    public boolean placeShip(ArrayList<Cell> cells) {
        if (isPlaced()) {
            return false;
        }

        for (Cell cell : cells) {
            if (cell.getSquareState() == SquareState.SHIP) {
                return false;
            }
        }
        for (Cell cell : cells) {
            cell.setSquareState(SquareState.SHIP);
        }
        this.cells = cells;
        return true;
    }

    public boolean isOnCell(Cell cell) {
        return cells.contains(cell);
    }

    public boolean isPlaced(){
        return cells.size() != 0;
    }

    private void setCells(ArrayList<Cell> cells){
        this.cells = cells;
    }

    public boolean isSunk() {
        return sunk;
    }

    public void removeShip() {
        for (Cell cell : cells) {
            cell.setSquareState(SquareState.WATER);
        }
        setCells(new ArrayList<>());
    }

    public int getLength(){
        switch(shipType){
            default:
                return 0;
            case MINESWEEPER:
                return 2;
            case SUBMARINE:
                return 3;
            case CRUISER:
                return 3;
            case BATTLESHIP:
                return 4;
            case AIRCRAFTCARRIER:
                return 5;
        }
    }

    public Cell getPlacement() {
        return cells.get(0);
    }

    public boolean isHorizontal() {
        return grid.getCellX(cells.get(0)) == grid.getCellY(cells.get(1));
    }

    public Cell getAnchor() {
        return cells.get(0);
    }
}
