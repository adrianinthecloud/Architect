package com.osfocus.www.basic.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {
        int numOfThreads = 30;
        Thread[] threads = new Thread[numOfThreads];

        for (int i = 0; i < numOfThreads; i++) {
            threads[i] = new Thread(() -> {
                sendPackage("Package-"+Thread.currentThread().getName());
            },"t-"+i);
        }

        Arrays.asList(threads).forEach(t->t.start());

        Arrays.asList(threads).forEach(t-> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Finished sending packages.");
    }

    public static void sendPackage(String str) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8081);
            socket.getOutputStream().write(str.getBytes());
            socket.getOutputStream().flush();

            System.out.println("Finished writing, waiting for response.");

            try (InputStream is = socket.getInputStream();) {
                byte[] byteBuffer = new byte[1024];
                int len = is.read(byteBuffer, 0, byteBuffer.length);
                System.out.println("input = " + new String(byteBuffer, 0, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
