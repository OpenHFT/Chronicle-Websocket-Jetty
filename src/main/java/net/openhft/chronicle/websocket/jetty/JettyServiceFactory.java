package net.openhft.chronicle.websocket.jetty;

import org.eclipse.jetty.websocket.servlet.*;

import java.util.function.Function;

/**
 * Created by peter on 23/04/16.
 */
public class JettyServiceFactory<R, S> extends WebSocketServlet implements WebSocketCreator {
    private final Class<R> responseClass;
    private final Function<R, S> serviceFactory;

    public JettyServiceFactory(Class<R> responseClass, Function<R, S> serviceFactory) {
        this.responseClass = responseClass;
        this.serviceFactory = serviceFactory;
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.setCreator(this);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        return new JettyWebSocketServiceAdapter<>(responseClass, serviceFactory);
    }
}
