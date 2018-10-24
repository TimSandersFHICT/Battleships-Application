package seabattlelogic;

import com.google.gson.JsonObject;
import seabattleclient.Client;
import seabattlegame.SeaBattleGame;
import seabattlegui.ShipType;
import seabattlegui.ShotType;
import seabattlegui.SquareState;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Game {

    public Player[] players;
    private int playerOnTurn;
    private boolean singlePlayerMode;


    private Client client;

    private SeaBattleGame seaBattleGame;

    public Player getApplicationPlayer() {
        return players[0];
    }

    private Grid aiGrid;

    public Game(SeaBattleGame seaBattleGame) {
        this.seaBattleGame = seaBattleGame;
        players = new Player[2];
        //select a random turn
        playerOnTurn = new Random().nextInt(2);
    }


    /**
     * registers a player and sets the game to single or multiplayer
     *
     * @param name
     * @param singlePlayerMode
     * @return the id of the registered player
     */
    public int registerPlayer(String name, boolean singlePlayerMode) {
        this.singlePlayerMode = singlePlayerMode;
        if (singlePlayerMode) {
            if (players[0] == null) {
                players[0] = new Player(name, 0);
                registerAi();
                return 0;
            } else {
                if (!players[0].getName().equals(name)) {
                    players[1] = new Player(name, 1);
                    return 1;
                } else {
                    return -1;
                }
            }
        } else {
            //connect to server
            if (client == null) {
                client = new Client(this, name);
            }
            try {
                //get id from server
                try {
                    int id = client.getPlayerId().poll(1, TimeUnit.SECONDS);
                    //check name taken
                    if (id > -1) {
                        System.out.println("Id " + name + ": " + id);
                        players[0] = new Player(name, id);
                    }
                    return id;

                } catch (NullPointerException ex) {
                    return -1;
                }

            } catch (InterruptedException e) {
                return -1;
            }
        }
    }

    private void registerAi() {
        if (singlePlayerMode) {
            players[1] = new Player("AI bot", 1);
            setupAi(players[1].getPlayerNr());
            players[1].setStateToReady();
            System.out.println("ai ready to play");
        }
    }

    /**
     * Gets the player with the playerNr.
     * @param playerNr 1 or 0, depending on the player
     * @return return the player with the playerNr
     */
    public Player getPlayerByNr(int playerNr) {
        if (playerNr > 1) {
            throw new IllegalArgumentException();
        }
        if (players[0].getPlayerNr() == playerNr) {
            return players[0];
        } else {
            return players[1];
        }
    }

    /**
     * Places a ship on the grid of the player.
     * @param playerNr   nr of the player where the ship should be placed on
     * @param shipType   the type of the ship
     * @param bowX       the min value of the x position
     * @param bowY       the min value of the y position
     * @param horizontal if the ship is placed horizontal
     * @return return if the ship is placed or not
     */
    public boolean placeShip(int playerNr, ShipType shipType, int bowX, int bowY, boolean horizontal) {
        Player player = getPlayerByNr(playerNr);

        //Returns ship type of ship
        if (player.isShipPlaced(shipType)) {
            player.removeShip(shipType);
        }
        return player.placeShip(shipType, bowX, bowY, horizontal);
    }

    /**
     * Removes a ship for a player.
     * @param playerNr playerNr for whom the ship will be removed
     * @param posX x position of the ship
     * @param posY y position of the ship
     */
    public boolean removeShip(int playerNr, int posX, int posY) {
        return getPlayerByNr(playerNr).removeShip(posX, posY);
    }

    /**
     * Indicate that a player has placed all ship and is ready to play.
     * @param playerNr playerNr that is ready to play
     * @return if the player is ready or not
     */
    public boolean setStateToReady(int playerNr) {
        boolean succes = getPlayerByNr(playerNr).allShipsPlaced();
        if (succes) {
            getPlayerByNr(playerNr).setStateToReady();
        }
        if (!singlePlayerMode) {
            client.sendReady();
        }
        setGameReady();
        return succes;
    }

    /**
     * sets up the ai
     *
     * @param playerNr player id of the ai
     */
    private void setupAi(int playerNr) {
        placeShipsRandom(playerNr);
        aiGrid = new Grid(10, 10);
        Player player = getPlayerByNr(playerNr);
        player.setStateToReady();
        setGameReady();
    }

    /**
     * fires a shot on the players grid at the selected position
     *
     * @param playerNr id of the player
     * @param posX     x position
     * @param posY     y position
     * @return the result of the shot
     */
    public ShotType fireShot(int playerNr, int posX, int posY) {
        Player player = getPlayerByNr(1 - playerNr);
        if (singlePlayerMode) {
            return player.fireShot(posX, posY);
        } else {
            System.out.println("We gunna give you a shot");
            client.sendShot(posX, posY);
            try {
                JsonObject json = client.getReturnShot().poll(5, TimeUnit.SECONDS);
                ShotType shotType = ShotType.valueOf(json.get("ShotType").getAsString());
                if (shotType == ShotType.SUNK) {
                    System.out.println("We got a ship sunk");
                    int xAnchor = json.get("X").getAsInt();
                    int yAnchor = json.get("Y").getAsInt();
                    boolean horizontal = json.get("Horizontal").getAsBoolean();
                    int length = json.get("Length").getAsInt();
                    int xLength = 1;
                    int yLength = 1;
                    if (horizontal) {
                        xLength = length;
                    } else {
                        yLength = length;
                    }
                    for (int i = xAnchor; i < xAnchor + xLength; i++) {
                        for (int j = yAnchor; j < yAnchor + yLength; j++) {
                            player.getGrid().getCell(i, j).setSquareState(SquareState.SHIPSUNK);
                        }
                    }
                    return shotType;
                } else {
                    player.getGrid().getCell(posX, posY).setSquareState(convertShotTypeToSquareState(shotType));
                    return shotType;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

    }


    private boolean CheckCellHit(int xCell, int yCell) {
        try {
            if (aiGrid.getCell(xCell, yCell).getSquareState() == SquareState.SHOTHIT) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            //ignore
        }
        return false;
    }

    public ShotType fireShotOpponent(int playerNr) {
        System.out.println("\n<--Ai generated code-->");
        Random random = new Random();
        int x = random.nextInt(10);
        int y = random.nextInt(10);

        Cell cell = aiGrid.getCellHit();

        //no ship hit, pick random next shot
        if (cell == null) {
            System.out.println("Calculating a semi-random hit");
            cell = aiGrid.getCell(x, y);
            while (cell.getSquareState() != SquareState.WATER) {
                System.out.println("Generating next semi-random target");
                x = random.nextInt(10);
                y = random.nextInt(10);
                cell = aiGrid.getCell(x, y);
            }

        } else {
            //a ship is hit, calculate next shot
            int xCell = aiGrid.getCellX(cell);
            int yCell = aiGrid.getCellY(cell);

            System.out.println("Starting X: " + xCell);
            System.out.println("Starting Y: " + yCell);

            boolean horizontal = false;
            boolean vertical = false;

            if (CheckCellHit(xCell, yCell + 1) || CheckCellHit(xCell, yCell - 1)) {
                System.out.println("Ship is placed vertical");
                vertical = true;
            }

            if (CheckCellHit(xCell + 1, yCell) || CheckCellHit(xCell - 1, yCell)) {
                System.out.println("Ship is placed horizontal");
                horizontal = true;
            }

            //ship is horizontal
            if (horizontal) {
                System.out.println("Calculating horizontal shot");
                int i = 0;
                Cell target;

                try {
                    target = aiGrid.getCell(xCell - 1, yCell);
                } catch (ArrayIndexOutOfBoundsException e) {
                    i++;
                    target = aiGrid.getCell(xCell + i, yCell);
                }

                while (target.getSquareState() != SquareState.WATER) {
                    i++;
                    target = aiGrid.getCell(xCell + i, yCell);
                }
                x = aiGrid.getCellX(target);
                y = aiGrid.getCellY(target);
            }

            //ship is vertical
            if (vertical) {
                System.out.println("Calculating vertical shot");
                int i = 0;
                Cell target;

                try {
                    target = aiGrid.getCell(xCell, yCell - 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    i++;
                    target = aiGrid.getCell(xCell, yCell + i);
                }

                while (target.getSquareState() != SquareState.WATER) {
                    i++;
                    target = aiGrid.getCell(xCell, yCell + i);
                }
                x = aiGrid.getCellX(target);
                y = aiGrid.getCellY(target);
            }

            //pick a side from the origin
            if (!vertical && !horizontal) {
                System.out.println("Single Unit");
                if (aiGrid.checkIfCellWater(xCell - 1, yCell)) {
                    x = aiGrid.getCellX(aiGrid.getCell(xCell - 1, yCell));
                    y = aiGrid.getCellY(aiGrid.getCell(xCell - 1, yCell));
                } else if (aiGrid.checkIfCellWater(xCell, yCell - 1)) {
                    x = aiGrid.getCellX(aiGrid.getCell(xCell, yCell - 1));
                    y = aiGrid.getCellY(aiGrid.getCell(xCell, yCell - 1));
                } else if (aiGrid.checkIfCellWater(xCell + 1, yCell)) {
                    x = aiGrid.getCellX(aiGrid.getCell(xCell + 1, yCell));
                    y = aiGrid.getCellY(aiGrid.getCell(xCell + 1, yCell));
                } else if (aiGrid.checkIfCellWater(xCell, yCell + 1)) {
                    x = aiGrid.getCellX(aiGrid.getCell(xCell, yCell + 1));
                    y = aiGrid.getCellY(aiGrid.getCell(xCell, yCell + 1));
                }
            }
        }

        //shoot
        ShotType shot = fireShot(1 - playerNr, x, y);
        SquareState state = convertShotTypeToSquareState(shot);
        aiGrid.getCell(x, y).setSquareState(state);

        System.out.println("Target X: " + x);
        System.out.println("Target Y: " + y);

        //update grid with shipsunk
        Player player = getPlayerByNr(playerNr);
        Grid playerGrid = player.getGrid();
        for (int i = 0; i < playerGrid.getCells().length; i++) {
            for (int j = 0; j < playerGrid.getCells()[0].length; j++) {
                if (playerGrid.getCell(i, j).getSquareState() == SquareState.SHIPSUNK) {
                    aiGrid.getCell(i, j).setSquareState(SquareState.SHIPSUNK);
                }
            }
        }
        System.out.println("<--------------------->\n");
        return shot;
    }

    /**
     * converts a ShotType to a SquareState
     * @param shot shot to convert
     * @return the converted value
     */
    private SquareState convertShotTypeToSquareState(ShotType shot) {
        switch (shot) {
            default:
                return SquareState.SHOTMISSED;
            case MISSED:
                return SquareState.SHOTMISSED;
            case HIT:
                return SquareState.SHOTHIT;
            case SUNK:
                return SquareState.SHIPSUNK;
            case ALLSUNK:
                return SquareState.SHIPSUNK;

        }
    }


    /**
     * return the grid of the selected player
     * @param playerNr id of the player
     * @return the grid of the player
     */
    public Grid getPlayerGrid(int playerNr) {
        return getPlayerByNr(playerNr).getGrid();
    }

    /**
     * removes al the ships from the player.
     * @param playerNr player to remove the ships for
     */
    public void removeAllShips(int playerNr) {
        //Get player by playerNr
        Player player = getPlayerByNr(playerNr);

        //Get ships of player with given playerNr
        for (Ship ship : player.getShips()) {
            //Remove ship
            ship.removeShip();
        }
    }

    /**
     * places all the ships for the player at pseudorandom positions.
     * It wil just take random positions and keep trying until they fit.
     *
     * @param playerNr player to place the ships for
     */
    public void placeShipsRandom(int playerNr) {
        Player player = getPlayerByNr(playerNr);
        for (Ship ship : player.getShips()) {
            boolean placed = false;
            do {
                Random random = new Random();
                boolean horizontal = random.nextBoolean();
                int x = 0;
                int y = 0;
                if (horizontal) {
                    x = random.nextInt(10 - ship.getLength());
                    y = random.nextInt(10);
                } else {
                    x = random.nextInt(10);
                    y = random.nextInt(10 - ship.getLength());
                }

                placed = placeShip(playerNr, ship.getShipType(), x, y, horizontal);
            } while (!placed);
        }
    }

    // Code for multiplayer

    public void setOpponent(String name) {
        System.out.println("Opponent: " + name);
        players[1] = new Player(name, 1 - players[0].getPlayerNr());
        System.out.println("Player: " + players[0].getName());
        try {
            seaBattleGame.getApplication().setOpponentName(players[0].getPlayerNr(), name);
        } catch (Exception e) {
            //ignore
        }
    }

    public boolean setGameReady() {
        if (players[0].isReady() && players[1].isReady()) {
            try {
                seaBattleGame.getApplication().enterPlaymode();
            } catch (Exception e) {
                //ignore
            }
            System.out.println("Both players ready");
            return true;
        }
        return false;
    }

    public void setOpponentReady() {
        players[1].setStateToReady();
        setGameReady();
    }

    public void shotFiredMultiplayer(int x, int y) {
        System.out.println("WE GOT SHOT");
        ShotType shot = players[0].fireShot(x, y);
        if (shot == ShotType.SUNK) {
            Cell cell = players[0].getGrid().getCell(x, y);
            Grid grid = players[0].getGrid();
            for (Ship ship : players[0].getShips()) {
                if (ship.isOnCell(cell)) {
                    Cell placement = ship.getPlacement();
                    int xAnchor = grid.getCellX(placement);
                    int yAnchor = grid.getCellY(placement);
                    int length = ship.getLength();
                    boolean horizontal = ship.isHorizontal();
                    client.sendResult(shot, xAnchor, yAnchor, length, horizontal);
                    break;
                }
            }
        } else {
            client.sendResult(shot);
        }
        seaBattleGame.fireShotMultiplayer(shot, x, y);
    }

}
