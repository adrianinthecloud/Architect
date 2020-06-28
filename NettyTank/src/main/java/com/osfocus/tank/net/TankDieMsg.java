package com.osfocus.tank.net;

import com.osfocus.tank.*;

import java.io.*;
import java.util.UUID;

public class TankDieMsg extends Msg {
	public UUID id;

	public TankDieMsg() {}

	public TankDieMsg(Tank t) {
		this.id = t.getId();
	}

	public TankDieMsg(UUID id) {
		super();
		this.id = id;
	}

	@Override
	public String toString() {
		return "TankDieMsg{" +
				"id=" + id +
				'}';
	}

	@Override
	public void handle() {
		if (id.equals(TankFrame.INSTANCE.getMainTank().getId())) {
			Tank m = TankFrame.INSTANCE.getMainTank();
			m.die();
			TankFrame.INSTANCE.explodes.add(new Explode(m.getX(), m.getY(), TankFrame.INSTANCE));
			return;
		}

		Tank t = TankFrame.INSTANCE.getTank(id);
		if (t == null) return;

		TankFrame.INSTANCE.tanks.remove(t);
		TankFrame.INSTANCE.explodes.add(new Explode(t.getX(), t.getY(), TankFrame.INSTANCE));
	}

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		byte[] bytes = null;
		try {
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
			id = new UUID(dis.readLong(), dis.readLong());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MsgType getMsgType() {
		return MsgType.TankDie;
	}
}
