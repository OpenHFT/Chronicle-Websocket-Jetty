package net.openhft.chronicle.websocket.jetty;

/**
 * Created by peter on 23/04/16.
 */
public class EchoImpl implements Echo {
    final Echo echo;

    public EchoImpl(Echo echo) {
        this.echo = echo;
    }

    public void echo(long time) {
        echo.echo(time);
    }
}
