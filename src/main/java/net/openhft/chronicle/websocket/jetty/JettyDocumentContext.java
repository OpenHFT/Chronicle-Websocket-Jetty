package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.JSONWire;
import net.openhft.chronicle.wire.Wire;

import java.util.function.Consumer;

/**
 * Created by peter on 22/04/16.
 */
public class JettyDocumentContext implements DocumentContext {
    private final Wire wire = new JSONWire(Bytes.allocateElasticDirect());
    private final int sourceId;
    private final Consumer<Wire> wireConsumer;

    public JettyDocumentContext(int sourceId, Consumer<Wire> wireConsumer) {
        this.sourceId = sourceId;
        this.wireConsumer = wireConsumer;
    }

    public void reset() {
        wire.clear();
    }

    @Override
    public long index() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMetaData() {
        return false;
    }

    @Override
    public void metaData(boolean metaData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPresent() {
        return wire.hasMore();
    }

    @Override
    public Wire wire() {
        return wire;
    }

    @Override
    public int sourceId() {
        return sourceId;
    }

    @Override
    public boolean isNotComplete() {
        throw new UnsupportedOperationException();
    }

    public void close() {
        wireConsumer.accept(wire);
    }
}
