package com.osfocus.www.basic.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(new InetSocketAddress("127.0.0.1", 8088));

            while (true) {
                Socket s = ss.accept();

                new Thread(() -> {
                    handle(s);
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handle(Socket s) {
        System.out.println("Inside handle");
        byte[] byteBuffer = new byte[1024];
        try (InputStream is = s.getInputStream();) {
            int len = is.read(byteBuffer);
            System.out.println(new String(byteBuffer, 0, len));
            s.getOutputStream().write("Welcome to the server.".getBytes());
            s.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
