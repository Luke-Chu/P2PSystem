import tool.MyStreamSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;

/**
 * 客户端发送消息线程
 */
public class ClientSendThread extends Thread{
    private final List<String> selectName;
    private final UserInfo userInfo;
    private final String myName;
    private final String sendMessage;
    public ClientSendThread(List<String> selectName, UserInfo userInfo,String name,String sendMessage) {
        this.selectName = selectName;
        this.userInfo = userInfo;
        this.myName = name;
        this.sendMessage = sendMessage;
    }

    @Override
    public void run() {
        try {
            for (String name:selectName){
                Node acceptorNode = userInfo.searchUserByName(name);
                SocketAddress socketAddress = new InetSocketAddress(acceptorNode.ip, acceptorNode.port);
                Socket socket = new Socket();
                socket.connect(socketAddress);
                //连接成功则发送消息
                MyStreamSocket myStreamSocket = new MyStreamSocket(socket);
                myStreamSocket.sendMessage(myName+"&"+sendMessage);
                myStreamSocket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
