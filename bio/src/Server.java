import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

public class Server implements Runnable{
    private static final int port = 8081;
    private static final String webroot = System.getProperty("user.dir") + File.separator + "webroot";
    private static final String shutdown_commamd = "/shutdown";
    private static Socket socket;
    private InputStream input = null;
    private OutputStream output = null;

    Server(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();

            // 创建Request对象并解析
            Request request = new Request(input);
            // 检查是否是关闭服务命令
            if (request.getUri().equals(shutdown_commamd)) {
                socket.close();
                System.exit(0);
            }

            String response = null;
            FileInputStream fis = null;
            try {
                String uri = request.getUri();
                if(uri.equals("/")){
                    uri = "/index.html";
                }
                File file = new File(webroot, uri);
                if(file.exists()) {
                    response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n";
                    fis = new FileInputStream(file);
                    int size = fis.available();
                    byte[] buffer = new byte[size];
                    fis.read(buffer);
                    fis.close();
                    String temp = new String(buffer,"UTF-8");
                    response = response + temp;
                } else {
                    URL url = new URL("http://"+(uri.substring(1)));
                    String host = url.getHost();
                    String path = url.getPath();
                    if(path.length()<1)
                        path = "/";
                    try {
                        Socket hostSocket = new Socket(host,80);
                        InputStream hinput = hostSocket.getInputStream();
                        OutputStream houtput = hostSocket.getOutputStream();

                        String send = request.changePath(request.getContent(),path);
                        send = request.changeUrl(send,"Host: ",host);
                        //System.out.println(send);

                        houtput.write(send.getBytes());
                        Request hostrequest = new Request(hinput);
                        response = hostrequest.getContent();
                        hostSocket.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                        File errFile = new File(webroot, "404.html");
                        response = "HTTP/1.1 404 File Not Found\r\n"+"Content-Type: text/html\r\n\r\n";
                        fis = new FileInputStream(errFile);
                        System.out.println("test1");
                        int size = fis.available();
                        byte[] buffer = new byte[size];
                        fis.read(buffer);
                        fis.close();
                        String temp = new String(buffer,"UTF-8");
                        response = response + temp;
                        System.out.println("test2");
                    }
                }
                output.write(response.getBytes());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            // 关闭 socket 对象
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
