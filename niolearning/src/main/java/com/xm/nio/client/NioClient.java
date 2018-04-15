package com.xm.nio.client;

import com.xm.nio.server.HeartBeat;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NioClient {

    public static void main(String[] args) throws Exception {
        ByteBuffer echoBuffer = ByteBuffer.allocate(1024);
        SocketChannel channel = null;
        Selector selector = null;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        // 请求连接
        channel.connect(new InetSocketAddress("localhost", 8787));
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_CONNECT);
        while (true){
            selector.select();
            Set selectedKeys = selector.selectedKeys();
            Iterator it = selectedKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                it.remove();
                if (key.isConnectable()) {
                    if (channel.isConnectionPending()) {
                        if (channel.finishConnect()) {
                            // 只有当连接成功后才能注册OP_READ事件
                            key.interestOps(SelectionKey.OP_READ);
                            echoBuffer.put("123456789abcdefghijklmnopq".getBytes());
                            echoBuffer.flip();
                            System.out.println("##" + HeartBeat.getString(echoBuffer));
                            channel.write(echoBuffer);
                            System.out.println("写入完毕");
                        } else {
                            key.cancel();
                        }
                    }
                }else if (key.isReadable()){
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = channel.read(buffer);
                    if (count > 0){
                        buffer.flip();
                        System.out.println("##" + HeartBeat.getString(buffer));
                    }
                }
            }
        }
    }

}
