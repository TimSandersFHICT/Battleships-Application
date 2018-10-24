package seabattleserver;

import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerContainer;

// Class that starts the server

public class Server {

    // Port used by the server
    public static final int PORT = 8096;

    public static void main(String[] args){
        System.out.println("Starting server...");
        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(PORT);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        try{
            ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
            container.addEndpoint(ServerLogic.class);
            server.start();
            server.join();
        }
        catch(Throwable t){
            t.printStackTrace(System.err);
        }
    }
}
