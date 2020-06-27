package com.osfocus.tank.net;

import com.osfocus.tank.Tank;
import com.osfocus.tank.TankFrame;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.TimeUnit;

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
                            pipeline.addLast(new TankJoinMsgEncoder())
                                    .addLast(new TankJoinMsgDecoder())
                                    .addLast(new ClientHandler());
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

            TimeUnit.SECONDS.sleep(3);
            channel.writeAndFlush(TankFrame.INSTANCE.getMainTank());

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
        ctx.writeAndFlush(new TankJoinMsg(TankFrame.INSTANCE.getMainTank()));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        TankJoinMsg tankJoinMsg = (TankJoinMsg) msg;
        if (tankJoinMsg.id.equals(TankFrame.INSTANCE.getMainTank().getId())) return;

        Tank t = new Tank(tankJoinMsg);
        TankFrame.INSTANCE.addTank(t);
    }
}
