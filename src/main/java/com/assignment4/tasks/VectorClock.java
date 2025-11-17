package com.assignment4.tasks;

import java.util.Arrays;

public class VectorClock {

  private final int[] timestamps;

  public VectorClock(int numOfClients) {
    timestamps = new int[numOfClients];
    Arrays.fill(timestamps, 0);
  }

  public synchronized void setVectorClock(int processId, int time) {
    //Set the vector clock value for the processId
    timestamps[processId] = time;
  }

  public synchronized void tick(int processId) {
    // Increment the vector clock value for the processId
    timestamps[processId]++;
  }

  public synchronized int getCurrentTimestamp(int processId) {
    return timestamps[processId];
  }

  public synchronized void updateClock(VectorClock other) {
    // Update the vector clock based on the values of another vector clock
    for (int i = 0; i < other.timestamps.length; i++) {
      if (other.timestamps[i] > this.timestamps[i]) { this.timestamps[i] = other.timestamps[i]; }
  }}

  public synchronized String showClock() {
    return Arrays.toString(timestamps);
  }

  // TODO:
  // For Task 2.2
  // Check if a message can be delivered or has to be buffered
  public synchronized boolean checkAcceptMessage(int senderId, VectorClock senderClock) {
    boolean acceptMessage = true;
    return acceptMessage;
  }
}
