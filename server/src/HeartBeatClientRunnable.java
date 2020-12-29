import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HeartBeatClientRunnable implements Runnable{

    private String destIP;
    private int destPort;
    private int srcPort;
    private int seqNumber;

    public HeartBeatClientRunnable(String destIP, int destPort, int srcPort, int seqNumber) {
        this.destIP = destIP;
        this.destPort = destPort;
        this.srcPort = srcPort;
        this.seqNumber = seqNumber;
    }

    @Override
    public void run() {
        try {
            // create socket with a timeout of 2 seconds
            Socket toServer = new Socket();
            toServer.connect(new InetSocketAddress(destIP, destPort), 2000);
            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);

            //Format the message
            String message = "hb|" + srcPort + "|" + seqNumber + "\n";

            // send the message forward
            printWriter.print(message);
            printWriter.flush();

            // close printWriter and socket
            printWriter.close();
            toServer.close();
        } catch (IOException e) {
        }
    }
}
