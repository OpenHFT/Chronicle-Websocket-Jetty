package net.openhft.chronicle.websocket.jetty;

public class HelloWorldImpl implements HelloWorld {
    private final HelloReplier replier;

    public HelloWorldImpl(HelloReplier replier) {
        this.replier = replier;
    }

    @Override
    public void hello(String name) {
        System.out.println("hello(" + name + ")");
        replier.reply("Hello " + name);
    }
}
