package Domain;

import org.junit.Before;
import org.junit.Test;
import seabattlegui.SquareState;
import seabattlelogic.Cell;
import seabattlelogic.Grid;

import static org.junit.Assert.*;

public class GridTest {
    Grid grid;

    @Before
    public void setUp() {
        grid = new Grid(10, 10);
    }

    @Test
    public void getCells() {
        Cell cell = grid.getCell(0, 0);
        Cell[][] cells = grid.getCells();
        assertSame(cells[0][0], cell);
    }

    @Test
    public void getCellHitCorrect() {
        grid.getCell(5, 5).setSquareState(SquareState.SHOTHIT);
        Cell cell = grid.getCellHit();
        assertNotNull(cell);
    }

    @Test
    public void getCellHitIncorrect() {
        Cell cell = grid.getCellHit();
        assertNull(cell);
    }

    @Test
    public void getCellX() {
        Cell cell = grid.getCell(5, 0);
        int x = grid.getCellX(cell);
        assertEquals(5, x);
    }

    @Test
    public void getCellY() {
        Cell cell = grid.getCell(5, 3);
        int y = grid.getCellY(cell);
        assertEquals(3, y);
    }

    @Test
    public void checkIfCellWaterCorrect() {
        boolean bool = grid.checkIfCellWater(5, 5);
        assertTrue(bool);
    }

    @Test
    public void checkIfCellWaterIncorrect() {
        grid.getCell(5, 5).setSquareState(SquareState.SHOTHIT);
        boolean bool = grid.checkIfCellWater(5, 5);
        assertFalse(bool);
    }
}
