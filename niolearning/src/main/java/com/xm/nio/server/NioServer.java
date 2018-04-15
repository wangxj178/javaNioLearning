package com.xm.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class NioServer {

    public int id = 100001;
    private Selector selector;

    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.init();
        nioServer.handle();
    }

    private void init() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8787));
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT).attach(id++);
    }

    private void handle(){
        while (true){
            try {
                Thread.sleep(1000);
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()){
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isAcceptable()){
                        handleAccept(key);
                    }else if (key.isReadable()){
                        handleRead(key);
                    }else if (key.isWritable()){
                        handleWrite(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void handleAccept(SelectionKey key) throws IOException {
        SocketChannel clientChannel=((ServerSocketChannel)key.channel()).accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(key.selector(),SelectionKey.OP_READ,ByteBuffer.allocate(1024));
        HeartBeat.getInstance().registerHeartBeat(clientChannel);
    }
    public void handleRead(SelectionKey key) throws IOException {
        // 获得与客户端通信的信道 
        SocketChannel clientChannel=(SocketChannel)key.channel();
        // 得到并清空缓冲区 
        ByteBuffer buffer = (ByteBuffer)key.attachment();
        buffer.clear();
        // 读取信息获得读取的字节数
        long bytesRead=clientChannel.read(buffer);
        if(bytesRead==-1){// 没有读取到内容的情况       
            clientChannel.close();
        }else{
            // 将缓冲区准备为数据传出状态       
            buffer.flip();
            String receivedString=HeartBeat.getString(buffer);
            // 控制台打印出来 
            System.out.println("接收到来自"+clientChannel.socket().getRemoteSocketAddress()+"的信息:"+receivedString);
            // 准备发送的文本 
            String sendString="你好,客户端. @"+new Date().toString()+"，已经收到你的信息"+receivedString;
            buffer=ByteBuffer.wrap(sendString.getBytes("UTF-8"));
            clientChannel.write(buffer);
            // 设置为下一次读取或是写入做准备
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }
    public void handleWrite(SelectionKey key) throws IOException {
        // do nothing   
    }


}
