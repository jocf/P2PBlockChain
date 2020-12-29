import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

public class BlockchainServerRunnable implements Runnable{

    private Socket clientSocket;
    private Blockchain blockchain;
    private HashMap<ServerInfo, Date> serverStatus;
    private ObjectOutputStream oos;
    private boolean recheck = false;

    public BlockchainServerRunnable(Socket clientSocket, Blockchain blockchain, HashMap<ServerInfo, Date> serverStatus) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
        this.serverStatus = serverStatus;
    }

    public void run() {
        try {
            oos = null;
            serverHandler(clientSocket.getInputStream(), clientSocket.getOutputStream());
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void broadcastSI(String message, ServerInfo toSkip){
            for (ServerInfo info : serverStatus.keySet()) {
                if (!info.equals(toSkip)) {
                    try {
                        Socket toServer = new Socket();
                        toServer.connect(new InetSocketAddress(info.getHost(), info.getPort()), 2000);
                        //PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);
                        PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);

                        // send the message forward
                        printWriter.print(message);
                        printWriter.flush();

                        // close printWriter and socket
                        printWriter.close();
                        toServer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    }

    public void processHB(String inputLine){
        // If the server doesn't exist in our hash-map then we need to add it.
        Date tempDate = new Date();
        String[] splitInputLine = inputLine.split("\\|");
        String remoteAddress = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
        int remotePort = Integer.parseInt(splitInputLine[1]);
        ServerInfo tempInfo = new ServerInfo(remoteAddress,remotePort);

        if(serverStatus.containsKey(tempInfo)) {
            // Already exists so update time
            serverStatus.replace(tempInfo,tempDate);
        }else{
            // Does not exist, BROADCAST si|<Q’s Port>|<P’s IP>|<P’s Port> and THEN add to the serverStatus so as to not broadcast
            // to itself.

            String message = "si|" + clientSocket.getLocalPort() + "|" + remoteAddress + "|" + remotePort +"\n";
            broadcastSI(message, null);
            serverStatus.put(tempInfo,tempDate);
        }

    }

    public void processSI(String inputLine){
        // If the server doesn't exist in our hash-map then we need to add it.
        Date tempDate = new Date();
        String[] splitInputLine = inputLine.split("\\|");
        ServerInfo tempInfo = new ServerInfo(splitInputLine[2],Integer.parseInt(splitInputLine[3]));
        if(!serverStatus.containsKey(tempInfo)){
            // Does not already exist
            // Create a temp serverinfo for the sender
            String senderAddress = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
            ServerInfo senderInfo = new ServerInfo(senderAddress,Integer.parseInt(splitInputLine[1]));
            String message = "si|" + clientSocket.getLocalPort() + "|" + splitInputLine[2] + "|" + splitInputLine[3] + "\n";
            broadcastSI(message, senderInfo);
            serverStatus.put(tempInfo,tempDate);
        }else{
            serverStatus.replace(tempInfo,tempDate);
        }

        String senderAddress = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
        int senderPort = Integer.parseInt(splitInputLine[1]);
        ServerInfo senderInfo = new ServerInfo(senderAddress,senderPort);

        if(!serverStatus.containsKey(senderInfo)){
            String message = "si|" + clientSocket.getLocalPort() + "|" + senderAddress + "|" + senderPort + "\n";
            broadcastSI(message, senderInfo);
            serverStatus.put(senderInfo,tempDate);
        }else{
            serverStatus.replace(senderInfo,tempDate);
        }
    }

    public void processLB(String inputLine){
        Date tempDate = new Date();
        String[] splitInputLine = inputLine.split("\\|");
        String senderAddress = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
        int senderPort = Integer.parseInt(splitInputLine[1]);
        ServerInfo senderInfo = new ServerInfo(senderAddress,senderPort);

        if(serverStatus.containsKey(senderInfo)){
            serverStatus.replace(senderInfo,tempDate);
        }

        // Now we need to see if we are up to date!
        if(blockchain.getHead() == null){
            Catchup tmpCatchup = new Catchup(1, senderPort, senderAddress, blockchain);
            Thread catchup = new Thread(tmpCatchup);
            catchup.start();
            try {
                catchup.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }else {
            if (Integer.parseInt(splitInputLine[2]) == blockchain.getLength()) {
                byte[] currHeadHash = blockchain.getHead().calculateHash();
                byte[] catchupHash = null;
                if (splitInputLine.length > 4) {
                    catchupHash = Base64.getDecoder().decode(splitInputLine[4]);
                }

                int index = 0;
                if (catchupHash != null) {
                    while (index < currHeadHash.length) {
                        if (currHeadHash[index] > catchupHash[index]) {
                            // do nothing, we are the latest block.
                            return;
                        }
                        if (currHeadHash[index] < catchupHash[index]) {
                            // We are smaller so we need to catchup
                            Catchup tmpCatchup = new Catchup(1, senderPort, senderAddress, blockchain);
                            Thread catchup = new Thread(tmpCatchup);
                            catchup.start();
                            try {
                                catchup.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return;
                        } else {
                            index++;
                        }
                    }
                }


            } else {
                if (Integer.parseInt(splitInputLine[2]) < blockchain.getLength()) {
                    // The length is SMALLER Than our current blockchain length
                    return;
                } else {
                    // It's larger so we are behind :( Just update though with CU its easy :)
                    Catchup tmpCatchup = new Catchup(1, senderPort, senderAddress, blockchain);
                    Thread catchup = new Thread(tmpCatchup);
                    catchup.start();
                    try {
                        catchup.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    public void processCU(String inputLine){
        if(inputLine.equals("cu")){
            try {
                oos.writeObject(blockchain.getHead());
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                String hash64 = inputLine.split("\\|")[1];

                Block currentBlock = blockchain.getHead().getPreviousBlock();
                while (currentBlock != null) {
                    String currenthash = Base64.getEncoder().encodeToString(currentBlock.calculateHash());
                    if (currenthash.equals(hash64)) {
                        oos.writeObject(currentBlock);
                        oos.flush();
                        return;
                    } else {
                        currentBlock = currentBlock.getPreviousBlock();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void serverHandler(InputStream clientInputStream, OutputStream clientOutputStream) {


        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(clientInputStream));
        PrintWriter outWriter = new PrintWriter(clientOutputStream, true);
        try {
            while (true) {
                String inputLine = inputReader.readLine();
                if (inputLine == null) {
                    break;
                }
                String[] tokens = inputLine.split("\\|");
                switch (tokens[0]) {
                    case "tx":
                        if (blockchain.addTransaction(inputLine))
                            outWriter.print("Accepted\n\n");
                        else
                            outWriter.print("Rejected\n\n");
                            outWriter.flush();
                        break;
                    case "pb":
                        outWriter.print(blockchain.toString() + "\n");
                        outWriter.flush();
                        break;
                    case "hb":
                        System.out.println("Message: " + inputLine);
                        processHB(inputLine);
                        break;
                    case "si":
                        System.out.println("Message: " + inputLine);
                        processSI(inputLine);
                        break;
                    case "lb":
                        System.out.println("Message: " + inputLine);
                        processLB(inputLine);
                        break;
                    case "cu":
                        System.out.println("Message: " + inputLine);
                        if(oos == null){
                            oos = new ObjectOutputStream(clientSocket.getOutputStream());
                        }
                        processCU(inputLine);
                        break;
                    case "cc":
                        return;
                    default:
                        outWriter.print("Error\n\n");
                        outWriter.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } /*catch (InterruptedException e) {
        }*/
    }
}
