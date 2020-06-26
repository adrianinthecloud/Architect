package com.osfocus.www.basic.netty.chatroom;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server {
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);

        ServerBootstrap b = new ServerBootstrap();

        try {
            ChannelFuture f = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new ServerChildHandler());
                        }
                    })
                    .bind(8088);
            f.sync();

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
            clients.add(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = null;
            int oldCnt = 0;
            try {
                buf = (ByteBuf) msg;
                byte[] bytes = new byte[buf.readableBytes()];
                oldCnt = buf.refCnt();
                buf.getBytes(buf.readerIndex(), bytes);
                System.out.println("Client-" + ctx.channel().remoteAddress() + ": " + new String(bytes));

                String sendMsg = ctx.channel().remoteAddress() + ": " + new String(bytes);
                clients.writeAndFlush(Unpooled.copiedBuffer(sendMsg.getBytes()));

                ctx.writeAndFlush("ACK: Message received.");
            } finally {
                if (buf != null && buf.refCnt() == oldCnt+1) {
                    ReferenceCountUtil.release(buf);
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();

            Server.clients.remove(ctx.channel());
            ctx.close();
        }
    }
}
