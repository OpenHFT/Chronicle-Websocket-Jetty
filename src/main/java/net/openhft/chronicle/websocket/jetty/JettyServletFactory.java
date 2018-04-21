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
        return new JettyWebSocketAdapter<>(outWrapper, channel);
    }
}