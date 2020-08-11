import java.io.InputStream;

public class Request {
    private final static int BUFFER_SIZE = 1024;
    private InputStream input;
    private String uri = null;
    private String content = null;

    public Request(InputStream input) {
        this.input = input;
        StringBuffer request = new StringBuffer();
        int readLength;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            readLength = input.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            readLength = -1;
        }
        for(int i = 0; i < readLength; i++) {
            request.append((char)buffer[i]);
        }
        content = request.toString();
        System.out.print(request.toString());
    }

    public String getUri() {
        if (uri != null) return uri;
        int index1, index2;
        index1 = content.indexOf(' ');
        if (index1 != -1) {
            index2 = content.indexOf(' ', index1 + 1);
            if (index2 > index1)
                uri = content.substring(index1 + 1, index2);
                return uri;
        }
        return null;
    }

    public String getContent() {
        return content;
    }

    public String changePath(String requestString,String path) {
        String temp = null;
        int len = requestString.length();
        int index1, index2;
        index1 = requestString.indexOf(' ');
        if (index1 != -1) {
            index2 = requestString.indexOf(' ', index1 + 1);
            if (index2 > index1)
                temp = requestString.substring(0, index1 + 1) + path + requestString.substring(index2, len);
        }
        return temp;
    }
    public String changeUrl(String requestString,String name,String data){
        int index1,index2;
        String temp = null;
        int len= requestString.length();
        index1 = requestString.indexOf(name);
        if (index1 != -1) {
            index2 = requestString.indexOf("\r\n", index1 + name.length()+1);
            if (index2 > index1)
                temp = requestString.substring(0, index1+name.length()) + data + requestString.substring(index2+1, len);
        }
        return temp;
    }
}