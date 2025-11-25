package com.assignment4.tasks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VectorClientThread implements Runnable {

  private final DatagramSocket clientSocket;
  private final VectorClock vcl;
  private final int id;
  private final byte[] receiveData = new byte[1024]; // Buffer for incoming data
  private final List<Message> buffer = new ArrayList<>(); // This buffer can be used for Task 2.2

  public VectorClientThread(DatagramSocket clientSocket, VectorClock vcl, int id) {
    this.clientSocket = clientSocket;
    this.vcl = vcl;
    this.id = id;
  }

  @Override
  public void run() {
  /*
      Write your code here to continuously listen for incoming messages from the server
      You should first process the received message and then update the vector clock based on the received message (you can use .replaceAll("[\\[\\]]", "").split(",\\s*"); to split a received vector clock into its components)
      Then display the received message and its vector clock
  */
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
                  String[] receivedTimestamps = parts[1].replaceAll("[\\[\\]]", "").split(",\\s*");
                  int senderId = Integer.parseInt(parts[2]);

                  VectorClock receivedClock = new VectorClock(receivedTimestamps.length);
                  for (int i = 0; i < receivedTimestamps.length; i++) {
                      receivedClock.setVectorClock(i, Integer.parseInt(receivedTimestamps[i]));
                  }

                  Message receivedMessage = new Message(messageText,receivedClock,senderId);

                  // Exclude connection messages from check
                  if (messageText.contains("Join")) {
                      displayMessage(receivedMessage);
                      continue;
                  }

                  if (vcl.checkAcceptMessage(senderId,receivedClock)) {
                      // Print received message
                      displayMessage(receivedMessage);
                      checkBuffer();
                  } else {
                      System.out.println("Buffered Message " + messageText + " with clock: " + receivedClock.showClock());
                      buffer.add(receivedMessage);
                  }


              } else {
                  System.out.println("Malformed message received: " + msg);
              }
          } catch (IOException e) {
              System.err.println("Error receiving message: " + e.getMessage());
          }
      }



  }
/*
    This method should print out the message (e.g. Client 1: Hello World!: [1, 0, 0]) and update
    the vector clock without ticking on receive. Then it should display the the updated vector clock.
    Example: Initial clock [0,0,0], updated clock after message from Client 1: [1, 0, 0]
*/
  private void displayMessage(Message message) {
      String receivedMessage = message.getMessage();
    if (receivedMessage.equals("Join")) { receivedMessage = "User " + message.getSenderID() + " has connected!"; }

    System.out.println("Client " + message.getSenderID() + ": " + receivedMessage + ": " + message.getClock().showClock());

    // Update vector clock without ticking on receive
    vcl.updateClock(message.getClock());

    System.out.println("Current clock: " + vcl.showClock());


  }

  private void checkBuffer() {
      boolean deliveredSomething;

      do {
          deliveredSomething = false;

          Iterator<Message> iterator = buffer.iterator();
          while (iterator.hasNext()) {
              Message m = iterator.next();

              if (vcl.checkAcceptMessage(m.getSenderID(), m.getClock())) {
                  displayMessage(m);
                  iterator.remove();
                  deliveredSomething = true;
              }
          }

      } while (deliveredSomething);
  }
}
