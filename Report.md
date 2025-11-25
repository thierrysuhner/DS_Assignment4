Assignment 4
------------

# Team Members
- Thierry Suhner
- Karla Ruggaber

# GitHub link to your (forked) repository (if submitting through GitHub)

https://github.com/thierrysuhner/DS_Assignment4

# Task 2

1. Why did message D have to be buffered and can we now always guarantee that all clients
   display the same message order?


   Message `D` arrived with the vector timestamp `[2,1,1,0]` (from client 1). At the moment it arrived, our local vector clock was `[1,1,0,1]`. To deliver a message from sender 1, the 2 vector-clock delivery conditions must hold: The sender's timestamp must be exactly one ahead (which is okay here, as our local vector for sender ID 1 was 1, and the arriving timestamp of the sender has 2 as its entry for sender ID 1, i.e. 1+1=2). But, furthermore all other components must be $\leq$ local. If we compare all positions, we find that the timestamp for the client with ID 3 is 1, while the timestamp of our local time vector for the client with ID 3 is 0. Thus, the constraint that all timestamps of the senders vectorclock must be $\leq$ than the timestamps of our local clock, is not fulfilled. The message has to be buffered.

Now, we can't guarantee that all clients display exactly the same message order. We can only guarantee this for causally related messages, thus when message X -> Y (X happened-before Y), all clients will deliver X before Y. But if two messages are concurrent (neither happens-before the other), then vector clocks do *not* impose a global total order, so different clients may show concurrent messages in different order, which is normal and expected.

2. Note that the chat application uses UDP. What could be an issue with this design choice—and
   how would you fix it?


  The issue could be, that messages could not arrive because of the design of UDP. UDP is an unreliable transport protocol and thus does not guarantee message delivery (packets can be dropped). This poses a problem because our vector-clock logic assumes that messages eventually arrive, even if out of order. So if UDP drops a packet, the client will wait forever for a missing message, and thus permanently blocking the buffer and preventing further messages from being delivered. We could fix it by using TCP instead of UDP, as it provides reliable delivery including retransmission and no duplicate packets, so messages arrive eventually. Alternatively, we could implement this reliability we need on top of the UDP protocol in our application, e.g. by adding sequence numbers for messages, ACKs for receivers and retransmission mechanisms.
   
# Task 3

1. What is potential causality in Distributed Systems, and how can you model it? Why
   “potential causality” and not just “causality”?


Generally speaking, in a distributed system, there is no global clock and processes execute independently. Therefore, we can't always know whether one event truly caused another, we can just assume whether one event *may* have influenced another based on the communication structure, which is known as potential causality. We can model potential causality using logical clock mechanisms: Lamport clocks (track partial order defined by "happens-before") or Vector clocks (capture full partial order and can distinguish causally-related and concurrent events).
     We can however still not observe actual, physical causality between events, just infer causality based on communication and ordering rules, which is why it's "potential causality" and not just "causality".

2. If you look at your implementation of Task 2.1, can you think of one limitation of Vector Clocks? How would you overcome the limitation?


First of all, vector clocks were included in each message of Task 2.1, but we did not use them to enforce causal ordering. Messages werde delivered immediately upon arrival, without checking whether their vector timestamps indicated missing causal predecessors. Thus, vector clocks alone don't enforce causal communication, they only represent potential causality. So causally related messages may still be displayed out of order, if the application does not interpret and use these timestamps for delaying delivery. To overcome this limitation, we had to implement Task 2.2 where we check vector timestamps on receive (using the acceptance condition), buffer messages that arrive too early and deliver buffered messages once their dependencies are satisfied.
Another limitation of vector clocks we found also with the interpretation implemented in Task 2.2 in mind is, that their size grows linearly with the number of processes in the system, as the vector clock must store one entry for every client and every client must maintain and compare these quickly growing vectors. This becomes problematic in systems that are dynamic (clients frequently joining/leaving), large-scale or resource-constrained, as vector clocks may become too large, expensive to send or costly to compare. We could overcome this problem by using lamport clocks if full causal ordering is not required or use more complicated vectors as clocks.
Furthermore, a major limitation of our vector clocks is that they assume the number of processes is fixed and known in advance (fixed index to locate clock-values). So if new processes join/terminate dynamically, vector clocks can't easily adapt. To overcome this issue, we could use Dynamic Vector Clocks that use variable-length representations (using lists/matrices of entries), allow process IDs to be added on-the-fly and provide mechanisms for eliminating entries of terminated processes.


3. Figure 4 shows an example of enforcing causal communication using Vector Clocks. You can find a detailed explanation of this example and the broadcast algorithm being used in
   the Distributed Systems book by van Steen and Tannenbaum (see Chapter 5.2.2, page 270). Would you achieve the same result if you used the same broadcast algorithm but replaced
   Vector Clocks with Lamport Clocks? If not, why not? Explain briefly. 

 
No, we would not achieve the same result with Lamport clocks, because they provide only a single scalar timestamp that enforces a total order but cannot distinguish between causally related events and concurrent events. In the figure, when P3 receives m* before m, Lamport clocks give P3 no way to detect that m -> m* (m happens before m*). Therefore, P3 delivers m* immediately, violating the causal order. Vector clocks prevent this by allowing P3 to postpone delivery untill the causally prior message m has been delivered.
