package com.osfocus.tank;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        TankFrame tf = new TankFrame();

        int initTankCount = Integer.parseInt((String) PropertyMgr.get("initTankCount"));

        for (int i = 0; i < initTankCount; i++) {
            tf.tanks.add(new Tank(50 + i*80, 200, Dir.DOWN, Group.BAD, tf));
        }

        while (true) {
            Thread.sleep(50);
            tf.repaint();
        }
    }
}
