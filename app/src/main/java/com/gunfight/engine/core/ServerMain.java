package com.gunfight.engine.core;

public class ServerMain {

    public static void main(String[] args) {
        System.out.println("Starting Gunfight Server...");

        Engine engine = new Engine();
        engine.run();
    }
}
