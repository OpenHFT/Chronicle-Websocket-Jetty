package net.openhft.chronicle.websocket.jetty;

import net.openhft.chronicle.wire.MarshallableOut;
import net.openhft.chronicle.wire.VanillaWireParser;
import net.openhft.chronicle.wire.WireParser;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class JettyWebSocketServerTest {
    private static final String BAR = "bar";
    private static final String FOO = "foo";

    @Test
    public void registersServletsWithDifferentNames() throws IOException, InterruptedException {
        int port = 19999;
        JettyWebSocketServer server = new JettyWebSocketServer(port);
        String foo = "/" + FOO;
        server.addService(foo + "/*", Service.class, Foo::new);
        String bar = "/" + BAR;
        server.addService(bar + "/*", Service.class, Bar::new);
        server.start();

        {
            BlockingQueue<CharSequence> q1 = new LinkedBlockingQueue<>();
            WireParser<MarshallableOut> parser1 = new VanillaWireParser<>((s, v, o) -> q1.add(v.text()));
            JettyWebSocketClient client1 = new JettyWebSocketClient("ws://localhost:" + port + foo + "/", parser1);
            client1.writeDocument(w -> w.writeEventName("serve").text("me"));
            assertEquals(FOO, q1.poll(1, TimeUnit.SECONDS));
            client1.close();
        }

        {
            BlockingQueue<CharSequence> q2 = new LinkedBlockingQueue<>();
            WireParser<MarshallableOut> parser2 = new VanillaWireParser<>((s, v, o) -> q2.add(v.text()));
            JettyWebSocketClient client2 = new JettyWebSocketClient("ws://localhost:" + port + bar + "/", parser2);
            client2.writeDocument(w -> w.writeEventName("serve").text("me"));
            assertEquals(BAR, q2.poll(1, TimeUnit.SECONDS));
            client2.close();
        }

        server.close();
    }

    @FunctionalInterface
    private interface Service {
        void serve(String request);
    }

    private static final class Foo implements Service {
        private final Service service;

        private Foo(Service service) {
            this.service = service;
        }

        @Override
        public void serve(String request) {
            service.serve(FOO);
        }
    }

    private static final class Bar implements Service {
        private final Service service;

        private Bar(Service service) {
            this.service = service;
        }

        @Override
        public void serve(String request) {
            service.serve(BAR);
        }
    }
}
