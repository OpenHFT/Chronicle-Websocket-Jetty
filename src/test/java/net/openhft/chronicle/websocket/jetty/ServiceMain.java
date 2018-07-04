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

import net.openhft.chronicle.core.jlbh.JLBH;
import net.openhft.chronicle.core.jlbh.JLBHOptions;
import net.openhft.chronicle.core.jlbh.JLBHTask;
import net.openhft.chronicle.core.util.NanoSampler;
import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.WireIn;

import java.io.IOException;
import java.util.function.BiConsumer;

/*
 * Created by Peter Lawrey on 23/04/16.
 */
public class ServiceMain {
    // -XX:+UnlockCommercialFeatures    -XX:+FlightRecorder    -XX:StartFlightRecording=dumponexit=true,filename=ServiceMain.jfr,settings=profile2    -XX:+UnlockDiagnosticVMOptions    -XX:+DebugNonSafepoints
    public static void main(String[] args) throws IOException {
        JLBH jlbh = new JLBH(new JLBHOptions().runs(6)
                .iterations(50000)
                .throughput(20000)
                .accountForCoordinatedOmmission(false)
                .recordOSJitter(false)
                .jlbhTask(new JLBHTask() {
                    JettyWebSocketClient client1;

                    @Override
                    public void init(JLBH jlbh) {

                        try {

                            BiConsumer<WireIn, MarshallableOut> consumer = (wireIn,
                                                                            marshallableOut) ->
                                    jlbh.sampleNanos(System.nanoTime() - wireIn.getValueIn().int64());

                            client1 = new JettyWebSocketClient("ws://localhost:9090/echo/", consumer);
                        } catch (IOException e) {
                            throw new AssertionError(e);
                        }
                    }

                    @Override
                    public void run(long startTimeNS) {
                        client1.writeDocument(w -> w.writeEventName("echo").int64(System.nanoTime()));
                    }
                }));

        JettyWebSocketServer server = new JettyWebSocketServer(9090);
        NanoSampler probe = jlbh.addProbe("on server");
        server.addService("/echo/*", Echo.class, r -> new EchoImpl(r) {
            @Override
            public void echo(long time) {
                super.echo(System.nanoTime());
                probe.sampleNanos(System.nanoTime() - time);
            }
        });
        server.start();

        jlbh.start();

        server.close();
        System.exit(0);
    }
}
