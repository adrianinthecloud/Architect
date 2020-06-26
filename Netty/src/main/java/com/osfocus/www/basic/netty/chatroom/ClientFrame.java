package com.osfocus.www.basic.netty.chatroom;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientFrame extends Frame {
    TextArea ta = new TextArea();
    TextField tf = new TextField();
    Client client = null;

    public ClientFrame() {
        this.setSize(600, 400);
        this.setLocation(100, 20);
        this.add(ta, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);
        client = new Client(this);
        tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendToServer(tf.getText());
            }
        });

        this.setVisible(true);
    }

    public void setText(String msg) {
        ta.setText(ta.getText() + msg + System.getProperty("line.separator"));
        tf.setText("");
    }

    public static void main(String[] args) {
        new ClientFrame();
    }
}
