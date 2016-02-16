package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by peter.lawrey on 04/02/2016.
 */
public class JettyWebSocketServer {
    private final Server server;
    private final ServletContextHandler context;

    public JettyWebSocketServer(String host, int port) {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        if (host != null)
            connector.setHost(host);
        connector.setPort(port);
        server.addConnector(connector);

        // Setup the basic application "context" for this application at "/"
        // This is also known as the handler tree (in jetty speak)
        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
    }

    public JettyWebSocketServer(int port) {
        this(null, port);
    }

    public <T> void addServlet(String path, Function<MarshallableOut, T> outWrapper, BiConsumer<WireIn, T> channel) {
        // Add a websocket to a specific path spec
        ServletHolder holderEvents = new ServletHolder("ws-events", new JettyServletFactory<T>(outWrapper, channel));
        context.addServlet(holderEvents, path);
    }

    public void start() {
        try {
            server.start();
        } catch (Exception e) {
            throw new IORuntimeException(e);
        }
    }

    public void close() throws IOException {
        try {
            server.stop();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
