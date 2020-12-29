import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;

public class BlockchainServer {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("An invalid number of startup arguments were provided!");
            return;
        }

        int localPort = 0;
        int remotePort = 0;
        String remoteHost = null;

        try {
            localPort = Integer.parseInt(args[0]);
            remoteHost = args[1];
            remotePort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return;
        }

        Blockchain blockchain = new Blockchain();

        HashMap<ServerInfo, Date> serverStatus = new HashMap<ServerInfo, Date>();
        serverStatus.put(new ServerInfo(remoteHost, remotePort), new Date());

        // start up PeriodicTimeoutCheck
        PeriodicTimeoutRunnable ptr = new PeriodicTimeoutRunnable(serverStatus);
        Thread ptt = new Thread(ptr);
        ptt.start();

        // start up PeriodicHeartBeat
        PeriodicHeartBeatRunnable phbr = new PeriodicHeartBeatRunnable(serverStatus, localPort);
        Thread phbt = new Thread(phbr);
        phbt.start();

        // start up BroadcastLatestBlock
        BroadcastLatestBlockRunnable plbr = new BroadcastLatestBlockRunnable(serverStatus, localPort, blockchain);
        Thread plbt = new Thread(plbr);
        plbt.start();

        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();


        // We will run a Catchup initially here on first program run.
        //System.out.println("Updated updated updated updated code");
        ServerInfo initialInfo = (ServerInfo) serverStatus.keySet().toArray()[0];
        //System.out.println("Host was available");
        Catchup tmpCatchup = new Catchup(0, initialInfo.getPort(), initialInfo.getHost(), blockchain);
        //System.out.println("Running NEW catchup on" + initialInfo.getHost() + initialInfo.getPort());
        Thread catchup = new Thread(tmpCatchup);
        catchup.start();
        try {
            catchup.join();
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        //System.out.println("CAUGHT UP TO: " + blockchain.toString());
        //--------------


        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new BlockchainServerRunnable(clientSocket, blockchain, serverStatus)).start();
            }
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        } finally {
            try {
                pcr.setRunning(false);
                pct.join();
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
    }
}
