public class ClientTest03 {
    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> new Client("P2PChat客户端3"));
        thread1.start();
    }
}
