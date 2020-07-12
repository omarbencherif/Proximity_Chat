package com.example.proximitychat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class proto_proximity_chat extends AppCompatActivity implements CardAdapter.ItemClickListener {

    public static final String TAG = "wifidirectdemo";

    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;
    CardAdapter adapter;
    RecyclerView recyclerView;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    List<String> deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;
    Bitmap pp;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_proto_proximity_chat);
        recyclerView = findViewById(R.id.peerRecyclerView);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in

        } else {
            // No user is signed in
        }

        request_Coarse_Location();
        initialWork();
        exqListener();

    }

    /*Requests permission for ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION for versions of
    Android which require permission to be accepted at runtime */
    private void request_Coarse_Location() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x12345);
        }
    }

    // Sets the text in the message box to the message string


    // keep collapsed
    // Allows the user to turn WiFi on/off through the app
    private void exqListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                    btnOnOff.setText("Enable");
                } else {
                    wifiManager.setWifiEnabled(true);
                    btnOnOff.setText("Disable");
                }

            }
        });
        // -------------------------------------------------------------------------------------------------------
        // Allows the user to discover other devices
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Discovers nearby WiFi direct devices
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        //byte[] bytesPhoto = bitmapToBytes(pp);
                        //sendReceive.write(bytesPhoto);
                        connectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Discovery Failed");

                    }
                });
            }
        });

        // Sends the message to the other device

    }

    // keep collapsed
    private byte[] bitmapToBytes(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();
        return byteArray;
    }

    private void initialWork() {
        // initialise views

        btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnSend = findViewById(R.id.sendButton);
        recyclerView = findViewById(R.id.peerRecyclerView);
        read_msg_box = findViewById(R.id.readMsg);
        connectionStatus = findViewById(R.id.connectionStatus);
        writeMsg = findViewById(R.id.writeMsg);

        // initialise wifi p2p objects
        // WifiManager allows us to enable or disable Wifi
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // WifiP2pManager lets an application discover available peers, setup connection to peers and query for the list of peers
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        // A channel that connects the application to the Wifi p2p framework.
        mChannel = mManager.initialize(this, getMainLooper(), null);


        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        /*mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Persistent groups removed");

            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG, "Failed to remove groups");


            }
        });
*/
        System.out.println();
    }
    // Manages the peer list
    // _________________________________________________________________________________________________________________________


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            //If device list has changed
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                // Updates the peer list

                // Makes arrays of both user's names and device names
                deviceNameArray = new ArrayList<String>();
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index = 0;

                // Each device's ID and its name are appended to separate lists.
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray.add(device.deviceName);
                    /*for (int i = 0; i < 6; i++) {
                        deviceNameArray.add(String.format("%d Test Device", i));
                    }*/
                    deviceArray[index] = device;
                    index++;
                }

                //CardAdapter adapter = new CardAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                //listView.setAdapter(adapter);


            }
            adapter = new CardAdapter(proto_proximity_chat.this, deviceNameArray);
            adapter.setClickListener(proto_proximity_chat.this);
            recyclerView.setAdapter(adapter);

        }
    };


    // Handles the item click for each item in the RecyclerView
    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();

        final WifiP2pDevice device = deviceArray[position];
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;


        mManager.requestConnectionInfo(mChannel, connectionInfoListener);
        // connect creates a new group
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(proto_proximity_chat.this, "Connection SUCCESSFUL",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(proto_proximity_chat.this, "Connection FAILED",
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    // NOTE: For some reason, connectioninfolistener is not responding to the mManager connect call on line 255.
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            // If the chat has already been started, the groups are cleared. This is to prevent
            // the ConnectionInfoListener from restarting the chat.
            if (info.groupFormed && info.isGroupOwner) {

                Intent myIntent = new Intent(proto_proximity_chat.this, ChatWindowActivity.class);
                myIntent.putExtra("role", "Host"); //Optional parameters
                proto_proximity_chat.this.startActivityForResult(myIntent, 1);


                // if a group has been formed and I am the group owner
                // The chat window intent is started for a client
            } else if (info.groupFormed && !info.isGroupOwner) {
                Intent myIntent = new Intent(proto_proximity_chat.this, ChatWindowActivity.class);
                myIntent.putExtra("role", "Client"); //Optional parameters
                myIntent.putExtra("info", groupOwnerAddress);

                proto_proximity_chat.this.startActivityForResult(myIntent, 2);

                // if the group has already been formed, but I am not the owner
                // A clientClass is created

                //clientClass = new ClientClass(groupOwnerAddress);
                //clientClass.start();

            }
        }
    };
            /*mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "Persistent groups removed");

                }

                @Override
                public void onFailure(int reason) {
                    Log.i(TAG, "Failed to remove groups");


                }
            });
        }

    };*/

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.i(TAG, "Persistent groups removed");

            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG, "Failed to remove groups");


            }
        });

    }
}

