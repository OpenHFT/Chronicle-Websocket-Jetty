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

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/*
 * Created by Peter Lawrey on 23/04/16.
 */
public class ServiceThroughputMain {
    // -XX:+UnlockCommercialFeatures    -XX:+FlightRecorder    -XX:StartFlightRecording=dumponexit=true,filename=ServiceThroughputMain.jfr,settings=profile2    -XX:+UnlockDiagnosticVMOptions    -XX:+DebugNonSafepoints
    public static void main(String[] args) throws IOException {
        JettyWebSocketServer server = new JettyWebSocketServer(9090);
        server.addService("/echo/*", Echo.class, EchoImpl::new);
        server.start();

        AtomicInteger count = new AtomicInteger();

        BiConsumer<WireIn, MarshallableOut> consumer = (wireIn, marshallableOut) -> count.incrementAndGet();

        JettyWebSocketClient client1 = new JettyWebSocketClient("ws://localhost:9090/echo/", consumer);
        int runs = 1000_000;
        long start = System.nanoTime();
        for (int i = 0; i < runs; i++) {
            client1.writeDocument(w -> w.writeEventName("echo").int64(System.nanoTime()));
            if (count.get() + 1000 < i)
                Jvm.pause(1);
        }
        while (count.get() < runs) {
            System.out.println(count);
            Jvm.pause(1000);
        }
        long time = System.nanoTime() - start;
        System.out.println("Throughput " + (long) (runs * 1e9 / time) + " messages per second.");
        server.close();
        System.exit(0);
    }
}
