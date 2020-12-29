import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BlockchainServer {

    public static void main(String[] args) {

        if (args.length != 1) {
            return;
        }
        ServerInfoList obj = new ServerInfoList();
        int portNumber = 0;
        if(obj.isInt(args[0])){
            portNumber = Integer.parseInt(args[0]);
        }else{
            return;
        }
        if((portNumber < 1024) || (portNumber > 65535)){
            return;
        }
        Blockchain blockchain = new Blockchain();
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(portNumber);
        }catch (IOException e){
            e.printStackTrace();
        }
        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();
        while (true) {
            if(serverSocket != null) {

                pcr.setRunning(true);
                Socket clientSocket = null;

                try {
                    clientSocket = serverSocket.accept();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                BlockchainServerRunnable bcr = new BlockchainServerRunnable(clientSocket, blockchain);

                Thread bct = new Thread(bcr);
                bct.start();

            /*try {
                bct.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/


                pcr.setRunning(false);

            /*try {
                pct.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            }else{
                break;
            }
        }
    }

    // implement any helper method here if you need any
}
