import tool.GetFormatDate;
import tool.MyStreamSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;

public class ComWithServer extends Thread{
    /*
    node 保存当前客户端自身的信息（包括用户名、IP 地址、监听端口等）。该对象用于告诉服务器“这是谁”，并在后续通信中作为身份标识。
    socketAddress 存储服务器的套接字地址信息（IP 地址和端口）。客户端通过该地址来连接服务器。
    userInfo 管理在线用户信息的对象。它提供添加、删除用户等方法，用于更新客户端侧的用户列表状态。 当服务器推送上线或下线通知时，将调用其方法更新在线用户信息。
    client 对客户端主界面或控制类的引用。通过这个引用，可以在接收到服务器消息后更新聊天记录区域、更新在线用户列表显示等界面操作。
    myStreamSocket 自定义封装了 Socket 输入输出功能的对象。它在与服务器建立连接后初始化，用于发送和接收消息，简化底层字符流的操作。
    isStop 一个布尔标志，表示当前通信线程是否应该停止运行。当发送下线消息或收到停止信号时，将该标志置为 true 以退出监听循环。
     */
    private final Node node;
    private final SocketAddress socketAddress;
    private final UserInfo userInfo;
    private final Client client;
    private MyStreamSocket myStreamSocket;
    private boolean isStop;
    public ComWithServer(Node node, UserInfo userInfo,SocketAddress socketAddress, Client client) {
        this.node = node;
        this.userInfo = userInfo;
        this.socketAddress = socketAddress;
        this.client = client;
        this.isStop = false;
    }

    // 用于向服务器发送下线消息。约定协议中，“#” 表示客户端关闭或退出的标识符。
    public void sendEndMessage() throws IOException {
        myStreamSocket.sendMessage("#");
        myStreamSocket.close();
        this.isStop = true;
        System.out.println("Offline Notification(Myself): "+node);
    }
    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            int timeoutPeriod = 5000;
            socket.connect(socketAddress,timeoutPeriod);    // 直到连接成功才进行数据传送（使用 5000 毫秒的超时时间）
            //将自己信息打包发送给服务器
            myStreamSocket = new MyStreamSocket(socket);
            myStreamSocket.sendMessage(node.toString());

            client.setChatRecord(GetFormatDate.getFormatDate(new Date())+"恭喜您 \""+node.username+"\" 与服务器连接成功！");
            // 消息接收循环
            while (!isStop){
                String originalMessage = myStreamSocket.receiveMessage();
                String[] userList = originalMessage.split("@@");
                switch (userList[0]) {
                    case "上线通知" -> {
                        // 服务器通知有新用户上线
                        String[] onlineUserInfo = userList[1].split("&");
                        String onlineUserName = onlineUserInfo[0];
                        InetAddress onlineUserIP = InetAddress.getByName(onlineUserInfo[1]);
                        int onlineUserListenPort = Integer.parseInt(onlineUserInfo[2]);
                        Node tempNode = new Node(onlineUserName, onlineUserIP, onlineUserListenPort);
                        userInfo.addUser(tempNode);
                        client.updateList(userInfo);
                        System.out.println("Online Notification: "+node);
                        client.setChatRecord(GetFormatDate.getFormatDate(new Date())+onlineUserName + "已上线！");
                    }
                    case "下线通知" -> {
                        // 服务器通知某用户下线
                        String[] offlineUserInfo = userList[1].split("&");
                        String offlineUserName = offlineUserInfo[0];
                        InetAddress offlineUserIP = InetAddress.getByName(offlineUserInfo[1]);
                        int offlineUserListenPort = Integer.parseInt(offlineUserInfo[2]);
                        Node tempNode = new Node(offlineUserName, offlineUserIP, offlineUserListenPort);
                        userInfo.deleteUser(offlineUserName);
                        client.updateList(userInfo);
                        System.out.println("Offline Notification: "+node);
                        client.setChatRecord(GetFormatDate.getFormatDate(new Date())+offlineUserName + "已下线！");
                    }
                    case "更新列表" -> {
                        // 用于更新整个用户列表
                        String[] originalUserInfo = userList[1].split("\\$");
                        for (String s : originalUserInfo) {
                            String[] onlineUserInfo = s.split("&");
                            String onlineUserName = onlineUserInfo[0];
                            InetAddress onlineUserIP = InetAddress.getByName(onlineUserInfo[1]);
                            int onlineUserListenPort = Integer.parseInt(onlineUserInfo[2]);
                            Node tempNode = new Node(onlineUserName, onlineUserIP, onlineUserListenPort);
                            userInfo.addUser(tempNode);
                        }
                        client.updateList(userInfo);
                        System.out.println("Renew User List: "+userInfo);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //System.exit(0);
        }
    }
}
