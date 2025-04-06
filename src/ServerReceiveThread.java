import tool.GetFormatDate;
import tool.MyStreamSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 服务器接收线程
 */
public class ServerReceiveThread extends Thread {
    /*
    成员变量说明
        Node node：用于保存当前连接的客户端节点信息。后续通过解析消息构造该对象。
        UserInfo userInfo：保存在线用户的信息集合，负责管理所有连接上来的用户。
        MyStreamSocket myStreamSocket：封装了 Socket 的输入输出操作，提供便捷的方法（如 sendMessage 和 receiveMessage），使得数据传输更简单且一致。
        StringBuilder systemLog：用于维护服务器的系统日志，记录连接、下线等事件，并会在 GUI 界面上显示。
        Server server：对主服务器的引用，便于调用服务器的状态（如 isStop()）和更新界面（例如更新在线用户列表或系统日志）。
     */
    private Node node;
    private final UserInfo userInfo;
    private final MyStreamSocket myStreamSocket;
    private final StringBuilder systemLog;
    private final Server server;

    public ServerReceiveThread(UserInfo userInfo, Socket socket, StringBuilder systemLog, Server server) throws IOException {
        this.userInfo = userInfo;
        this.myStreamSocket = new MyStreamSocket(socket);
        this.systemLog = systemLog;
        this.server = server;
    }

    public void updateInformation(StringBuilder userList) throws IOException {
        myStreamSocket.sendMessage(String.valueOf(userList));
    }

    @Override
    public void run() {
        /*
        Timer 新建一个守护定时器（参数 true 表示是守护线程），用来定期检测并推送最新的用户状态通知信息。
        Date 对象 获取当前时间作为定时器任务的开始时间。
        isStop 标志 用于在循环中判断结束条件，尽管该变量在 run() 中只做了局部使用（注意与服务器整体的停止标志不同）。
         */
        Timer timer = new Timer(true);
        Date date = new Date();
        boolean isStop = false;
        try {
            String[] message = myStreamSocket.receiveMessage().split("&");
            node = new Node(message[0], InetAddress.getByName(message[1]), Integer.parseInt(message[2]));
            System.out.println(message[1]);

            userInfo.addUser(node);
            String timeStamp = GetFormatDate.getFormatDate(new Date());
            systemLog.append(timeStamp);    // 生成时间戳
            systemLog.append("用户\"").append(message[0]).append("\"连接成功！\n");

            // 此处添加了分支处理，提高了日志信息的提示完整性
            if (userInfo.getCount() > 0){
                systemLog.append("当前在线用户：");
                for (int i = 0; i < userInfo.getCount(); i++) {
                    systemLog.append(userInfo.searchUserByIndex(i).username).append(" ");
                }
                systemLog.append("\n");
            }
            else {
                systemLog.append("当前在线用户：暂无\n");
            }
            /*
            通知与状态更新：
                设置 server.onlineMessage 为新的上线通知消息。
                调用 userInfo.setOnlineStatus(true) 表示有所有用户列表中的用户的 isOnlineInfo 上线通知为 true。
                将节点自身的 isOnlineInfo 标志设为 false（避免向自己发送通知）。
                更新服务器界面的系统日志与在线用户列表。
             */
            server.onlineMessage = new OnlineOfflineMessage(node);
            userInfo.setOnlineStatus(true);
            node.setOnlineInfo(false);  //不给自己发通知，自己需要更新列表
            server.setSystemLog(systemLog);
            server.updateList(userInfo);

            /*
            定时任务设置： 调用 timer.schedule 注册一个定时任务，该任务将在 date 定时开始，并以 1000 毫秒（1秒）的周期重复执行。
            任务内部逻辑： 在每次执行时，利用一个 StringBuilder 构造需要发送的“通知消息”。
                如果节点的 isOfflineInfo 为 true，则构造“下线通知”，并将失败标志清除。
                如果节点的 isOnlineInfo 为 true，则构造“上线通知”，并重置该状态。
                如果节点的 isJustOnline 为 true（刚上线状态），则构造“更新列表”消息，将当前用户信息发送出去，并设为 false。
            更新信息： 每次构造好通知消息后，调用 updateInformation 方法，通过封装的输出流将消息发送给该连接的客户端。
             */
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    StringBuilder userList = new StringBuilder();
                    if (node != null && node.isOfflineInfo()) {
                        userList.append("下线通知@@");
                        userList.append(server.offlineMessage.node().toString());
                        node.setOfflineInfo(false); //  确保只会触发一次
                        try {
                            updateInformation(userList);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (node != null && node.isOnlineInfo()) {
                        userList.append("上线通知@@");
                        userList.append(server.onlineMessage.node().toString());
                        node.setOnlineInfo(false);  //  确保只会触发一次
                        try {
                            updateInformation(userList);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else if (node != null && node.isJustOnline()) {
                        userList.append("更新列表@@");
                        userList.append(userInfo);
                        node.setJustOnline(false);  //  确保只会触发一次
                        try {
                            updateInformation(userList);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, date, 1000);

            /*
            循环监听： 使用 while (!isStop) 循环不断调用 myStreamSocket.receiveMessage() 接收客户端发来的消息。
            处理特殊消息“#”： 当收到消息“#”，表示客户端要求下线或断开连接。此时：
                向客户端发送确认信息（“#@@”）。
            从 userInfo 中注销该用户；更新服务器在线用户列表与界面显示。
                日志记录下线操作，并在系统日志中显示哪些用户仍在线。
                同时生成新的离线通知消息（通过 OnlineOfflineMessage），并将信息状态设为离线。
            关闭当前套接字连接；将局部 isStop 置为 true，退出循环，结束当前线程的运行。
             */
            String msg;
            while (!isStop) {
                msg = myStreamSocket.receiveMessage();
                if ("#".equals(msg)) {
                    myStreamSocket.sendMessage("#@@");
                    userInfo.deleteUser(node);
                    server.updateList(userInfo);
                    String timestamp = GetFormatDate.getFormatDate(new Date());
                    systemLog.append(timestamp);
                    systemLog.append(node.username).append("已下线\n");

                    // 此处添加了分支处理，提高了日志信息的提示完整性
                    if (userInfo.getCount() > 0){
                        systemLog.append("当前在线用户：");
                        for (int i = 0; i < userInfo.getCount(); i++) {
                            systemLog.append(userInfo.searchUserByIndex(i).username).append(" ");
                        }
                        systemLog.append("\n");
                    }
                    else {
                        systemLog.append("当前在线用户：暂无\n");
                    }

                    server.setSystemLog(systemLog);
                    System.out.println("Offline Notification From Server: "+node.username);
                    //准备发送给所以在线客户
                    server.offlineMessage = new OnlineOfflineMessage(node);
                    userInfo.setOfflineStatus(true);
                    node = null;
                    myStreamSocket.close();
                    isStop = true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
