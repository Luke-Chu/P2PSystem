import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;

public class Node implements Serializable {
    @Serial
    private static final long serialVersionUID = -2496531371087020697L;
    String username;       //用户名
    InetAddress ip;             //IP地址
    int port;                   //端口号
    private boolean isOnlineInfo;   //上线通知
    private boolean isOfflineInfo;  //下线通知
    private boolean isJustOnline;  //是否刚上线

    public Node(String username, InetAddress ip, int port) {
        this.username = username;
        this.ip = ip;
        this.port = port;
        /*
        isOfflineInfo 和 isOnlineInfo 均初始化为 false，表示初始状态下没有发送上线或下线通知
        isJustOnline 设为 true，意味着该节点刚刚上线，需要触发事件（更新通讯列表）
         */
        isOfflineInfo = false;
        isOnlineInfo = false;
        isJustOnline = true;
    }

    public boolean isOnlineInfo() {
        return isOnlineInfo;
    }

    public void setOnlineInfo(boolean onlineInfo) {
        isOnlineInfo = onlineInfo;
    }

    public boolean isOfflineInfo() {
        return isOfflineInfo;
    }

    public void setOfflineInfo(boolean offlineInfo) {
        isOfflineInfo = offlineInfo;
    }

    public boolean isJustOnline() {
        return isJustOnline;
    }

    public void setJustOnline(boolean justOnline) {
        isJustOnline = justOnline;
    }

    @Override
    public String toString() {
        return username+"&"+ip.toString().substring(1)+"&"+port;
    }

}
