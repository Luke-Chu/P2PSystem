public class ClientTest {
    /**
     * 启动一个客户端
     */
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            new Client("P2PChat客户端1");
        });
        thread.start();
    }
}
