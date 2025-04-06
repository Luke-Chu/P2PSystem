import tool.MyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serial;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;
import tool.GetFormatDate;
import java.util.Date;

/**
 * 服务器界面
 */
public class Server extends JFrame {
    /*
    使用 serialVersionUID 确保在序列化和反序列化间保持版本兼容性。
    @Serial 注解提高了代码可读性，表明该字段和序列化相关。
     */
    @Serial
    private static final long serialVersionUID = 529570721056537567L;
    private StringBuilder systemLog = new StringBuilder();       //系统记录
    private Vector<String> onlinePeople;    //在线用户
    private JTextArea systemLogTextArea;   //聊天记录组件，显示系统日志的文本区域
    private JTextArea sendMessageTextArea;  //发送消息组件，输入发送信息的文本区域
    private JTextField onlineCountTextFile; //在线人数组件，显示在线人数的文本框
    private JList<String> onlinePeopleList;         //在线用户组件，显示在线用户列表
    private JButton sendMessageButton;      //发送消息组件，点击发送信息的按钮
    private JButton clearMessageButton;     //清空消息组件，清空消息的按钮
    private JButton startServerButton;                //启动服务器组件
    private JButton shutDownServerButton;               //关闭服务器组件
    private boolean isStop;      //是否关闭服务器---线程关闭
    public OnlineOfflineMessage onlineMessage;  //上线消息
    public OnlineOfflineMessage offlineMessage; //下线消息
    private UserInfo userInfo; // 用于消息群发
    private Node node;         // 服务器节点

    public boolean isStop() {
        return isStop;
    }
    public void setSystemLog(StringBuilder systemLog) {
        this.systemLog = systemLog;
        systemLogTextArea.setText(String.valueOf(systemLog));
    }
    public Server(String title) throws HeadlessException {
        super(title);
        this.setSize(700,500);        //窗口大小
        this.setLocationRelativeTo(null);          //居中显示
        this.setResizable(false);                  //不允许调整窗口大小
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //设置点击关闭按钮时退出程序

        JPanel panel = new JPanel();
        panel.setLayout(null);  // 使用绝对布局，故所有组件的位置和尺寸都需要手动设定
        this.init(panel);
        this.getContentPane().add(panel);
        this.setVisible(true);               // 窗口可见
        this.eventListener();                // 添加事务监听
    }

    /*
    init(JPanel panel) 方法：
    该方法用来初始化界面上的各个组件，并设定它们的大小和位置（均通过 setBounds 方法进行绝对定位）：
    系统日志区域：
        JLabel label1 显示“系统记录”。
        systemLogTextArea 用于显示系统的运行日志，并放在一个带边框的滚动窗格 JScrollPane 中，便于查看长日志内容。
    发送消息区域：
        sendMessageTextArea 放在滚动窗格中供用户输入消息，
        sendMessageButton 和 clearMessageButton 分别用于发送消息和清空输入内容。它们在初始化时被禁用（setEnabled(false)），直到服务器启动后才启用。
    在线用户列表区域：
        onlinePeopleList 用于显示当前在线的用户，由一个 Vector 数据源提供；
        onlineCountTextFile 显示当前在线用户数；
        JLabel onlineLabel 用来标识该部分为“在线用户列表”。
    控制服务器启动与停止的按钮：
        startServerButton 与 shutDownServerButton 分别用于启动和关闭服务器。
        初始状态下，“启动服务器”按钮启用，“关闭服务器”按钮禁用。
     */
    public void init(JPanel panel){
        JLabel label1 = new JLabel("系统记录");
        label1.setBounds(5,0,492,25);
        panel.add(label1);

        systemLogTextArea = new JTextArea();
        systemLogTextArea.setEditable(false);
        systemLogTextArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane systemLogScrollPane = new JScrollPane(systemLogTextArea);
        systemLogScrollPane.setBounds(5,26,492,300);
        systemLogScrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(systemLogScrollPane);

        sendMessageTextArea = new JTextArea();
        sendMessageTextArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane sendMessageScrollPane = new JScrollPane(sendMessageTextArea);
        sendMessageScrollPane.setBounds(5,330,492,98);
        panel.add(sendMessageScrollPane);

        sendMessageButton = new JButton();
        sendMessageButton.setText("发送");
        sendMessageButton.setBounds(5,430,200,30);
        sendMessageButton.setEnabled(false);    // 初始化时，处于禁用状态
        panel.add(sendMessageButton);

        clearMessageButton = new JButton();
        clearMessageButton.setText("清除");
        clearMessageButton.setBounds(295,430,200,30);
        clearMessageButton.setEnabled(false);   // 初始化时，处于禁用状态
        panel.add(clearMessageButton);

        JLabel onlineLabel = new JLabel("在线用户列表");
        onlineLabel.setBounds(500,0,182,25);
        panel.add(onlineLabel);

        onlineCountTextFile = new JTextField();
        onlineCountTextFile.setText("在线用户0人");
        onlineCountTextFile.setBounds(500,26,182,25);
        onlineCountTextFile.setBackground(Color.LIGHT_GRAY);
        onlineCountTextFile.setOpaque(true);
        panel.add(onlineCountTextFile);

        onlinePeople = new Vector<>();
        onlinePeopleList = new JList<>(onlinePeople);
        onlinePeopleList.setBounds(500,52,182,340);
        onlinePeopleList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        onlinePeopleList.setBackground(Color.lightGray);
        panel.add(onlinePeopleList);

        startServerButton = new JButton();
        startServerButton.setText("启动服务器");
        startServerButton.setBounds(500,396,182,32);
        panel.add(startServerButton);

        shutDownServerButton = new JButton();
        shutDownServerButton.setText("关闭服务器");
        shutDownServerButton.setBounds(500,430,182,30);
        shutDownServerButton.setEnabled(false);
        panel.add(shutDownServerButton);
    }
    public void eventListener(){
        //发送消息
        sendMessageButton.addActionListener(e -> {
            //TODO 服务器群发消息给客户端（手动选择服务器端用户列表中的用户进行群发操作）
            try {
                sendBroadcastMessage();
            } catch (MyException | UnknownHostException ex) {
                setSystemLog(ex.getMessage());
            }
        });

        //清空消息
        clearMessageButton.addActionListener(e -> sendMessageTextArea.setText(null));

        //启动服务器
        startServerButton.addActionListener(e -> {
            startServer();
        });

        //关闭服务器
        shutDownServerButton.addActionListener(e -> {
            isStop = true;
            System.exit(0);
        });
        //右上角关闭窗口按钮
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isStop = true;
                dispose();  //会调用 windowClosed(WindowEvent e) 方法
            }
        });
        /*
        注：上面服务器的退出事件写得并不好，这个不好我认为不一定是服务器端
        当客户端与服务器端连接时，如果服务器突然退出，客户端会抛出异常，
        这是由于在服务器没有向客户端发送连接断开这类消息来终止连接，此时客户端就被迫断开连接了
        抛出红色的异常 (java.net.SocketException: Connection reset) 看着总是有些心烦，建议好好处理下
         */
    }
    // 追加显示聊天区信息
    public void setSystemLog(String ExceptionRecord) {
        this.systemLog.append(ExceptionRecord).append("\n");
        setSystemLog(systemLog);
    }
    public void sendBroadcastMessage() throws MyException, UnknownHostException {
        // 获取待发送的消息
        String sendMsg = sendMessageTextArea.getText();
        System.out.println("Input Broadcast Message from Server: "+sendMsg);
        if (sendMsg.isEmpty())
            throw new MyException("消息不能为空!");

        // 获取待发送消息的所有目标用户
        List<String> AllUserName = onlinePeopleList.getSelectedValuesList();
        // 初始化服务器端节点信息对象
        ServerSendThread serverSendThread = new ServerSendThread(AllUserName,userInfo, node.username,sendMsg);
        serverSendThread.start();   // 启动发送进程

        // 更新发送方的聊天记录
        StringBuilder select = new StringBuilder();
        for (String strName:AllUserName)
            select.append(strName).append("/");
        systemLog.append(GetFormatDate.getFormatDate(new Date()));
        systemLog.append(node.username).append("(broadcast) to < ");
        for (String strName:AllUserName)
            systemLog.append(strName).append(" ");
        systemLog.append("> --->");
        systemLog.append(sendMsg).append("\n");
        setSystemLog(systemLog);
        sendMessageTextArea.setText(null);  // 清空消息输入区，方便下一次输入
    }
    //启动服务器
    public void startServer(){
        try{
            // 在固定端口 1234 上创建 ServerSocket，以等待客户端连接
            ServerSocket serverSocket = new ServerSocket(1234);      //启动服务器
            systemLog.append("等待连接......"+"\n");    // 日志追加，提示服务器状态
            setSystemLog(systemLog);
            String localhost = InetAddress.getLocalHost().getHostAddress();
            InetAddress inetAddress = InetAddress.getLocalHost();   // 获取本机 IP 地址
            System.out.println(inetAddress);
            node = new Node("server",InetAddress.getByName(localhost),1234);  // 构造节点
            /*
            禁用启动服务器按钮，激活关闭服务器、消息群发和清除信息的按钮
            只有服务器成功启动之后，才能发送信息、清除信息以及关闭服务器
             */
            startServerButton.setEnabled(false);
            shutDownServerButton.setEnabled(true);
            sendMessageButton.setEnabled(true);
            clearMessageButton.setEnabled(true);
            this.isStop = false;
            //在线用户列表
            userInfo = new UserInfo();
            //创建服务器端监听线程，监听客户端的连接请求
            ServerListenThread serverListenThread = new ServerListenThread(userInfo, serverSocket,systemLog,this);
            serverListenThread.setName("--服务器监听线程--");
            serverListenThread.start();
        } catch (IOException e) {
            systemLog.append("error0");
        }
    }
    public void updateList(UserInfo userInfo){
        int count = userInfo.getCount();
        onlinePeople.clear();
        if (count > 0){
            for (int i = 0; i < count; i ++){
                Node tempNode = userInfo.searchUserByIndex(i);
                onlinePeople.add(tempNode.username);
            }
            onlinePeopleList.setListData(onlinePeople); // 更新显示在线用户列表
        }
        //在线人数
        String onlineCount = "在线用户" + count + "人";
        onlineCountTextFile.setText(onlineCount);
//        onlinePeopleList.setListData(onlinePeople); // 此处代码有点冗余，可能是为了确保界面刷新，但通常只需调用一次
    }
}
