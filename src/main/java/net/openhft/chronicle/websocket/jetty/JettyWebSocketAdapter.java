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
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.*;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Function;

/*
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketAdapter<T> extends WebSocketAdapter implements MarshallableOut {
    private static final Logger IN = LoggerFactory.getLogger(JettyWebSocketAdapter.class.getName() + ".IN");
    private static final Logger OUT = LoggerFactory.getLogger(JettyWebSocketAdapter.class.getName() + ".OUT");
    final ThreadLocal<Wire> inWireTL = ThreadLocal.withInitial(() -> new JSONWire(Bytes.allocateElasticDirect()));
    final ThreadLocal<JettyDocumentContext> writingDocumentTL = ThreadLocal.withInitial(() -> new JettyDocumentContext(0, this::sendWireContents));
    private final T wrapper;
    private final BiConsumer<WireIn, T> channel;
    private final int sourceId;

    public JettyWebSocketAdapter(Function<MarshallableOut, T> outWrapper, BiConsumer<WireIn, T> channel) {
        this(outWrapper, channel, 0);
    }

    public JettyWebSocketAdapter(Function<MarshallableOut, T> outWrapper, BiConsumer<WireIn, T> channel, int sourceId) {
        this.sourceId = sourceId;
        this.channel = channel;
        this.wrapper = outWrapper.apply(this);
    }

    @Override
    public boolean recordHistory() {
        return sourceId != 0;
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
        if (IN.isDebugEnabled())
            IN.debug("message in - " + message);
        Wire wire = getInWire();
        wire.bytes().append8bit(message);
        channel.accept(wire, wrapper);
    }

    @Override
    public RemoteEndpoint getRemote() throws WebSocketException {
        RemoteEndpoint remote = super.getRemote();
        if (remote == null)
            throw new IORuntimeException("Not connected");
        return remote;
    }

    @Override
    public DocumentContext writingDocument(boolean metaData) {
        assert !metaData;
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
