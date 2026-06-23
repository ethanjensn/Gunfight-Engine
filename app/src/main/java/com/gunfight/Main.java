package com.gunfight;

import com.gunfight.net.GameServer;
import com.gunfight.net.WebServer;

public class Main {
    
    public String getGreeting() {
        return "Hello from Gunfight Engine";
    }
    
    public static void main(String[] args) {
        WebServer.start(3000, "../web-client");

        GameServer server = new GameServer(8080);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }
}
