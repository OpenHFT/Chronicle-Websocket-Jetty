/*
 * Copyright 2016 higherfrequencytrading.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
    private long index = 0;

    public JettyDocumentContext(int sourceId, Consumer<Wire> wireConsumer) {
        this.sourceId = sourceId;
        this.wireConsumer = wireConsumer;
    }

    public void reset() {
        wire.clear();
    }

    @Override
    public long index() {
        return index;
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
        index++;
    }
}
