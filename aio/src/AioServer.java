class AioServer{
    public static void main(String[] args){
        new AioServerHandle().start();
        try {
            Thread.sleep(10000000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}