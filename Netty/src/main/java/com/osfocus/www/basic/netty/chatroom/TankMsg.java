package com.osfocus.www.basic.netty.chatroom;

public class TankMsg {
	public int x, y;

	public TankMsg(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "TankMsg:" + x + "," + y; 
	}
}
