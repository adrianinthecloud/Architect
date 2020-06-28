package com.osfocus.tank.net;

import com.osfocus.tank.Dir;
import com.osfocus.tank.Group;
import com.osfocus.tank.Tank;
import com.osfocus.tank.TankFrame;

import java.io.*;
import java.util.UUID;

public class TankJoinMsg extends Msg {
	public int x, y;
	public Dir dir;
	public boolean moving;
	public Group group;
	public UUID id;

	public TankJoinMsg() {}

	public TankJoinMsg(Tank t) {
		this.x = t.getX();
		this.y = t.getY();
		this.dir = t.getDir();
		this.group = t.getGroup();
		this.id = t.getId();
		this.moving = t.isMoving();
	}

	public TankJoinMsg(int x, int y, Dir dir, boolean moving, Group group, UUID id) {
		super();
		this.x = x;
		this.y = y;
		this.dir = dir;
		this.moving = moving;
		this.group = group;
		this.id = id;
	}



	@Override
	public String toString() {
		return "TankJoinMsg{" +
				"x=" + x +
				", y=" + y +
				", dir=" + dir +
				", moving=" + moving +
				", group=" + group +
				", id=" + id +
				'}';
	}

	@Override
	public void handle() {
		if (id.equals(TankFrame.INSTANCE.getMainTank().getId()) ||
				TankFrame.INSTANCE.containTank(id)) return;

		Tank t = new Tank(this);
		TankFrame.INSTANCE.addTank(t);
		Client.INSTANCE.channel.writeAndFlush(new TankJoinMsg(TankFrame.INSTANCE.getMainTank()));
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
			dos.writeBoolean(moving);
			dos.writeInt(group.ordinal());
			dos.writeLong(id.getMostSignificantBits());
			dos.writeLong(id.getLeastSignificantBits());
			dos.flush();
			bytes = baos.toByteArray();
		} catch (Exception e) {
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
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		try {
			x = dis.readInt();
			y = dis.readInt();
			dir = Dir.values()[dis.readInt()];
			moving = dis.readBoolean();
			group = Group.values()[dis.readInt()];
			id = new UUID(dis.readLong(), dis.readLong());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MsgType getMsgType() {
		return MsgType.TankJoin;
	}
}
