import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptCompleteHandler implements CompletionHandler<AsynchronousSocketChannel, AioServerHandle> {
    //第一部分
    private AsynchronousServerSocketChannel serverSocketChannel;
    public AcceptCompleteHandler(AsynchronousServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }
    //第二部分
    @Override
    public void completed(final AsynchronousSocketChannel channel, AioServerHandle attachment) {
        //第二部分第一小节
        attachment.serverSocketChannel.accept(attachment, this);
        System.out.println("有客户端链接进来");
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        //第二部分第二小节
        channel.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.flip();
                byte[] bytes = new byte[attachment.remaining()];
                attachment.get(bytes);
                System.out.println("客户端发送来的数据是：" + new String(bytes));

                Request request = new Request(new String(bytes));
                if (request.getUri().equals("/shutdown")) {
                    //serverSocketChannel.close();
                    System.exit(0);
                }

                String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
                FileInputStream fis = null;
                String response = null;
                try {
                    String uri = request.getUri();
                    if(uri.equals("/")){
                        uri = "/index.html";
                    }
                    File file = new File(WEB_ROOT, uri);
                    if(file.exists()) {
                        response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n";
                        fis = new FileInputStream(file);
                        System.out.println("test1");
                        int size = fis.available();
                        byte[] buffer = new byte[size];
                        fis.read(buffer);
                        fis.close();
                        String temp = new String(buffer,"UTF-8");
                        response = response + temp;
                        System.out.println("test2");
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
                            File errFile = new File(WEB_ROOT, "404.html");
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
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                //System.out.println("客户端发送来的数据是：" + new String(bytes));
                //String sendMsg = "服务端返回的数据：java的架构师技术栈";
                System.out.println("test3");
                String sendMsg = response;
                System.out.println(sendMsg);
                ByteBuffer writeBuffer = ByteBuffer.allocate(sendMsg.getBytes().length);
                writeBuffer.put(sendMsg.getBytes());
                writeBuffer.flip();
                channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                    @Override
                    public void completed(Integer result, ByteBuffer attachment) {
                        if (attachment.hasRemaining()) {
                            channel.write(attachment, attachment, this);
                        } else {
                            try{
                                channel.close();
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    @Override
                    public void failed(Throwable exc, ByteBuffer attachment) {
                        try {
                            System.out.println("服务端出现写数据异常");
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            //第二部分第三小节
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                try {
                    System.out.println("服务端读取数据异常");
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //第三部分
    @Override
    public void failed(Throwable exc, AioServerHandle attachment) {
        System.out.println("服务端链接异常");
    }
}