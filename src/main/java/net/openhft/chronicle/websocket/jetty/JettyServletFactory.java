package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;
import org.eclipse.jetty.websocket.servlet.*;

import java.util.function.BiConsumer;

public class JettyServletFactory extends WebSocketServlet implements WebSocketCreator {
    private final BiConsumer<WireIn, MarshallableOut> channel;

    public JettyServletFactory(BiConsumer<WireIn, MarshallableOut> channel) {
        this.channel = channel;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(this);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        return new JettyWebSocketAdapter(channel);
    }
}