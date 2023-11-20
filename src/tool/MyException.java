package tool;

import java.io.Serial;

/**
 * 自定义了一个异常类
 */
public class MyException extends Exception{
    @Serial
    private static final long serialVersionUID = 8488931671802346721L;

    public MyException() {
    }

    public MyException(String message) {
        super(message);
    }
}
