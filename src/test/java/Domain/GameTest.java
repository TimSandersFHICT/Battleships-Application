package Domain;

import org.junit.Before;
import org.junit.Test;
import seabattlegame.SeaBattleGame;
import seabattlegui.ShipType;
import seabattlegui.ShotType;
import seabattlelogic.Game;
import seabattlelogic.Grid;
import seabattlelogic.Player;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameTest {
    Game game;

    @Before
    public void setUp() {
        game = new Game(new SeaBattleGame());
    }

    @Test
    public void registerPlayerCorrect() {
        int value = game.registerPlayer("Tim", true);
        assertEquals(value, 0);
    }

    @Test
    public void registerPlayerDuplicate() {
        game.registerPlayer("Tim", true);
        int value = game.registerPlayer("Tim", true);
        assertEquals(value, 1);
    }

    @Test
    public void getPlayerByNr() {
        int value = game.registerPlayer("Tim", true);
        Player player = game.getPlayerByNr(value);
        assertEquals(player.getPlayerNr(), value);
    }

    @Test
    public void placeShipCorrect() {
        int value = game.registerPlayer("Tim", true);
        boolean bool = game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        assertTrue(bool);
    }

    @Test
    public void placeShipOverLap() {
        int value = game.registerPlayer("Tim", true);
        game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        boolean bool = game.placeShip(value, ShipType.BATTLESHIP, 1, 1, true);
        assertFalse(bool);
    }

    @Test
    public void placeShipSame() {
        int value = game.registerPlayer("Tim", true);
        game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        boolean bool = game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        assertTrue(bool);
    }

    @Test
    public void placeShipOutOfBounds() {
        int value = game.registerPlayer("Tim", true);
        boolean bool = game.placeShip(value, ShipType.AIRCRAFTCARRIER, 8, 8, false);
        assertFalse(bool);
    }

    @Test
    public void removeShipCorrect() {
        int value = game.registerPlayer("Tim", true);
        game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        boolean bool = game.removeShip(value, 1, 1);
        assertTrue(bool);
    }

    @Test
    public void removeShipIncorrect() {
        int value = game.registerPlayer("Tim", true);
        game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        boolean bool = game.removeShip(value, 5, 1);
        assertFalse(bool);
    }

    @Test
    public void setStateToReadyInCorrect() {
        int value = game.registerPlayer("Tim", true);
        boolean bool = game.setGameReady();
        assertFalse(bool);
    }

    @Test
    public void fireShot() {
        int value = game.registerPlayer("Tim", true);
        ShotType shotType = game.fireShot(0, 1, 1);
        assertEquals(shotType, ShotType.MISSED);
    }

    public int fireShotOpponent() {
        game = new Game(new SeaBattleGame());
        int value = game.registerPlayer("Tim", true);
        game.placeShipsRandom(value);
        int count = 0;
        while (game.fireShotOpponent(0) != ShotType.ALLSUNK) {
            count++;
        }
        return count;
    }

    @Test
    public void fireShotOpponentTest() {
        int testamount = 5;
        int total = 0;
        for (int i = 0; i < testamount; i++) {
            total += fireShotOpponent();
        }
        float value = total / testamount;
        System.out.println(value);
        assertTrue(value < 70);
    }

    @Test
    public void getPlayerGrid() {
        int value = game.registerPlayer("Tim", true);
        Grid grid = game.getPlayerGrid(value);
        assertSame(game.players[0].getGrid(), grid);
    }

    @Test
    public void removeAllShips() {
        int value = game.registerPlayer("Tim", true);
        game.placeShip(value, ShipType.MINESWEEPER, 1, 1, true);
        game.removeAllShips(value);
        assertEquals(0, game.players[0].getShips()[0].cells.size());
    }

    @Test
    public void placeShipsRandom() {
        int value = game.registerPlayer("Tim", true);
        game.placeShipsRandom(value);
        assertTrue(game.players[0].isShipPlaced(ShipType.MINESWEEPER));
        assertTrue(game.players[0].isShipPlaced(ShipType.AIRCRAFTCARRIER));
        assertTrue(game.players[0].isShipPlaced(ShipType.SUBMARINE));
        assertTrue(game.players[0].isShipPlaced(ShipType.CRUISER));
        assertTrue(game.players[0].isShipPlaced(ShipType.BATTLESHIP));
    }

    @Test
    public void setOpponent() {
        int value = game.registerPlayer("Tim", true);
        game.setOpponent("Pieter");
        assertEquals("Pieter", game.getPlayerByNr(1).getName());
    }

    @Test
    public void setGameReady() {
        int value = game.registerPlayer("Tim", true);
        game.placeShipsRandom(value);
        game.setStateToReady(0);
        game.setOpponentReady();
        boolean bool = game.setGameReady();
        assertTrue(bool);
    }

    @Test
    public void setOpponentReady() {
        int value = game.registerPlayer("Tim", true);
        game.setOpponentReady();
        assertTrue(game.getPlayerByNr(1).isReady());
    }
}
