package com.gunfight.net;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class WebServer {
    public static void start(int port, String directory) {
        HttpServer server = SimpleFileServer.createFileServer(
            new InetSocketAddress("0.0.0.0", port),
            Path.of(directory).toAbsolutePath(),
            SimpleFileServer.OutputLevel.VERBOSE
        );
        server.start();
        System.out.println("Web server started at http://0.0.0.0:" + port);
    }
}
