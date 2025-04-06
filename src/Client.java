import tool.GetFormatDate;
import tool.MyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serial;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * 客户端界面
 */
public class Client extends JFrame{
    @Serial
    private static final long serialVersionUID = 7440762172509840123L;
    private final StringBuilder chatRecord;       //聊天记录，用于保存和累计聊天记录文本，便于实时更新和显示聊天内容
    private Vector<String> onlinePeople;    //在线用户，用于存放在线用户的用户名列表，并通过 JList 进行展示
    private String userName = "";                //用户名，保存当前客户端用户的用户名，后续登录时会获取该输入框中的内容
    private JTextArea chatRecordTextArea;   //聊天记录组件，显示聊天记录的多行文本区域，设置为不可编辑
    private JTextArea sendMessageTextArea;  //发送消息组件，用于输入待发送消息的区域
    private JTextField onlineCountTextFile; //在线人数组件，用于展示在线用户列表
    private JList<String> onlinePeopleList;         //在线用户组件
    private JTextField userNameTextFile;    //用户名组件
    private JButton sendMessageButton;      //发送消息组件
    private JButton clearMessageButton;     //清空消息组件
    private JButton userLog;                //用户登录组件
    private JButton userExit;               //用户退出组件
    private Node node;                    //用户结点
    private final UserInfo userInfo;            //在线用户列表信息
    private ComWithServer comWithServer;   //和服务器之间通信线程
    private final boolean isStop;         //是否关闭客户端了
    //构造函数
    public Client(String title) throws HeadlessException {
        super(title);
        this.setSize(700,500);        //窗口大小
        this.setLocationRelativeTo(null);         // 绝对布局
        this.setResizable(false);                  //不可变大小
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //关闭按钮

        JPanel panel = new JPanel();
        panel.setLayout(null);
        this.init(panel);
        this.getContentPane().add(panel);
        this.setVisible(true);               //可见
        this.eventListener();
        chatRecord = new StringBuilder();
        isStop = false;
        userInfo = new UserInfo();
    }

    public void init(JPanel panel){
        JLabel label1 = new JLabel("聊天记录");
        label1.setBounds(5,0,492,25);
        panel.add(label1);

        chatRecordTextArea = new JTextArea();
        chatRecordTextArea.setEditable(false);
        chatRecordTextArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane chatRecordScrollPanel = new JScrollPane(chatRecordTextArea);
        chatRecordScrollPanel.setBounds(5,26,492,300);
        chatRecordScrollPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.add(chatRecordScrollPanel);

        sendMessageTextArea = new JTextArea();
        sendMessageTextArea.setBackground(Color.LIGHT_GRAY);
        JScrollPane sendMessageScrollPane = new JScrollPane(sendMessageTextArea);
        sendMessageScrollPane.setBounds(5,330,492,100);
        panel.add(sendMessageScrollPane);

        sendMessageButton = new JButton();
        sendMessageButton.setText("发送");
        sendMessageButton.setEnabled(false);
        sendMessageButton.setBounds(5,430,200,30);
        panel.add(sendMessageButton);

        clearMessageButton = new JButton();
        clearMessageButton.setText("清除");
        clearMessageButton.setEnabled(false);
        clearMessageButton.setBounds(295,430,200,30);
        panel.add(clearMessageButton);

        JLabel label2 = new JLabel("在线列表");
        label2.setBounds(500,0,182,25);
        panel.add(label2);

        onlineCountTextFile = new JTextField();
        onlineCountTextFile.setText("在线用户0人");
        onlineCountTextFile.setBounds(500,26,182,25);
        onlineCountTextFile.setBackground(Color.LIGHT_GRAY);
        onlineCountTextFile.setOpaque(true);
        panel.add(onlineCountTextFile);

        onlinePeople = new Vector<>();
        onlinePeopleList = new JList<>(onlinePeople);
        onlinePeopleList.setBounds(500,52,182,274);
        onlinePeopleList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        onlinePeopleList.setBackground(Color.lightGray);
        panel.add(onlinePeopleList);

        JLabel label3 = new JLabel("用户名：");
        label3.setBounds(500,330,182,32);
        panel.add(label3);

        userNameTextFile = new JTextField();
        userNameTextFile.setText(userName);
        userNameTextFile.setBounds(500,365,182,32);
        userNameTextFile.setBackground(Color.LIGHT_GRAY);
        panel.add(userNameTextFile);

        userLog = new JButton();
        userLog.setText("登录");
        userLog.setBounds(500,399,182,30);
        panel.add(userLog);

        userExit = new JButton();
        userExit.setText("退出");
        userExit.setEnabled(false);
        userExit.setBounds(500,430,182,30);
        panel.add(userExit);
    }
    public void eventListener(){
        //发送消息
        sendMessageButton.addActionListener(e -> {
            try {
                sendMessage();
            } catch (MyException ex) {
                setChatRecord(ex.getMessage()+"\n");
            }
        });

        //清空消息
        clearMessageButton.addActionListener(e -> sendMessageTextArea.setText(null));

        //登录
        userLog.addActionListener(e -> {
            try {
                if (!userNameTextFile.getText().isEmpty()) {
                    login();
                }
            } catch (IOException ex) {
                setChatRecord("用户名不能为空");
            }
        });

        //退出
        userExit.addActionListener(e -> {
            try {
                comWithServer.sendEndMessage();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.exit(0);
        });

        /*
         * 建议给窗口关闭按钮写一个事件，
         * 如果不写，每次关闭窗口都需要点击推出按钮，
         * 直接点击右上角的X按钮，控制台会抛出异常，且服务器不会更新列表
         * 建议再添加该事件，有助于增强程序的健壮性
         * 窗口关闭事件应该跟 void windowClosing(Win dowEvent e)方法相关
         */
        //我想了下，继续写一写，所以有了下面这段代码，应该没问题(服务器也要写一个)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    comWithServer.sendEndMessage();
                    dispose();  //会调用 windowClosed(WindowEvent e) 方法
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

    }
    // 追加显示聊天区信息
    public void setChatRecord(String chatRecord) {
        this.chatRecord.append(chatRecord).append("\n");
        chatRecordTextArea.setText(String.valueOf(this.chatRecord));
    }
    // 聊天区信息整块设置
    public void setChatRecord(StringBuilder chatRecord) {
        chatRecordTextArea.setText(String.valueOf(chatRecord));
    }
    public String getUserName() {
        userName = userNameTextFile.getText().trim();   // 获取用户名，去除两端空格后返回
        return userName;
    }
    public boolean isStop() {
        return isStop;
    }
    public void login() throws IOException {
        String localhost = InetAddress.getLocalHost().getHostAddress();
        InetAddress inetAddress = InetAddress.getLocalHost();   // 获取本机 IP 地址
        System.out.println(inetAddress);
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, 1234);
        int myReceivePort = RandomPort.getAvailableRandomPort();    // 获取一个可用的端口号，作为客户端接收消息的端口
        System.out.println("receivePort--->"+myReceivePort);

        node = new Node(getUserName(),InetAddress.getByName(localhost),myReceivePort);  // 构造节点
        comWithServer = new ComWithServer(node,userInfo,socketAddress,this);
        comWithServer.start();  // 启动通信线程

        // 界面状态调整：登登录后，启用发送、清空消息、退出客户端按钮，同时禁用登录按钮和用户名输入框，防止用户重复登录
        sendMessageButton.setEnabled(true);
        clearMessageButton.setEnabled(true);
        userExit.setEnabled(true);
        userLog.setEnabled(false);
        userNameTextFile.setEditable(false);

        // 启动接收线程，在该 P2P 模式中，客户端同时也为服务器端，用于异步接收外来信息
        ClientReceiveThread clientReceiveThread = new ClientReceiveThread(node,this);
        clientReceiveThread.setName("--"+node.username+"的接收线程--");
        clientReceiveThread.setDaemon(true);    // 设为保护线程，确保在主进程退出时也能自动结束
        clientReceiveThread.start();    // 启动接收线程
    }
    public void sendMessage() throws MyException {
        // 获取待发送的消息
        String sendMsg = sendMessageTextArea.getText();
        System.out.println("Input Message From Client: "+sendMsg);
        if (sendMsg.isEmpty())
            throw new MyException("消息不能为空!");

        // 获取待发送消息的目标用户
        List<String> selectName = onlinePeopleList.getSelectedValuesList();
        ClientSendThread clientSendThread = new ClientSendThread(selectName,userInfo, node.username,sendMsg);
        clientSendThread.start();   // 启动发送进程

        // 更新发送方的聊天记录
        StringBuilder select = new StringBuilder();
        for (String strName:selectName)
            select.append(strName).append("、");
        chatRecord.append(GetFormatDate.getFormatDate(new Date()));
        chatRecord.append(node.username).append("--->").append(select.deleteCharAt(select.length()-1)).append("\n");
        chatRecord.append(sendMsg).append("\n");
        setChatRecord(chatRecord);
        sendMessageTextArea.setText(null);  // 清空消息输入区，方便下一次输入
    }
    public void updateList(UserInfo userInfo){
        int count = userInfo.getCount();
        onlinePeople.clear();
        if (count > 0){
            for (int i = 0; i < count; i ++){
                Node tempNode = userInfo.searchUserByIndex(i);
                onlinePeople.add(tempNode.username);
            }
            onlinePeopleList.setListData(onlinePeople);
        }
        //在线人数
        String onlineCount = "在线用户" + count + "人";
        onlineCountTextFile.setText(onlineCount);
    }
}