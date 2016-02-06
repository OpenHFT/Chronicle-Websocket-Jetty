package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;
import org.eclipse.jetty.websocket.servlet.*;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class JettyServletFactory<T> extends WebSocketServlet implements WebSocketCreator {
    private final Function<MarshallableOut, T> outWrapper;
    private final BiConsumer<WireIn, T> channel;

    public JettyServletFactory(Function<MarshallableOut, T> outWrapper, BiConsumer<WireIn, T> channel) {
        this.outWrapper = outWrapper;
        this.channel = channel;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(this);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        return new JettyWebSocketAdapter<T>(outWrapper, channel);
    }
}