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
    private Channel channel;

    private EventLoopGroup group = null;

    public Client() {

    }

    public void connect() {
        group = new NioEventLoopGroup(1);

        Bootstrap b = new Bootstrap();

        try {
            ChannelFuture f = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ClientHandler());
                        }
                    })
                    .connect(SERVER, PORT);

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (!future.isSuccess()) {
                        System.out.println("not connected!");
                    } else {
                        System.out.println("connected!");
                        channel = future.channel();
                    }
                }
            });

            f.sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void sendToServer(String msg) {
        if (channel == null) {
            throw new NullPointerException();
        }
        channel.writeAndFlush(Unpooled.copiedBuffer(msg.getBytes()));
    }

    public void closeConnect() {
        this.sendToServer("__bye__");
    }

    public void shutdown() {
        try {
            if (channel != null) {
                channel.closeFuture().sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
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
            ClientFrame.INSTANCE.setText(new String(bytes));
        } finally {
            if (buf != null) ReferenceCountUtil.release(buf);
        }
    }
}
