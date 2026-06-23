package com.gunfight.net;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
    private static final Logger log = LoggerFactory.getLogger(WebServer.class);
    public static void start(int port, String directory) {
        HttpServer server = SimpleFileServer.createFileServer(
            new InetSocketAddress("0.0.0.0", port),
            Path.of(directory).toAbsolutePath(),
            SimpleFileServer.OutputLevel.VERBOSE
        );
        server.start();
        log.info("Web server started at http://0.0.0.0:{}", port);
    }
}
