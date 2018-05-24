package com.glqdlt.myhome.wakeonsystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Slf4j
@SpringBootApplication
public class WakeOnSystemApplication implements CommandLineRunner {


    @Value("${myhome.broadcast.port}")
    public  String PORT;
    @Value("${myhome.broadcast.address}")
    public  String ipStr;
    @Value("${myhome.system.first.mac}")
    public  String macStr;

    public static void main(String[] args) {
        SpringApplication.run(WakeOnSystemApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(ipStr);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, Integer.parseInt(PORT));
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();

            log.info("Send packet.");
        } catch (Exception e) {
            log.error("Some Exception", e);
            System.exit(1);
        }
    }

    private byte[] getMacBytes(String macStr) {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            log.error("Wrong Mac Address", e);
            System.exit(1);
        }
        return bytes;
    }
}
