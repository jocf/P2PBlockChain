import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class BlockchainClientRunnable implements Runnable {

    private String reply;
    private String serverName;
    private int portNumber;
    private String message;

    public BlockchainClientRunnable(int serverNumber, String serverName, int portNumber, String message) {
        this.reply = "Server" + serverNumber + ": " + serverName + " " + portNumber + "\n"; // header string
        this.serverName = serverName;
        this.portNumber = portNumber;
        this.message = message;
    }

    public void run() {
        // Send the MESSAGE to the server via a socket
        // Receive the message, STORE locally in reply
        // Send CC to SOCKET
        // Append reply to REPLY variable

        Socket sock;

        try{
            sock = new Socket(serverName, portNumber);
        } catch (IOException e) {
            System.err.println("Server is not available\n\n");
            System.err.println("The server could not be connected to! " +
                    "Please check the provided server name and port number!");
            this.reply = this.reply + "Server is not available\n\n";
            return;
        }

        //Write code in here to do cool stuff with sending messages and stuff.

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try{
            inputStream = sock.getInputStream();
            outputStream = sock.getOutputStream();
        } catch (IOException e){
            System.err.println("An exception occurred when starting the input or output stream. " +
                    "Please ensure server is connected and running.");
            this.reply = this.reply + "Server is not available\n\n";
        }

        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(inputStream));
        PrintWriter outWriter = new PrintWriter(outputStream, true);

        outWriter.println(message);
        outWriter.flush();


        try {
            while (((inputReader.readLine()) != null) && inputReader.ready() == true) {
                //System.out.print(reply);
                char[] buffer = new char[1024];
                int length = 1024;
                while(length == 1024) {
                    length = inputReader.read(buffer);
                    //System.out.print(Arrays.copyOfRange(buffer, 0, length));
                    Arrays.copyOfRange(buffer, 0, length);
                }
                String str2 = String.valueOf(buffer);
                this.reply = this.reply + str2;
                //System.out.print(this.reply);
            }
        }catch( IOException e){
            System.err.println("Error when receiving message! Did the server close" +
                    " unexpectedly?");
            this.reply = this.reply + "Server is not available\n\n";
        }

        // Close the socket

        outWriter.print("cc");
        outWriter.flush();


        try {
            //this.reply = this.reply + intext + "\n";
            sock.close();
            return;
        } catch (IOException e) {
            System.err.println("Server connection could not be closed! " +
                    "This occurred for the server " + serverName + ":" + portNumber + "!");
            this.reply = this.reply + "Server is not available\n\n";
            return;
        }
    }

    public String getReply() {
        return reply;

    }

    // implement any helper method here if you need any
}