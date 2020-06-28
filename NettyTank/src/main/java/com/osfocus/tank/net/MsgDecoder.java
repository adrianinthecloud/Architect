package com.osfocus.tank.net;

import com.osfocus.tank.Dir;
import com.osfocus.tank.Group;
import com.osfocus.tank.PropertyMgr;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.UUID;

import static com.osfocus.tank.net.MsgType.TankJoin;

public class MsgDecoder extends ByteToMessageDecoder {
    private static final String PACKAGE = (String) PropertyMgr.get("codecPackage");

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 8) return;
        in.markReaderIndex();

        MsgType msgType = MsgType.values()[in.readInt()];
        int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return; // wait for TCP to reconstruct a full message
        }

        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        Msg msg = (Msg) Class.forName(PACKAGE+"."+msgType.toString()+"Msg").getConstructor().newInstance();

        msg.parse(bytes);
//
//        msg.x = in.readInt();
//        msg.y = in.readInt();
//        msg.dir = Dir.values()[in.readInt()];
//        msg.moving = in.readBoolean();
//        msg.group = Group.values()[in.readInt()];
//        msg.id = new UUID(in.readLong(), in.readLong());

        out.add(msg);
    }
}
