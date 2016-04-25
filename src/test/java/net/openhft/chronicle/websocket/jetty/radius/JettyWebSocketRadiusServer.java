package net.openhft.chronicle.websocket.jetty.radius;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.websocket.jetty.JettyWebSocketServer;

/**
 * Created by peter.lawrey on 06/02/2016.
 */
public class JettyWebSocketRadiusServer {
    private final JettyWebSocketServer server;

    public JettyWebSocketRadiusServer(int port) {
        this.server = new JettyWebSocketServer(port);
        server.addService("/*", Radius.class, RadiusPublisher::new);
        server.start();
    }

    static class RadiusPublisher implements IRadiusPublisher{
        private Radius radius;

        RadiusPublisher(Radius radius) {
            this.radius = radius;
            System.out.println("New connection");
        }

        public void start(String s) {
            new Thread(() -> {
                for(int i=1;i<100;i++) {
                    radius.radius(i);
                    System.out.println("Message sent " + i);
                    Jvm.pause(1000);

                }
            }).start();
        }
    }

    public static void main(String[] args) {
        new JettyWebSocketRadiusServer(7000);
    }
}
