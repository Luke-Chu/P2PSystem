public class ClientTest02 {
    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> new Client("P2PChat客户端2"));
        thread1.start();
    }
}
