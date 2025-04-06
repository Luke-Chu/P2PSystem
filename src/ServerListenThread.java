import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器监听线程
 */
public class ServerListenThread extends Thread{
    /*
    成员变量介绍
        UserInfo userInfo：保存在线用户信息的对象，用于管理和更新连接过来的用户。
        ServerSocket serverSocket：服务器端的套接字，在指定端口监听并等待客户端连接。
        StringBuilder systemLog：用于存放或者累积系统日志信息，通常在界面上显示服务器运行状态。
        Server server：对服务器主窗体的引用，在运行过程中可用于查询服务器是否已停止（例如调用 server.isStop()）。
    这些成员变量均声明为 final，意味着它们在构造方法中被赋值后不再改变，确保了线程中对这些共享数据的不可变性，有助于并发场景下的数据一致性。
     */
    private final UserInfo userInfo;
    private final ServerSocket serverSocket;
    private final StringBuilder systemLog;
    private final Server server;
    public ServerListenThread(UserInfo userInfo, ServerSocket serverSocket, StringBuilder systemLog, Server server) {
        this.userInfo = userInfo;
        this.serverSocket = serverSocket;
        this.systemLog = systemLog;
        this.server = server;
    }

    @Override
    public void run() {
        // 无限循环监听，只要服务器没有停止，就一直通过 ServerSocket.accept() 等待新的连接请求
        try {
            while (!server.isStop()){
                Socket socket = serverSocket.accept();
                ServerReceiveThread serverReceiveThread = new ServerReceiveThread(userInfo,socket,systemLog,server);
                /*
                取出当前连接的客户端 IP 地址，并将这个信息包含到线程名称中，
                便于调试和日志追踪。通过给线程命名，可以更直观地识别每个线程负责与哪个客户端之间的连接。
                 */
                String name = socket.getInetAddress().getHostAddress();
                serverReceiveThread.setName("--服务器与"+name+"连接线程--");
                /*
                调用 start() 方法后，ServerReceiveThread 内部的 run() 方法会被异步调用，
                从而处理该客户端的通信，主监听线程则继续等待其他连接。
                 */
                serverReceiveThread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
