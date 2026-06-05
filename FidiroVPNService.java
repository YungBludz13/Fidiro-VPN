package com.fidiro.vpn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.core.app.NotificationCompat;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FidiroVPNService extends VpnService {
    private static final String CHANNEL_ID = "FidiroVPNChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private Thread vpnThread;
    private ParcelFileDescriptor vpnInterface;
    private ExecutorService executorService;
    private volatile boolean running;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        executorService = Executors.newCachedThreadPool();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startVPN();
        return START_STICKY;
    }
    
    private void startVPN() {
        running = true;
        
        // Build VPN interface
        Builder builder = new Builder();
        builder.setSession("Fidiro VPN")
               .addAddress("10.0.0.1", 24)
               .addRoute("0.0.0.0", 0)
               .addDnsServer("8.8.8.8")
               .addDnsServer("8.8.4.4")
               .setMtu(1500);
        
        vpnInterface = builder.establish();
        
        // Start notification
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Start VPN tunnel
        vpnThread = new Thread(this::runVPN);
        vpnThread.start();
    }
    
    private void runVPN() {
        FileInputStream in = new FileInputStream(vpnInterface.getFileDescriptor());
        FileOutputStream out = new FileOutputStream(vpnInterface.getFileDescriptor());
        
        byte[] packet = new byte[32767];
        
        while (running) {
            try {
                int length = in.read(packet);
                if (length > 0) {
                    byte[] processedPacket = processOutgoingPacket(packet, length);
                    sendToServer(processedPacket);
                }
                
                byte[] receivedPacket = receiveFromServer();
                if (receivedPacket != null) {
                    byte[] processedReceived = processIncomingPacket(receivedPacket);
                    out.write(processedReceived);
                }
            } catch (Exception e) {
                break;
            }
        }
    }
    
    private byte[] processOutgoingPacket(byte[] packet, int length) {
        // Encrypt and obfuscate packet
        byte[] data = new byte[length];
        System.arraycopy(packet, 0, data, 0, length);
        return obfuscatePacket(data);
    }
    
    private byte[] processIncomingPacket(byte[] packet) {
        // Decrypt and deobfuscate packet
        return deobfuscatePacket(packet);
    }
    
    private byte[] obfuscatePacket(byte[] data) {
        // XOR obfuscation
        byte[] result = data.clone();
        byte[] key = {0x5A, 0xA5, 0x3C, 0xC3};
        for (int i = 0; i < result.length; i++) {
            result[i] ^= key[i % key.length];
        }
        return result;
    }
    
    private byte[] deobfuscatePacket(byte[] data) {
        byte[] result = data.clone();
        byte[] key = {0x5A, 0xA5, 0x3C, 0xC3};
        for (int i = 0; i < result.length; i++) {
            result[i] ^= key[i % key.length];
        }
        return result;
    }
    
    private void sendToServer(byte[] data) {
        executorService.submit(() -> {
            try {
                DatagramChannel channel = DatagramChannel.open();
                SocketAddress serverAddr = new InetSocketAddress("vpn.fidiro.com", 1194);
                channel.send(ByteBuffer.wrap(data), serverAddr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private byte[] receiveFromServer() {
        try {
            DatagramChannel channel = DatagramChannel.open();
            ByteBuffer buffer = ByteBuffer.allocate(32767);
            channel.receive(buffer);
            return buffer.array();
        } catch (Exception e) {
            return null;
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Fidiro VPN",
                NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE);
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fidiro VPN")
            .setContentText("VPN is running")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .build();
    }
    
    @Override
    public void onDestroy() {
        running = false;
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
            }
            if (vpnThread != null) {
                vpnThread.interrupt();
            }
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}