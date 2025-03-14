package com.sender;

import java.io.OutputStream;
import java.io.PrintWriter;

public class BluetoothSender {
    private static PrintWriter writer;

    public static void initialize(OutputStream out) {
        writer = new PrintWriter(out, true);
    }

    public static void send(String data) {
        System.out.println("Sending (Encrypted): " + data);
        if (writer != null) {
            writer.println(data);
            System.out.println("Sent: " + data);
        } else {
            System.err.println("Bluetooth not connected.");
        }
    }
}
