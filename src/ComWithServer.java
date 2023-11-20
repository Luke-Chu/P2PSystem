import tool.GetFormatDate;
import tool.MyStreamSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Date;

public class ComWithServer extends Thread{
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
    public void sendEndMessage() throws IOException {
        myStreamSocket.sendMessage("#");
        myStreamSocket.close();
        this.isStop = true;
        System.out.println("我自己下线通知："+node);
    }
    @Override
    public void run() {
        try {
            Socket socket = new Socket();
            int timeoutPeriod = 5000;
            socket.connect(socketAddress,timeoutPeriod);    //直到连接成功才进行数据传送
            //将自己信息打包发送给服务器
            myStreamSocket = new MyStreamSocket(socket);
            myStreamSocket.sendMessage(node.toString());

            client.setChatRecord(GetFormatDate.getFormatDate(new Date())+"恭喜您 \""+node.username+"\" 与服务器连接成功！");
            while (!isStop){
                String originalMessage = myStreamSocket.receiveMessage();
                String[] userList = originalMessage.split("@@");
                switch (userList[0]) {
                    case "上线通知" -> {
                        String[] onlineUserInfo = userList[1].split("&");
                        String onlineUserName = onlineUserInfo[0];
                        InetAddress onlineUserIP = InetAddress.getByName(onlineUserInfo[1]);
                        int onlineUserListenPort = Integer.parseInt(onlineUserInfo[2]);
                        Node tempNode = new Node(onlineUserName, onlineUserIP, onlineUserListenPort);
                        userInfo.addUser(tempNode);
                        client.updateList(userInfo);
                        System.out.println("上线通知："+node);
                        client.setChatRecord(GetFormatDate.getFormatDate(new Date())+onlineUserName + "已上线！");
                    }
                    case "下线通知" -> {
                        String[] offlineUserInfo = userList[1].split("&");
                        String offlineUserName = offlineUserInfo[0];
                        InetAddress offlineUserIP = InetAddress.getByName(offlineUserInfo[1]);
                        int offlineUserListenPort = Integer.parseInt(offlineUserInfo[2]);
                        Node tempNode = new Node(offlineUserName, offlineUserIP, offlineUserListenPort);
                        userInfo.deleteUser(offlineUserName);
                        client.updateList(userInfo);
                        System.out.println("下线通知："+node);
                        client.setChatRecord(GetFormatDate.getFormatDate(new Date())+offlineUserName + "已下线！");
                    }
                    case "更新列表" -> {
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
                        System.out.println("更新列表："+userInfo);
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
