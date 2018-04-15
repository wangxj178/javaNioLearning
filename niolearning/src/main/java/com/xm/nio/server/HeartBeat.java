package com.xm.nio.server;

import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.DelayQueue;

/**
 * 保存客户端连接信息，每隔30秒发送一次心跳
 */
public class HeartBeat implements Runnable{
    //单例
    private static HeartBeat heartBeat = null;
    //客户端延迟队列
    private static DelayQueue<DelaySocketChannel> delayQueue = new DelayQueue<DelaySocketChannel>();
    //心跳间隔时间，单位：毫秒
    private final long delta = 30000;
    public static HeartBeat getInstance(){
        if (heartBeat == null){
            synchronized (delayQueue){
                heartBeat = new HeartBeat();
                new Thread(heartBeat).start();
            }
        }
        return heartBeat;
    }

    public void registerHeartBeat(SocketChannel socketChannel){
        if (socketChannel == null) return;
        DelaySocketChannel delaySocketChannel = new DelaySocketChannel(socketChannel,delta);
        delayQueue.add(delaySocketChannel);
    }


    @Override
    public void run() {
        while (true){
            DelaySocketChannel delaySocketChannel=null;
            try {
                delaySocketChannel = delayQueue.take();
                if (delaySocketChannel.getSocketChannel().isConnected()){
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    String timeStr = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
                    String heartBeatStr = timeStr+": heart beat ...";
                    buffer.put(heartBeatStr.getBytes());
                    buffer.flip();
                    delaySocketChannel.getSocketChannel().write(buffer);
                    delaySocketChannel.refresh();//刷新下一次执行的时间
                    delayQueue.add(delaySocketChannel);
                }else{
                    delaySocketChannel.getSocketChannel().close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
                try {
                    if (delaySocketChannel.getSocketChannel() != null)
                        delaySocketChannel.getSocketChannel().close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public static String getString(ByteBuffer buffer)
    {
        String string = "";
        try
        {
            for(int i = 0; i<buffer.limit();i++){
                string += (char)buffer.get(i);
            }
            return string;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }
}
