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

import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;

import java.io.IOException;
import java.util.function.BiConsumer;

/*
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
        marshallableOut.writeDocument(wireIn, (out, in) -> {
            out.wireOut().bytes().write(in.bytes());
        });
    }

    public void close() throws IOException {
        server.close();
    }
}
