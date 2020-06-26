package com.osfocus.www.basic.netty.chatroom;

import com.osfocus.www.basic.netty.chatroom.ClientFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

public class Client {
    private final static String SERVER = "localhost";
    private final static int PORT = 8088;
    private Channel c;

    private EventLoopGroup group = null;

    ClientFrame frame = null;

    public Client(ClientFrame frame) {
        group = new NioEventLoopGroup(1);

        Bootstrap b = new Bootstrap();

        try {
            ChannelFuture f = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ClientHandler(frame));
                        }
                    })
                    .connect(SERVER, PORT);

            f.sync();
            c = f.channel();
            System.out.println("Connected to server(" + SERVER + ").");
            this.frame = frame;
        } catch (InterruptedException e) {
            e.printStackTrace();
            group.shutdownGracefully();
        }
    }

    public void sendToServer(String msg) {
        if (c == null) {
            throw new NullPointerException();
        }

        c.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
    }

    public void shutdown() {
        try {
            if (c != null) {
                c.closeFuture().sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ClientHandler extends ChannelInboundHandlerAdapter {
        private final ClientFrame frame;

        public ClientHandler(ClientFrame frame) {
            this.frame = frame;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            ByteBuf buf = Unpooled.copiedBuffer("Hello".getBytes());
            ctx.writeAndFlush(buf);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = null;
            try {
                buf = (ByteBuf) msg;
                byte[] bytes = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), bytes);
                frame.setText(new String(bytes));
            } finally {
                if (buf != null) ReferenceCountUtil.release(buf);
            }
        }
    }
}
