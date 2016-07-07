package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.*;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

/**
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketServiceAdapter<R, S> extends WebSocketAdapter implements MarshallableIn, MarshallableOut {
    private static final Logger IN = LoggerFactory.getLogger(JettyWebSocketServiceAdapter.class.getName() + ".IN");
    private static final Logger OUT = LoggerFactory.getLogger(JettyWebSocketServiceAdapter.class.getName() + ".OUT");
    final ThreadLocal<JettyDocumentContext> readingDocumentTL = ThreadLocal.withInitial(() -> new JettyDocumentContext(0, w -> {
    }));
    final ThreadLocal<JettyDocumentContext> writingDocumentTL = ThreadLocal.withInitial(() -> new JettyDocumentContext(0, this::sendWireContents));
    private final MethodReader reader;

    public JettyWebSocketServiceAdapter(Class<R> responseClass, Function<R, S> serviceFactory) {
        R writer = methodWriter(responseClass);
        S service = serviceFactory.apply(writer);
        reader = this.methodReader(service);
    }

    @Override
    public boolean recordHistory() {
        return false;
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
    }

    public Wire getInWire() {
        Wire wire = readingDocumentTL.get().wire();
        wire.clear();
        return wire;
    }

    @Override
    public void onWebSocketText(String message) {
        if (IN.isDebugEnabled())
            IN.debug("message in - " + message);
        Wire wire = getInWire();
        wire.bytes().append8bit(message);
        while (reader.readOne()) {
        }
    }

    @Override
    public RemoteEndpoint getRemote() throws WebSocketException {
        RemoteEndpoint remote = super.getRemote();
        if (remote == null)
            throw new IORuntimeException("Not connected");
        return remote;
    }

    @Override
    public DocumentContext readingDocument() {
        return readingDocumentTL.get();
    }

    @Override
    public DocumentContext writingDocument() {
        JettyDocumentContext context = writingDocumentTL.get();
        context.reset();
        return context;
    }

    public void sendWireContents(Wire wire) {
        String strOut = wire.bytes().toString();
        try {
            RemoteEndpoint remote = getRemote();
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (remote) {
                remote.sendString(strOut);
            }
            if (OUT.isDebugEnabled())
                OUT.debug("message out - " + strOut);

        } catch (WebSocketException | IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
