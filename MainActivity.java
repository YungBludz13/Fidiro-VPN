package com.fidiro.vpn;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.net.VpnService;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private static final int VPN_REQUEST_CODE = 100;
    private Button connectButton, disconnectButton;
    private TextView statusText;
    private FidiroVPNService vpnService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        statusText = findViewById(R.id.statusText);
        
        connectButton.setOnClickListener(v -> startVPN());
        disconnectButton.setOnClickListener(v -> stopVPN());
        
        checkPermissions();
    }
    
    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.INTERNET}, 1);
        }
    }
    
    private void startVPN() {
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent vpnIntent = new Intent(this, FidiroVPNService.class);
            startService(vpnIntent);
            statusText.setText("VPN Connected");
            Toast.makeText(this, "Fidiro VPN Connected", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void stopVPN() {
        Intent vpnIntent = new Intent(this, FidiroVPNService.class);
        stopService(vpnIntent);
        statusText.setText("VPN Disconnected");
        Toast.makeText(this, "Fidiro VPN Disconnected", Toast.LENGTH_SHORT).show();
    }
}