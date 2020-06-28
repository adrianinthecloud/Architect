package com.osfocus.tank.net;

import com.osfocus.tank.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class BulletNewMsg extends Msg {
    public int x, y;
    public Dir dir;
    UUID id;

    public BulletNewMsg() {};

    public BulletNewMsg(UUID id, int x, int y, Dir dir) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.dir = dir;
    }

    @Override
    public void handle() {
        if (id.equals(TankFrame.INSTANCE.getMainTank().getId())) return;
        TankFrame.INSTANCE.bullets.add(new Bullet(x, y, dir, Group.BAD));
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] bytes = null;

        try {
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(dir.ordinal());
            dos.writeLong(id.getMostSignificantBits());
            dos.writeLong(id.getLeastSignificantBits());
            dos.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bytes;
    }

    @Override
    public void parse(byte[] bytes) {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        x = buf.readInt();
        y = buf.readInt();
        dir = Dir.values()[buf.readInt()];
        id = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public String toString() {
        return "BulletNewMsg{" +
                "x=" + x +
                ", y=" + y +
                ", dir=" + dir +
                ", id=" + id +
                '}';
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.BulletNew;
    }
}
