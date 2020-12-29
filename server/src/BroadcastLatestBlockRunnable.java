import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;

public class BroadcastLatestBlockRunnable implements Runnable {
    private HashMap<ServerInfo, Date> serverStatus;
    private int srcPort;
    private Blockchain blockchain;

    public BroadcastLatestBlockRunnable(HashMap<ServerInfo, Date> serverStatus, int srcPort, Blockchain blockchain){
        this.serverStatus = serverStatus;
        this.srcPort = srcPort;
        this.blockchain = blockchain;
    }

    @Override
    public void run() {
        Random r = new Random();
        while (true) {
            String base64Hash = "";
            if(blockchain.getHead() != null) {
                base64Hash = Base64.getEncoder().encodeToString(blockchain.getHead().calculateHash());
            }else{

            }
            String message = "lb|" + srcPort + "|" + blockchain.getLength() + "|" + base64Hash + "\n";

            // We need to spawn 5 threads. If there are less than 5 items in the hashmap, then just spawn n threads

            ArrayList<Thread> threadArrayList = new ArrayList<>();
            if(serverStatus.size() <= 5) {
                for (ServerInfo info : serverStatus.keySet()) {
                    Thread thread = new Thread(new BroadcastLatestBlock(message, info.getHost(), info.getPort()));
                    threadArrayList.add(thread);
                    thread.start();

                }
            }else{
                // We need to pick 5 numbers from 0 to n-1 (where n is the num elements in hashmap) at random.
                ArrayList<Integer> nums = new ArrayList<>();
                Random rand = new Random();
                while(nums.size() != 5){
                    int randint = rand.nextInt(serverStatus.size()-1);
                    if(!nums.contains(randint)){
                        nums.add(randint);
                    }
                }

                int index = 0;
                for (ServerInfo info : serverStatus.keySet()) {
                    if(nums.contains(index)) {
                        Thread thread = new Thread(new BroadcastLatestBlock(message, info.getHost(), info.getPort()));
                        threadArrayList.add(thread);
                        thread.start();
                    }
                    index++;
                }
            }

            for (Thread thread : threadArrayList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
            }

            //Sleep three second(s)
            try {
                Thread.sleep(40000);
            } catch (InterruptedException e) {
            }
        }
    }
}