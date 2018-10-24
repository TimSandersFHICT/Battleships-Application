package seabattleserver;

// Class containing the logic for the server

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.*;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;

@ServerEndpoint(value = "/seabattle/")
public class ServerLogic {

    // List to store sessions
    private static ArrayList<Session> sessions = new ArrayList<>();
    // Array to store players
    private static String[] players = new String[2];

    // Method called when a new connection is opened
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[Server] New Connection, session ID: " + session.getId());
        // Check if there already are 2 sessions (one for each player)
        if(sessions.size() == 2) {
            // Deny new session
            try {
                session.close();
                System.out.println("[Server] Connection denied, max amount of sessions reached");
            } catch (Exception E) {
                System.out.println("[Server] Connection denied, max amount of sessions reached");
            }
            return;
        }
        // New session allowed, add to sessions
        sessions.add(session);
        System.out.println("[Server] Connection established! No. of sessions is now: " + sessions.size());
    }

    // Method called when the server receives a message
    @OnMessage
    public void onMessage(String message,Session session) {
        JsonObject json = new JsonParser().parse(message).getAsJsonObject();
        if (keyInJson(json, "Register")) {
            //Register
            registerPlayer(session, json.get("Register").toString());
        } else if (keyInJson(json, "Ready")) {
            //Ready
            ReadyPlayer(session);
        } else if (keyInJson(json, "Fire")) {
            //Fire shot location
            SendShotLocation(json, session);
        } else if (keyInJson(json, "ShotType")) {
            //Fire shot type
            SendShotType(json, session);
        }
    }

    // Method used for checking if a JSON object has a certain key
    private boolean keyInJson(JsonObject json, String key) {
        try {
            return json.has(key);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    private void SendShotType(JsonObject json, Session session) {
        Session other = getOtherSession(session);
        try {
            other.getBasicRemote().sendText(json.toString());
        } catch (Exception E) {
            System.out.println("[ERROR] Could not send shot type");
        }
    }

    private void SendShotLocation(JsonObject json, Session session) {
        Session other = getOtherSession(session);
        try {
            other.getBasicRemote().sendText(json.toString());
        } catch (Exception E) {
            System.out.println("[ERROR] Could not send shot location");
        }
    }

    private void ReadyPlayer(Session session) {
        Session other = getOtherSession(session);

        JsonObject json = new JsonObject();
        json.addProperty("Ready", true);

        try {
            other.getBasicRemote().sendText(json.toString());
        } catch (Exception E) {
            System.out.println("[ERROR] Could not send player ready");
        }
    }

    private void sendEnemyName() {
        int i = sessions.size() - 1;
        for (Session s : sessions) {
            try {
                JsonObject json = new JsonObject();
                json.addProperty("EnemyName", players[i]);
                s.getBasicRemote().sendText(json.toString());
            } catch (IOException e) {
                System.out.println("[ERROR] Could not send enemy name");
            }
            i--;
        }
    }

    private void registerPlayer(Session session, String name) {
        int value = -1;
        if (sessions.size() == 2) {
            if (!players[0].equals(name)) {
                value = 1;
                players[1] = name;
            }
        } else if (sessions.size() == 1) {
            value = 0;
            players[0] = name;
        }
        JsonObject json = new JsonObject();
        json.addProperty("Register", value);

        try {
            session.getBasicRemote().sendText(json.toString());
        } catch (IOException e) {
            System.out.println("[ERROR] Could not send register player");
        }

        if (sessions.size() == 2) {
            //set enemy names
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendEnemyName();
        }
    }

    private Session getOtherSession(Session session) {
        for (Session s : sessions) {
            if (!s.equals(session)) {
                return s;
            }
        }
        return null;
    }

    // Method called when a connection/session is closed
    @OnClose
    public void onClose(CloseReason reason, Session session) {
        System.out.println("[Server] Session " + session.getId() + " CLOSED: " + reason);
        sessions.remove(session);
    }

    // Method called when an error occurs
    @OnError
    public void onError(Throwable cause, Session session) {
        System.out.println("[Server] Session " + session.getId() + "[ERROR] ");
        cause.printStackTrace(System.err);
    }

}
