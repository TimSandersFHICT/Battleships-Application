package seabattlelogic;

import seabattlegui.SquareState;

public class Grid {

    private Cell[][] cells;

    public Grid(int width, int height) {

        this.cells = new Cell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    public Cell[][] getCells() {
        return cells;
    }

    public Cell getCell(int posX, int posY) {
        return cells[posX][posY];
    }

    public Cell getCellHit() {
        for (Cell[] cellArray : cells) {
            for (Cell cell : cellArray) {
                if (cell.getSquareState() == SquareState.SHOTHIT) {
                    return cell;
                }
            }
        }
        return null;
    }

    public int getCellX(Cell checkCell) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = cells[i][j];
                if (cell == checkCell) {
                    return i;
                }
            }
        }
        return 0;
    }

    public int getCellY(Cell checkCell) {
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell cell = cells[i][j];
                if (cell == checkCell) {
                    return j;
                }
            }
        }
        return 0;
    }

    public boolean checkIfCellWater(int x, int y) {
        try {
            if (cells[x][y].getSquareState() == SquareState.WATER) {
                return true;
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
        }
        return false;
    }
}
