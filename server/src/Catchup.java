import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;

public class Catchup implements Runnable {

    private int catchupType;
    private int port;
    private String host;
    private Blockchain blockchain;
    private ObjectInputStream ois;
    private OutputStream os;
    private Socket clientSocket;
    private PrintWriter outWriter;

    public Catchup(int catchupType, int port, String host, Blockchain blockchain){
        this.catchupType = catchupType;
        this.port = port;
        this.host = host;
        this.blockchain = blockchain;
    }

    public void getFirstBlock(){
        if(os != null) {
            // Send the initial catchup here :D
            try {
                outWriter.print("cu\n");
                outWriter.flush();
                if(ois == null){
                    this.ois = new ObjectInputStream(this.clientSocket.getInputStream());
                }
                Block block = (Block) this.ois.readObject();
                blockchain.setHead(block);
                blockchain.setLength(1);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void catchup(){

        if((os != null) && (ois != null)) {
            // Send the initial catchup here :D

            try {
                int length = 1;
                Block currentBlock = blockchain.getHead();
                if(currentBlock == null){
                    return;
                }
                while (true) {
                    // We need to fetch the rest of the blockchain now :)
                    // Just keep checking to see if the next block is NULL or not.
                    String hash64 = Base64.getEncoder().encodeToString(currentBlock.getPreviousHash());
                    if(hash64.equals("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")){
                        blockchain.setLength(length);
                        return;
                    }
                    String message = "cu|" + hash64 + "\n";
                    outWriter.print(message);
                    outWriter.flush();
                    Block tempBlock = (Block) this.ois.readObject();
                    currentBlock.setPreviousBlock(tempBlock);
                    currentBlock = tempBlock;
                    length++;

                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void processCatchup(){
        // catchupType : 0 == cu, 1 == cu|hash
        if(catchupType == 0){
            this.ois = null;
            this.os = null;
            this.clientSocket = null;
            try {
                this.clientSocket = new Socket();
                this.clientSocket.connect(new InetSocketAddress(host, port),2000);
                this.os = this.clientSocket.getOutputStream();
                this.outWriter = new PrintWriter(this.os, true);
            }catch (IOException e){
                return;
            }
            getFirstBlock();
            catchup();
            try {
                if(os != null){
                    os.close();
                }
                if(ois != null){
                    ois.close();
                }
                if(outWriter != null){
                    outWriter.close();
                }
                if(clientSocket != null){
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }else{
            this.ois = null;
            this.os = null;
            this.clientSocket = null;
            try {
                this.clientSocket = new Socket();
                this.clientSocket.connect(new InetSocketAddress(host, port),2000);
                this.os = this.clientSocket.getOutputStream();
                this.outWriter = new PrintWriter(this.os, true);
            }catch (IOException e){
            }
            getFirstBlock();
            catchup();
            try {
                if(os != null){
                    os.close();
                }
                if(ois != null){
                    ois.close();
                }
                if(outWriter != null){
                    outWriter.close();
                }
                if(clientSocket != null){
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
    }

    @Override
    public void run() {
        processCatchup();
    }
}
