package seabattlelogic;

import seabattlegui.ShipType;
import seabattlegui.ShotType;
import seabattlegui.SquareState;
import sun.tools.jar.CommandLine;
import java.lang.System;

import java.util.ArrayList;

public class Player {

    private Ship[] ships;
    private Grid grid;
    private String name;
    private boolean ready;
    private int playerNr;


    public Player(String name, int playerNr) {
        this.name = name;
        this.playerNr = playerNr;
        ready = false;
        this.grid = new Grid(10, 10);
        ships = new Ship[5];
        ships[0] = new Ship(ShipType.MINESWEEPER,grid);
        ships[1] = new Ship(ShipType.SUBMARINE,grid);
        ships[2] = new Ship(ShipType.CRUISER,grid);
        ships[3] = new Ship(ShipType.BATTLESHIP,grid);
        ships[4] = new Ship(ShipType.AIRCRAFTCARRIER,grid);
    }

    public String getName() {
        return this.name;
    }

    public int getPlayerNr(){ return playerNr;}


    /**
     * Gets ship with specified type from player
     * @param shipType shiptype from ship
     * @return ship of specified shiptype
     */
    public Ship getShipFromType(ShipType shipType){
        for (Ship ship : ships){
            if (ship.getShipType() == shipType){
                return ship;
            }
        }
        return null;
    }

    /**
     * Places a ship on the selected x and y position, with the correct type and correct length.
     * Direction is based on Horizontal. If a ship of this type exists we will remove it from the grid
     *
     * @param shipType     Type of the ship
     * @param bowX         X start position
     * @param bowY         Y start position
     * @param isHorizontal direction ship is placed in.
     * @return
     */
    public boolean placeShip(ShipType shipType, int bowX, int bowY, boolean isHorizontal) {
        Ship ship = getShipFromType(shipType);
        int height = 1;
        int width = 1;
        if (isHorizontal) {
            width = ship.getLength();
        }
        else{
            height = ship.getLength();
        }

        ArrayList<Cell> cellsToAdd = new ArrayList<Cell>();

        try {
            for(int i = bowX; i < bowX + width; i++){
                for (int j = bowY; j < bowY + height; j++) {
                    cellsToAdd.add(grid.getCells()[i][j]);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
        return ship.placeShip(cellsToAdd);
    }

    /**
     * Remove a specified ship
     * @param shipType shipType to be removed
     */
    public void removeShip(ShipType shipType) {
        for (Ship ship : ships){
            if (ship.getShipType() == shipType) {
                ship.removeShip();
            }
        }
    }

    /**
     * removes a ship that is on the selected position on the grid
     *
     * @param posX the x position, 0 based
     * @param posY the y position, 0 based
     * @return if the removal was succesfull or not
     */
    public boolean removeShip(int posX, int posY) {
        Cell cell = grid.getCells()[posX][posY];
        for (Ship ship : ships) {
            if (ship.isOnCell(cell)) {
                ship.removeShip();
                return true;
            }
        }
        return false;
    }

    /**
     * Set a players state to ready
     */
    public void setStateToReady() {
        ready = true;
    }

    public ShotType fireShot(int posX, int posY) {
        System.out.println("PosX = " + posX + " PosY = " + posY);
        ShotType shot = ShotType.HIT;
        Cell cell = grid.getCell(posX, posY);

        SquareState squareState = cell.getSquareState();
        switch (squareState) {
            case WATER:
                shot = ShotType.MISSED;
                cell.setSquareState(SquareState.SHOTMISSED);
                break;
            case SHOTMISSED:
                shot = ShotType.MISSED;
                break;
            case SHOTHIT:
                shot = ShotType.HIT;
                break;
            case SHIPSUNK:
                shot = ShotType.SUNK;
                break;
            case SHIP:
                cell.setSquareState(SquareState.SHOTHIT);
                shot = CheckIfShipSunk(cell);
                if (shot != ShotType.HIT) {
                    cell.setSquareState(SquareState.SHIPSUNK);
                }
                break;
        }
        return shot;
    }

    private ShotType CheckIfShipSunk(Cell cell) {
        ShotType shot = ShotType.HIT;
        for (Ship ship : ships) {
            if (ship.isOnCell(cell)) {
                if (ship.checkIfSunk()) {
                    shot = ShotType.SUNK;
                }
            }
        }
        for (Ship ship : ships) {
            if (!ship.isSunk()) {
                return shot;
            }
        }
        return ShotType.ALLSUNK;
    }

    /**
     * gets if a ship with the type is placed on the grid
     * @param shipType type to check for
     * @return if the ship is placed
     */
    public boolean isShipPlaced(ShipType shipType){
        return getShipFromType(shipType).isPlaced();
    }

    public Grid getGrid() {
        return grid;
    }

    public Ship[] getShips() {
        return ships;
    }

    public boolean isReady() {return ready;}

    public boolean allShipsPlaced() {
        for (Ship ship : ships) {
            if (!ship.isPlaced()) {
                return false;
            }
        }
        return true;
    }
}
