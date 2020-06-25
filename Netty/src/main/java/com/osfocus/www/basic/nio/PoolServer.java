package com.osfocus.www.basic.nio;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPoolServer {
    ExecutorService pool = Executors.newFixedThreadPool(50);

    private Selector selector;

    public static void main(String[] args) {
        MyPoolServer server = new MyPoolServer();
        try {
            server.initServer(8081);
            server.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initServer(int port) throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();

        ssc.configureBlocking(false);

        ssc.socket().bind(new InetSocketAddress(port));

        this.selector = Selector.open();

        ssc.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server started successfully.");
    }

    public void listen() throws IOException {
        while (true) {
            selector.select();
            Iterator it = this.selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                it.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel channel = server.accept();
                    channel.configureBlocking(false);

                    channel.register(this.selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));
                    pool.execute(new MyThreadHandlerChannel(key));
                }
            }
        }
    }
}

class MyThreadHandlerChannel extends Thread {
    private SelectionKey key;
    MyThreadHandlerChannel(SelectionKey key) {
        this.key = key;
    }

    @Override
    public void run() {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int size = 0;
            while ((size = channel.read(buffer)) > 0) {
                buffer.flip();
                baos.write(buffer.array(), 0, size);
                buffer.clear();
            }
            baos.close();
            byte[] content = baos.toByteArray();
            ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
            writeBuf.put(content);
            writeBuf.flip();
            channel.write(writeBuf);
            if (size == -1) {
                channel.close();
            } else {
                key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                key.selector().wakeup();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
