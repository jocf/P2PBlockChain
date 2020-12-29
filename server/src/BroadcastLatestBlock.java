import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BroadcastLatestBlock implements Runnable{

    private String message;
    private String host;
    private int port;

    public BroadcastLatestBlock(String message, String host, int port){
        this.message = message;
        this.host = host;
        this.port = port;

    }

    @Override
    public void run(){
        try {
            Socket toServer = new Socket();
            toServer.connect(new InetSocketAddress(host, port), 2000);
            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);
            // send the message forward
            printWriter.print(message);
            printWriter.flush();

            // close printWriter and socket
            printWriter.close();
            toServer.close();
        }catch (IOException e) {
        }
    }
}
