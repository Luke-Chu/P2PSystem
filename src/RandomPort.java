import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * 获取随机端口号
 */
public class RandomPort {
    public static int getAvailableRandomPort(){
        Random random = new Random();
        while (true){
            try {
                int port = random.nextInt(65535);
                ServerSocket socket = new ServerSocket(port);
                socket.close();
                return port;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
