/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seabattlegame;

import seabattlegui.ISeaBattleGUI;
import seabattlegui.ShipType;
import seabattlegui.ShotType;
import seabattlegui.SquareState;
import seabattlelogic.Cell;
import seabattlelogic.Game;

import java.rmi.RemoteException;

/**
 * The Sea Battle game. To be implemented.
 * @author Nico Kuijpers
 */
public class SeaBattleGame implements ISeaBattleGame {

    Game game;
    ISeaBattleGUI application;

    public SeaBattleGame() {
        game = new Game(this);
    }

    public ISeaBattleGUI getApplication() {return this.application;}

    @Override
    public int registerPlayer(String name, ISeaBattleGUI application, boolean singlePlayerMode) {
       this.application = application;
       int playerNr = game.registerPlayer(name, singlePlayerMode);
        if (singlePlayerMode) {
            application.setOpponentName(playerNr, game.getPlayerByNr(1 - playerNr).getName());
        }
       return playerNr;
    }

    @Override
    public boolean placeShipsAutomatically(int playerNr) {
       //Place ships randomly and refresh grid
       game.placeShipsRandom(playerNr);
       refreshGrid(playerNr);
       return true;
    }

    @Override
    public boolean placeShip(int playerNr, ShipType shipType, int bowX, int bowY, boolean horizontal) {
        //Check if ship is placed
        boolean placed = game.placeShip(playerNr, shipType, bowX, bowY, horizontal);

        //If placed == true refresh grid of player with given playerNr
        if (placed) {
            refreshGrid(playerNr);
        }

        return placed;
    }

    @Override
    public boolean removeShip(int playerNr, int posX, int posY) {
        //returns player by playerNr and removes ship
        boolean removed = game.removeShip(playerNr, posX, posY);

        //After ship is removed refresh grid
        refreshGrid(playerNr);
        return removed;
    }

    @Override
    public boolean removeAllShips(int playerNr) {
        //Get placed ships of player with matching playerNr
        game.removeAllShips(playerNr);

        //After ships is removed refresh grid
        refreshGrid(playerNr);
        return true;
    }

    @Override
    public boolean notifyWhenReady(int playerNr) {
        return game.setStateToReady(playerNr);
    }

    @Override
    public ShotType fireShotPlayer(int playerNr, int posX, int posY) {
        //Print new turn
        System.out.println("\n\n<-- New Turn -->\n");

        //Fire shot by player on positions
        ShotType shot = game.fireShot(playerNr, posX, posY);

        //Print result of shot
        System.out.println("Result of the player shot: " + shot);

        //Refresh opponent grid
        refreshOpponentGrid(playerNr);
        return shot;
    }

    @Override
    public ShotType fireShotOpponent(int playerNr) {
        //Let ai make best choice for shooting
        ShotType shot = game.fireShotOpponent(playerNr);

        //Refresh players grid
        refreshGrid(playerNr);

        //Print result of opponent shot
        System.out.println("Result of the opponent shot:" + shot);

        //Opponent fires shot
        application.opponentFiresShot(playerNr, shot);
        return ShotType.MISSED;
    }

    @Override
    public boolean startNewGame(int playerNr) {
        //Make new game
        game = new Game(this);
        for (int i = 0; i <= 9; i++) {
            for (int j = 0; j <= 9; j++) {
                //Refresh show square
                application.showSquareOpponent(playerNr, i, j, SquareState.WATER);
                application.showSquarePlayer(playerNr, i, j, SquareState.WATER);
            }
        }
        return true;
    }

    private void refreshGrid(int playerNr) {
        //Run 10 times for each cell in the grid
        for (int i = 0; i <= 9; i++) {
            //Run 10 times for each cell in the grid
            for (int j = 0; j <= 9; j++) {
                //Gets grid for the player with given playerNr
                Cell cell = game.getPlayerGrid(playerNr).getCells()[i][j];
                //Show state in square of ocean area
                application.showSquarePlayer(playerNr, i, j, cell.getSquareState());
            }
        }
    }

    private void refreshOpponentGrid(int playerNr) {
        //Run 10 times for each cell in the grid
        for (int i = 0; i <= 9; i++) {
            //Run 10 times for each cell in the grid
            for (int j = 0; j <= 9; j++) {
                //Get grid for opponents grid
                Cell cell = game.getPlayerGrid(1 - playerNr).getCells()[i][j];
                //if there is no ship in the square state
                if (cell.getSquareState() != SquareState.SHIP)
                    //Show state in square of ocean area
                    application.showSquareOpponent(playerNr, i, j, cell.getSquareState());
            }
        }
    }

    public void fireShotMultiplayer(ShotType result, int x, int y) {
        refreshGrid(game.getApplicationPlayer().getPlayerNr());
        System.out.println("Result of the opponent shot:" + result);
        application.opponentFiresShot(game.getApplicationPlayer().getPlayerNr(), result);
    }
}
