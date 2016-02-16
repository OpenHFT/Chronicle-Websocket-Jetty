package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.VanillaWireParser;
import net.openhft.chronicle.wire.WireParser;
import net.openhft.chronicle.wire.WireType;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by peter.lawrey on 06/02/2016.
 */
public class EchoTest {
    @Test
    public void testEcho() throws IOException, InterruptedException {
        JettyWebSocketEchoServer server = new JettyWebSocketEchoServer(19091);
        BlockingQueue<String> q = new LinkedBlockingQueue<>();
        WireParser parser = new VanillaWireParser((s, v, o) -> q.add(s + " - " + v.text()));

        JettyWebSocketClient client = new JettyWebSocketClient("ws://localhost:19091/echo/", parser);
        client.marshallable(w -> w.writeEventName(() -> "echo").text("Hello World"));
        client.marshallable(w -> w.writeEventName(() -> "echo2").text("Hello World2"));

        assertEquals("echo - Hello World", q.poll(1, TimeUnit.SECONDS));
        assertEquals("echo2 - Hello World2", q.poll(1, TimeUnit.SECONDS));
        client.close();
        server.close();
    }

    @Test
    public void testEchoMarshallable() throws IOException, InterruptedException {
        FXPrice fxPrice1 = WireType.TEXT.fromString(
                "!net.openhft.chronicle.websocket.jetty.FXPrice {\n" +
                        "  bidprice: 1.2345,\n" +
                        "  offerprice: 1.2354,\n" +
                        "  pair: EURUSD,\n" +
                        "  size: 1000000,\n" +
                        "  level: 1,\n" +
                        "  exchangeName: RTRS\n" +
                        "}");
        FXPrice fxPrice2 = WireType.TEXT.fromString(
                "!net.openhft.chronicle.websocket.jetty.FXPrice {\n" +
                        "  bidprice: 1.235,\n" +
                        "  offerprice: 1.236,\n" +
                        "  pair: EURUSD,\n" +
                        "  size: 2000000,\n" +
                        "  level: 1,\n" +
                        "  exchangeName: RTRS\n" +
                        "}");

        JettyWebSocketEchoServer server = new JettyWebSocketEchoServer(19092);
        BlockingQueue<FXPrice> q = new LinkedBlockingQueue<>();
        WireParser parser = new VanillaWireParser((s, v, o) -> q.add(v.object(FXPrice.class)));

        JettyWebSocketClient client = new JettyWebSocketClient("ws://localhost:19092/echo/", parser);
        client.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice1));
        client.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice2));

        assertEquals(fxPrice1, q.poll(1, TimeUnit.MINUTES));
        assertEquals(fxPrice2, q.poll(1, TimeUnit.MINUTES));
        client.close();
        server.close();
    }

    @Test
    @Ignore("Long running")
    public void perfTestLatency() throws IOException, InterruptedException {
        FXPrice fxPrice = WireType.TEXT.fromString(
                "!net.openhft.chronicle.websocket.jetty.FXPrice {\n" +
                        "  bidprice: 1.2345,\n" +
                        "  offerprice: 1.2354,\n" +
                        "  pair: EURUSD,\n" +
                        "  size: 1000000,\n" +
                        "  level: 1,\n" +
                        "  exchangeName: RTRS\n" +
                        "}");

        JettyWebSocketEchoServer server = new JettyWebSocketEchoServer(9090);
        BlockingQueue<FXPrice> q = new LinkedBlockingQueue<>();
        WireParser<MarshallableOut> parser = new VanillaWireParser<>((s, v, o) -> q.add(v.object(FXPrice.class)));

        JettyWebSocketClient client = new JettyWebSocketClient("ws://localhost:9090/echo/", parser);

        int runs = 50000;
        for (int t = 0; t < 4; t++) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < runs; i++) {
                client.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice));
                q.take();
            }
            long time = System.currentTimeMillis() - start;
            System.out.printf("RTT took an average of %,d us%n", time * 1000 / runs);
        }

        client.close();
        server.close();
    }


    @Test
    @Ignore("Long running")
    public void perfTestThroughput() throws IOException, InterruptedException {
        FXPrice fxPrice = WireType.TEXT.fromString(
                "!net.openhft.chronicle.websocket.jetty.FXPrice {\n" +
                        "  bidprice: 1.2345,\n" +
                        "  offerprice: 1.2354,\n" +
                        "  pair: EURUSD,\n" +
                        "  size: 1000000,\n" +
                        "  level: 1,\n" +
                        "  exchangeName: RTRS\n" +
                        "}");

        JettyWebSocketEchoServer server = new JettyWebSocketEchoServer(9090);
        BlockingQueue<FXPrice> q = new LinkedBlockingQueue<>();
        WireParser<MarshallableOut> parser = new VanillaWireParser<>((s, v, o) -> q.add(v.object(FXPrice.class)));

        JettyWebSocketClient client1 = new JettyWebSocketClient("ws://localhost:9090/echo/", parser);
        JettyWebSocketClient client2 = new JettyWebSocketClient("ws://localhost:9090/echo/", parser);

        int runs = 200000;
        for (int t = 0; t < 4; t++) {
            long start = System.currentTimeMillis();
            int count = 0;
            for (int i = 0; i < runs; i += 2) {
                client1.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice));
                client2.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice));
                while (q.poll() != null)
                    count++;
            }
            for (; count < runs; count++) {
                q.take();
            }
            long time = System.currentTimeMillis() - start;
            System.out.printf("Throughput of %,d messages/second%n", runs * 1000 / time);
        }

        client1.close();
        client2.close();
        server.close();
    }
}
