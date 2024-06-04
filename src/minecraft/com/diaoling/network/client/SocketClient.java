package com.diaoling.network.client;

import com.diaoling.utils.misc.enums.ConnectionState;
import com.diaoling.network.packet.Packet;
import com.diaoling.utils.misc.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * @author DiaoLing
 * @since 4/7/2024
 */
public class SocketClient {
    private final String host;
    private final int port;
    private Channel channel;
    private ExecutorService executorService;
    private final AtomicReference<ConnectionState> connectionState = new AtomicReference<>(ConnectionState.DISCONNECTED);

    public SocketClient() {
        this("localhost", 45600);
    }

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void start() {
        start(this.host, this.port);
    }

    public synchronized void start(String host, int port) {
        if (isConnected() || isConnecting()) {
            Logger.warning("Client is already connected or connecting.");
            return;
        }
        connectionState.set(ConnectionState.CONNECTING);
        executorService.submit(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ClientInitializer())
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

                int retryDelay = 5000;
                while (!Thread.interrupted()) {
                    try {
                        ChannelFuture future = bootstrap.connect(host, port).sync();
                        this.channel = future.channel();
                        connectionState.set(ConnectionState.CONNECTED);
                        future.channel().closeFuture().sync();
                        break;
                    } catch (InterruptedException e) {
                        Logger.warning("Interrupted during connection. Exiting.");
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        Logger.info("Connection failed. Retrying in " + retryDelay + " milliseconds...");
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(retryDelay));
                        retryDelay = Math.min(retryDelay * 2, 15000);
                    }
                }
            } finally {
                connectionState.set(ConnectionState.DISCONNECTED);
                group.shutdownGracefully();
            }
        });
    }

    public void send(Packet packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet).addListener(future -> {
                if (!future.isSuccess()) {
                    Logger.error("Failed to send packet: " + future.cause().getMessage());
                }
            });
        } else {
            Logger.warning("Channel is not active. Cannot send packet.");
        }
    }

    public synchronized void disconnect() {
        if (channel != null && channel.isOpen()) {
            channel.close().addListener(future -> {
                if (future.isSuccess()) {
                    Logger.info("Disconnected successfully.");
                } else {
                    Logger.warning("Failed to disconnect.");
                }
                connectionState.set(ConnectionState.DISCONNECTED);
            });
        } else {
            Logger.info("Channel is already closed or not initialized.");
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                Logger.error("Executor did not terminate in the allotted time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isConnected() {
        return connectionState.get() == ConnectionState.CONNECTED && this.channel.isActive();
    }

    public boolean isConnecting() {
        return connectionState.get() == ConnectionState.CONNECTING;
    }

    public Channel getChannel() {
        return channel;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ConnectionState getConnectionState() {
        return connectionState.get();
    }

    private void setConnectionState(ConnectionState newState) {
        connectionState.set(newState);
        Logger.info("Connection state changed to: " + newState);
    }
}
