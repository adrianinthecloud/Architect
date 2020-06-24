package com.osfocus.www.basic.bio;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("127.0.0.1", 8088);
        s.getOutputStream().write("HelloServer".getBytes());
        s.getOutputStream().flush();

        System.out.println("Finish sending, waiting for response.");
        byte[] bytes = new byte[1024];
        int len = s.getInputStream().read(bytes);
        System.out.println(new String(bytes, 0, len));
        s.close();
    }
}
