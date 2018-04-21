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

package net.openhft.chronicle.websocket.jetty.radius;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.websocket.jetty.JettyWebSocketServer;

/*
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketRadiusServer {
    private final JettyWebSocketServer server;

    public JettyWebSocketRadiusServer(int port) {
        this.server = new JettyWebSocketServer(port);
        server.addService("/*", Radius.class, RadiusPublisher::new);
        server.start();
    }

    public static void main(String[] args) {
        new JettyWebSocketRadiusServer(7000);
    }

    static class RadiusPublisher implements IRadiusPublisher {
        private Radius radius;

        RadiusPublisher(Radius radius) {
            this.radius = radius;
            System.out.println("New connection");
        }

        @Override
        public void start(String s) {
            new Thread(() -> {
                for (int i = 1; i < 100; i++) {
                    radius.radius(i);
                    System.out.println("Message sent " + i);
                    Jvm.pause(1000);

                }
            }).start();
        }
    }
}
