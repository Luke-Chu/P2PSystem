package tool;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * 这个类完全是书上的，我认为这个类极其重要very very important
 * @author Luke
 */
public class MyStreamSocket extends Socket {
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public MyStreamSocket(InetAddress acceptorHost, int acceptorPort) throws SocketException, IOException {
        this.socket = new Socket(acceptorHost, acceptorPort);
        setStreams();
    }

    public MyStreamSocket(Socket socket) throws IOException {
        this.socket = socket;
        setStreams();
    }

    private void setStreams() throws IOException {
        InputStream inputStream = socket.getInputStream();
        input = new BufferedReader(new InputStreamReader(inputStream));
        OutputStream outputStream = socket.getOutputStream();
        output = new PrintWriter(new OutputStreamWriter(outputStream));
    }

    public void sendMessage(String message) throws IOException {
        output.println(message);
        output.flush();
    }

    public String receiveMessage() throws IOException {
        String message = input.readLine();
        StringBuilder meg = new StringBuilder(message);
        while (input.ready()){
            meg.append("\n").append(input.readLine());
        }
        return meg.toString();
    }
}
