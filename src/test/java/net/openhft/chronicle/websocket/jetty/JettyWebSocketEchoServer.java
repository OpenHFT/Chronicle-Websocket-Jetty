package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;

import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketEchoServer implements BiConsumer<WireIn, MarshallableOut> {
    private final JettyWebSocketServer server;

    public JettyWebSocketEchoServer(int port) {
        this.server = new JettyWebSocketServer(port);
        server.addServlet("/echo/*", out -> out, this);
        server.start();
    }

    @Override
    public void accept(WireIn wireIn, MarshallableOut marshallableOut) {
        marshallableOut.marshallable(wireIn, (out, in) -> {
//            in.copyTo(out.wireOut());
            out.wireOut().bytes().write(in.bytes());
        });
    }

    public void close() throws IOException {
        server.close();
    }
}
