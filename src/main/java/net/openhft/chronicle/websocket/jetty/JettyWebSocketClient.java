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

import net.openhft.chronicle.core.io.SimpleCloseable;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

/*
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketClient extends SimpleCloseable implements MarshallableOut {
    private static final Logger LOGGER = LoggerFactory.getLogger(JettyWebSocketClient.class);

    private final WebSocketClient client;
    private final JettyWebSocketAdapter<MarshallableOut> adapter;
    private final boolean recordHistory;

    public JettyWebSocketClient(String uriString, BiConsumer<WireIn, MarshallableOut> parser) throws IOException {
        this(uriString, parser, false);
    }

    public JettyWebSocketClient(String uriString, BiConsumer<WireIn, MarshallableOut> parser, boolean recordHistory) throws IOException {
        this.recordHistory = recordHistory;
        URI uri = URI.create(uriString);

        client = new WebSocketClient();

        try {
            client.start();
            // The socket that receives events
            adapter = new JettyWebSocketAdapter<>(out -> out, parser);
            // Attempt Connect
            Future<Session> fut = client.connect(adapter, uri);
            // Wait for Connect
            Session session = fut.get();
            adapter.onWebSocketConnect(session);

        } catch (IOException ioe) {
            throw ioe;

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public DocumentContext writingDocument(boolean metaData) {
        assert !metaData;
        return adapter.writingDocument();
    }

    @Override
    public boolean recordHistory() {
        return recordHistory;
    }

    @Override
    protected void performClose() {
        try {
            client.stop();
        } catch (Exception e) {
            LOGGER.info("Error on close of " + client, e);
        }
    }
}
