package com.osfocus.tank;

import java.awt.*;

public class Explode {
    public static final int WIDTH = ResourceMgr.bulletD.getWidth();
    public static final int HEIGHT = ResourceMgr.bulletD.getHeight();

    private final TankFrame tf;

    private int x, y;

    private int step = 0;

    public Explode(int x, int y, TankFrame tf) {
        this.x = x;
        this.y = y;
        this.tf = tf;

        new Audio("audio/explode.wav").start();
    }

    public void paint(Graphics g) {
        g.drawImage(ResourceMgr.explodes[step++], x, y, null);
        if (step >= ResourceMgr.explodes.length) {
            tf.explodes.remove(this);
        }
    }
}
