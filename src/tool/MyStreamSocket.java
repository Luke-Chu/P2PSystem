package tool;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * 这个类完全是书上的，我认为这个类极其重要 very very important
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
        /*
        在这些包装操作中，没有显式指定字符编码（会使用平台默认编码），如果你要求统一编码（如 UTF-8），
            可以在 InputStreamReader 和 OutputStreamWriter 的构造方法中添加对应的编码参数。
        如果在不同的系统中需要处理统一的字符编码问题，可以显式指定编码参数；同时也可以添加异常处理机制，防止因连接异常导致程序崩溃。
         */
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
