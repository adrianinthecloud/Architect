package com.osfocus.www.basic.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("127.0.0.1", 8081);
            socket.getOutputStream().write("Hello Server".getBytes());
            socket.getOutputStream().flush();

            System.out.println("Finished writing, waiting for response.");

            try (InputStream is = socket.getInputStream();) {
                byte[] byteBuffer = new byte[1024];
                System.out.println("Byte len = " + byteBuffer.length);
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
