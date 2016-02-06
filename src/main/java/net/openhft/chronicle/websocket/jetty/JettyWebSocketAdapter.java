package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketAdapter<T> extends WebSocketAdapter implements MarshallableOut {
    final ThreadLocal<Wire> inWireTL = ThreadLocal.withInitial(() -> new JSONWire(Bytes.allocateElasticDirect()));
    final ThreadLocal<Wire> outWireTL = ThreadLocal.withInitial(() -> new JSONWire(Bytes.allocateElasticDirect()));
    private final T wrapper;
    private final BiConsumer<WireIn, T> channel;

    public JettyWebSocketAdapter(Function<MarshallableOut, T> outWrapper, BiConsumer<WireIn, T> channel) {
        this.channel = channel;
        this.wrapper = outWrapper.apply(this);
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
    }

    public Wire getInWire() {
        Wire wire = inWireTL.get();
        wire.clear();
        return wire;
    }

    @Override
    public void onWebSocketText(String message) {
        Wire wire = getInWire();
        wire.bytes().append8bit(message);
        channel.accept(wire, wrapper);
    }

    public Wire getOutWire() {
        Wire wire = outWireTL.get();
        wire.clear();
        return wire;
    }

    @Override
    public void send(WireKey key, WriteValue value) {
        Wire wire = getOutWire();
        value.writeValue(wire.write(key));
        try {
            getRemote().sendString(wire.bytes().toString());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

    @Override
    public <T> void marshallable(T t, BiConsumer<ValueOut, T> writer) {
        Wire wire = getOutWire();
        writer.accept(wire.getValueOut(), t);
        try {
            getRemote().sendString(wire.bytes().toString());
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }
}
