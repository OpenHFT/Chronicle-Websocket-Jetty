package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.wire.VanillaWireParser;
import net.openhft.chronicle.wire.WireParser;
import net.openhft.chronicle.wire.WireType;
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
        JettyWebSocketEchoServer server = new JettyWebSocketEchoServer(9090);
        BlockingQueue<String> q = new LinkedBlockingQueue<>();
        WireParser parser = new VanillaWireParser((s, v, o) -> q.add(s + " - " + v.text()));

        JettyWebSocketClient client = new JettyWebSocketClient("ws://localhost:9090/echo/", parser);
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

        JettyWebSocketEchoServer server = new JettyWebSocketEchoServer(9090);
        BlockingQueue<FXPrice> q = new LinkedBlockingQueue<>();
        WireParser parser = new VanillaWireParser((s, v, o) -> q.add(v.object(FXPrice.class)));

        JettyWebSocketClient client = new JettyWebSocketClient("ws://localhost:9090/echo/", parser);
        client.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice1));
        client.marshallable(w -> w.writeEventName(() -> "price").marshallable(fxPrice2));

        assertEquals(fxPrice1, q.poll(1, TimeUnit.MINUTES));
        assertEquals(fxPrice2, q.poll(1, TimeUnit.MINUTES));
        client.close();
        server.close();
    }
}
