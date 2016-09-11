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
