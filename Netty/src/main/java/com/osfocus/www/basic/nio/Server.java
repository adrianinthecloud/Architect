package com.osfocus.www.basic.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    public static void main(String[] args) {
        ServerSocketChannel ssc = null;
        try {
            ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress("127.0.0.1", 8081));
            ssc.configureBlocking(false);
            Selector selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    handle(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                ssc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handle(SelectionKey key) {
        if (key.isAcceptable()) {
            ServerSocketChannel ssc = null;
            try {
                ssc = (ServerSocketChannel) key.channel();
                SocketChannel sc = (SocketChannel) ssc.accept();
                sc.configureBlocking(false);
                InetSocketAddress client = (InetSocketAddress) sc.getRemoteAddress();

                System.out.println("Established connection with client-" + client.getHostString());

                Selector selector = key.selector();

                sc.register(selector, SelectionKey.OP_READ);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (key.isReadable()) {
            SocketChannel sc = null;
            try {
                sc = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(512);
                buffer.clear();
                int len = sc.read(buffer);
                if (len != -1) {
                    System.out.println("Message from Client: " + new String(buffer.array(), 0, len));
                }
                buffer.put(" is received".getBytes());
                sc.write(buffer.flip());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (sc != null) {
                    try {
                        sc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
