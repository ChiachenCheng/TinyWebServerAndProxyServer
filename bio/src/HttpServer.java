import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpServer {
    private static final int port = 8081;
    private static final String webroot = System.getProperty("user.dir") + File.separator + "webroot";
    private static final String shutdown_commamd = "/shutdown";
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(32, 64, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.await();
    }

    public void await() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        Socket socket = null;
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
            executor.execute(new Server(socket));
        }
        //serverSocket.close();
    }

}