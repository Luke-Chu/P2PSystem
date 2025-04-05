import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

/**
 * 获取有效的随机端口号
 */
public class RandomPort {
    public static int getAvailableRandomPort(){
        Random random = new Random();
        while (true){
            try {
                /*
                random.nextInt(65535) 生成一个 0 到 65534 范围内的随机数。
                注意：实际有效端口一般范围有 1024 到 65535，由于 0 到 1023 端口通常被保留给系统使用，所以这段代码
                可能需要按照需求，调整端口生成范围。
                 */
                int port = random.nextInt(65535);
                // 尝试在生成的端口上创建 ServerSocket，如果端口已被占用或权限不足，该操作会抛出 IOException
                ServerSocket socket = new ServerSocket(port);
                socket.close();
                return port;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
