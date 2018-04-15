package com.xm.nio.server;

import java.nio.channels.SocketChannel;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelaySocketChannel implements Delayed {

    private SocketChannel socketChannel;

    private long delta;

    private long trigger;

    public DelaySocketChannel(SocketChannel socketChannel,long milliSecond){
        this.socketChannel = socketChannel;
        this.delta = milliSecond;
        this.trigger = System.nanoTime()+TimeUnit.NANOSECONDS.convert(delta,TimeUnit.MILLISECONDS);
    }

    public void refresh(){
        this.trigger = System.nanoTime()+TimeUnit.NANOSECONDS.convert(delta,TimeUnit.MILLISECONDS);
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public int compareTo(Delayed o) {
        DelaySocketChannel that=(DelaySocketChannel)o;
        if(this.trigger>that.trigger)
            return 1;
        if(this.trigger<that.trigger)
            return -1;
        return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(trigger-System.nanoTime(),TimeUnit.NANOSECONDS);
    }
}
