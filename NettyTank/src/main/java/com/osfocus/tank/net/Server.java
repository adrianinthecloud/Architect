package com.osfocus.tank.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

import static io.netty.channel.ChannelOption.TCP_NODELAY;

public class Server {
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void serverStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);

        ServerBootstrap b = new ServerBootstrap();

        try {
            ChannelFuture f = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY,true) // disable Nagle's algorithm
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new TankJoinMsgEncoder())
                                    .addLast(new TankJoinMsgDecoder())
                                    .addLast(new ServerChildHandler());
                        }
                    })
                    .bind(8088);
            f.sync();

            ServerFrame.INSTANCE.updateServerMsg("Server started.");
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class ServerChildHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ServerFrame.INSTANCE.updateClientMsg(ctx.channel().id() + " connected");
            clients.add(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            try {
//                TankJoinMsg tankmsg = (TankJoinMsg) msg;
//                System.out.println("msg = " + tankmsg);
//            } finally {
//                ReferenceCountUtil.release(msg);
//            }
            ServerFrame.INSTANCE.updateClientMsg(ctx.channel().id() + " sent message: " + msg);
            clients.writeAndFlush((TankJoinMsg) msg);
//            ctx.writeAndFlush(Unpooled.copiedBuffer("test".getBytes()));

//            ByteBuf buf = null;
//            int oldCnt = 0;
//            try {
//                buf = (ByteBuf) msg;
//                byte[] bytes = new byte[buf.readableBytes()];
//                oldCnt = buf.refCnt();
//                buf.getBytes(buf.readerIndex(), bytes);
//                System.out.println("Client-" + ctx.channel().remoteAddress() + ": " + new String(bytes));
//                String s = new String(bytes);
//                String sendMsg = ctx.channel().remoteAddress() + ": " + s;
//                if (s.equalsIgnoreCase("__bye__")) {
//                    System.out.println(ctx.channel().remoteAddress() + " requested to close connection.");
//                    clients.remove(ctx.channel());
//                    ctx.close();
//                } else {
//                    clients.writeAndFlush(Unpooled.copiedBuffer(sendMsg.getBytes()));
//                    ServerFrame.INSTANCE.updateClientMsg(sendMsg);
//                }
//            } finally {
//                if (buf != null && buf.refCnt() == oldCnt+1) {
//                    ReferenceCountUtil.release(buf);
//                }
//            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();

            Server.clients.remove(ctx.channel());
            ctx.close();
        }
    }
}
