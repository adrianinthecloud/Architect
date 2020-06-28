package com.osfocus.tank.net;

import com.osfocus.tank.Dir;
import com.osfocus.tank.Tank;
import com.osfocus.tank.TankFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class TankStopMsg extends Msg {
    public int x, y;
    UUID id;

    public TankStopMsg() {}

    public TankStopMsg(UUID id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public void handle() {
        if (id.equals(TankFrame.INSTANCE.getMainTank().getId())) return;
        Tank t = TankFrame.INSTANCE.getTank(id);
        if (t != null) {
            t.setMoving(false);
            t.setX(x);
            t.setY(y);
        }
    }

    @Override
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        byte[] bytes = null;

        try {
            dos.writeInt(x);
            dos.writeInt(y);
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
        id = new UUID(buf.readLong(), buf.readLong());
    }

    @Override
    public String toString() {
        return "TankStopMsg{" +
                "x=" + x +
                ", y=" + y +
                ", id=" + id +
                '}';
    }

    @Override
    public MsgType getMsgType() {
        return MsgType.TankStop;
    }
}
