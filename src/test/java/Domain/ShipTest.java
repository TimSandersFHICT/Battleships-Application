package Domain;

import org.junit.Before;
import org.junit.Test;
import seabattlegui.ShipType;
import seabattlegui.SquareState;
import seabattlelogic.Cell;
import seabattlelogic.Grid;
import seabattlelogic.Ship;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class ShipTest {
    Ship ship;
    Grid grid;

    @Before
    public void setUp() {
        grid = new Grid(10, 10);
        ship = new Ship(ShipType.MINESWEEPER, grid);
    }

    @Test
    public void getShipType() {
        assertEquals(ShipType.MINESWEEPER, ship.getShipType());
    }

    @Test
    public void checkIfSunk() {
        boolean bool = ship.checkIfSunk();
        assertTrue(bool);
    }

    @Test
    public void placeShip() {
        ArrayList<Cell> cells = new ArrayList<>();
        cells.add(grid.getCell(5, 5));
        cells.add(grid.getCell(5, 6));
        ship.placeShip(cells);
        assertEquals(cells.size(), ship.cells.size());
    }

    @Test
    public void isOnCell() {
    }

    @Test
    public void isPlaced() {
    }


    @Test
    public void checkIfSunk2() {
        ArrayList<Cell> cells = new ArrayList<>();
        cells.add(grid.getCell(5, 5));
        cells.add(grid.getCell(5, 6));
        ship.placeShip(cells);
        grid.getCell(5, 5).setSquareState(SquareState.SHOTHIT);
        grid.getCell(5, 6).setSquareState(SquareState.SHOTHIT);
        assertTrue(ship.checkIfSunk());
        assertTrue(ship.isSunk());
    }

    @Test
    public void removeShip() {
        ArrayList<Cell> cells = new ArrayList<>();
        cells.add(grid.getCell(5, 5));
        cells.add(grid.getCell(5, 6));
        ship.placeShip(cells);
        ship.removeShip();
        assertEquals(0, ship.cells.size());
    }

    @Test
    public void getLength() {
        assertEquals(2, ship.getLength());
    }

    @Test
    public void getAnchor() {
        ArrayList<Cell> cells = new ArrayList<>();
        cells.add(grid.getCell(5, 5));
        cells.add(grid.getCell(5, 6));
        ship.placeShip(cells);
        assertSame(grid.getCell(5, 5), ship.getAnchor());
    }

    @Test
    public void isHorizontalFalse() {
        ArrayList<Cell> cells = new ArrayList<>();
        cells.add(grid.getCell(5, 5));
        cells.add(grid.getCell(5, 6));
        ship.placeShip(cells);
        assertFalse(ship.isHorizontal());
    }

    @Test
    public void isHorizontalTrue() {
        ArrayList<Cell> cells = new ArrayList<>();
        cells.add(grid.getCell(5, 5));
        cells.add(grid.getCell(6, 5));
        ship.placeShip(cells);
        assertTrue(ship.isHorizontal());
    }
}
