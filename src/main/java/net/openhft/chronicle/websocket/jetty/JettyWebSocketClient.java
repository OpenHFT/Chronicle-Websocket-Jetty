package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.core.io.Closeable;
import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.ValueOut;
import net.openhft.chronicle.wire.WireIn;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

/**
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketClient implements MarshallableOut, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyWebSocketClient.class);

    private final WebSocketClient client;
    private final JettyWebSocketAdapter adapter;

    public JettyWebSocketClient(String uriString, BiConsumer<WireIn, MarshallableOut> parser) throws IOException {
        URI uri = URI.create(uriString);

        client = new WebSocketClient();

        try {
            client.start();
            // The socket that receives events
            adapter = new JettyWebSocketAdapter(parser);
            // Attempt Connect
            Future<Session> fut = client.connect(adapter, uri);
            // Wait for Connect
            Session session = fut.get();
            adapter.onWebSocketConnect(session);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T> void marshallable(T t, BiConsumer<ValueOut, T> writer) {
        adapter.marshallable(t, writer);
    }

    @Override
    public void close() {
        try {
            client.stop();
        } catch (Exception e) {
            LOGGER.info("Error on close of " + client, e);
        }
    }
}
