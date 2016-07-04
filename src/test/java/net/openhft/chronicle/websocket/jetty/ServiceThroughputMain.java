package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.VanillaWireParser;
import net.openhft.chronicle.wire.WireParser;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by peter on 23/04/16.
 */
public class ServiceThroughputMain {
    // -XX:+UnlockCommercialFeatures    -XX:+FlightRecorder    -XX:StartFlightRecording=dumponexit=true,filename=ServiceThroughputMain.jfr,settings=profile2    -XX:+UnlockDiagnosticVMOptions    -XX:+DebugNonSafepoints
    public static void main(String[] args) throws IOException {
        JettyWebSocketServer server = new JettyWebSocketServer(9090);
        server.addService("/echo/*", Echo.class, EchoImpl::new);
        server.start();

        AtomicInteger count = new AtomicInteger();
        WireParser<MarshallableOut> parser = new VanillaWireParser<>((s, v, o) -> count.incrementAndGet());
        JettyWebSocketClient client1 = new JettyWebSocketClient("ws://localhost:9090/echo/", parser);
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
