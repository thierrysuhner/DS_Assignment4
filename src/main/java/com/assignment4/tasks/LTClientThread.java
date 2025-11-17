package com.assignment4.tasks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

// This Class handles the continuous listening for incoming messages from the server
public class LTClientThread implements Runnable {

  private final DatagramSocket clientSocket;
  private final LamportTimestamp lc;
  byte[] receiveData = new byte[1024];

  public LTClientThread(DatagramSocket clientSocket, LamportTimestamp lc) {
    this.clientSocket = clientSocket;
    this.lc = lc;
  }

  @Override
  public void run() {
    // Continuously listen for incoming messages from the server
    while (true) {
      try {
        // Prepare packet for receiving
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        // Block until a packet arrives
        clientSocket.receive(receivePacket);

        // Convert bytes -> string
        String msg = new String(receivePacket.getData(), 0, receivePacket.getLength());

        // Parse the message: expected format: "message:timestamp:id"
        String[] parts = msg.split(":");
        if (parts.length == 3) {
          String messageText = parts[0];
          int receivedTimestamp = Integer.parseInt(parts[1]);
          int senderId = Integer.parseInt(parts[2]);

          // Print received message
          if (messageText.equals("Join")) { messageText = "User " + senderId + " has connected!"; }
          System.out.println("Client" + senderId + ": " + messageText + ":" + receivedTimestamp);

          // Update the clock based on the timestamp received from the server
          // Do not update timestamp if message was just "join"
          if (!messageText.equals("Join")) { lc.updateClock(receivedTimestamp); }
          System.out.println("Current clock: " + lc.getCurrentTimestamp());
        } else {
          System.out.println("Malformed message received: " + msg);
        }
      } catch (IOException e) {
        System.err.println("Error receiving message: " + e.getMessage());
      }
    }
  }
}