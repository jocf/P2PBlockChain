import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PeriodicHeartBeatRunnable implements Runnable {

    private HashMap<ServerInfo, Date> serverStatus;
    private int sequenceNumber;
    private int srcPort;

    public PeriodicHeartBeatRunnable(HashMap<ServerInfo, Date> serverStatus, int port) {
        this.serverStatus = serverStatus;
        this.sequenceNumber = 0;
        this.srcPort = port;
    }

    @Override
    public void run() {
        while(true) {
            // broadcast HeartBeat message to all peers
            ArrayList<Thread> threadArrayList = new ArrayList<>();
            for (ServerInfo info : serverStatus.keySet()) {
                String tempIp = info.getHost();
                int tempDestPort = info.getPort();
                Thread thread = new Thread(new HeartBeatClientRunnable(tempIp,tempDestPort,srcPort,sequenceNumber));
                threadArrayList.add(thread);
                thread.start();
            }

            for (Thread thread : threadArrayList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
            }

            // increment the sequenceNumber
            sequenceNumber += 1;

            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}
