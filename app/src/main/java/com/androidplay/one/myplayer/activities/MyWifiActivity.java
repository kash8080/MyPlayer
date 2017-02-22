package com.androidplay.one.myplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidplay.one.myplayer.ApplicationController;
import com.androidplay.one.myplayer.R;
import com.androidplay.one.myplayer.WiFiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class MyWifiActivity extends AppCompatActivity implements ChannelListener,PeerListListener {

    ApplicationController con;
    Toolbar toolbar;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    private boolean retryChannel = false;
    private boolean wifi_isEnabled=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        con=new ApplicationController(this.getApplicationContext(),this);
        setContentView(R.layout.activity_my_wifi);
        toolbar=(Toolbar)findViewById(R.id.MyToolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(new ApplicationController().getPrimary());

        mManager = (WifiP2pManager) getSystemService(this.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    @Override
    protected void onResume() {
        super.onResume();
        con.activityOnResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void chooseDeviceFromList(WifiP2pDeviceList wifiP2pDeviceList){
        Log.i("wifii","choose device from list method activity");

        //received list from peerlistener
        List<WifiP2pDevice> refreshedPeers=new ArrayList<>();
        for(WifiP2pDevice dv:wifiP2pDeviceList.getDeviceList()){
            refreshedPeers.add(dv);
        }
         if (!refreshedPeers.equals(peers)) {
             peers.clear();
             peers.addAll(refreshedPeers);

             // If an AdapterView is backed by this data, notify it
             // of the change.  For instance, if you have a ListView of
             // available peers, trigger an update.
             //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
             Log.d("wifii", "all peer refreshed list--------");

             for(WifiP2pDevice devicee:peers){
                 Log.d("wifii", devicee.toString());

             }
             // Perform any other updates needed based on the new list of
             // peers connected to the Wi-Fi P2P network.
             }

         if (peers.size() == 0) {
            Log.d("wifii", "No devices found");
             return;
             }

        //obtain a peer from the WifiP2pDeviceList
        /*
        WifiP2pDevice device;
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new ActionListener() {

             @Override
             public void onSuccess() {
                 Log.i("wifii","connect success");

                 //success logic
                 }

             @Override
             public void onFailure(int reason) {
                 Log.i("wifii","connect fail");
                 //failure logic
                 }
        });
        */
    }

    public void setWifi_isEnabled(boolean wifi_isEnabled) {
        Log.i("wifii","wifi_isEnabled = "+wifi_isEnabled);

        this.wifi_isEnabled = wifi_isEnabled;
        if (wifi_isEnabled){
            resetData();
        }
    }


    public void resetData() {
        Log.d("wifii", "resetData");

/**

 * Remove all peers and clear all fields. This is called on

 * BroadcastReceiver receiving a state change event.

 */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.atn_direct_enable:

                if (mManager != null && mChannel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e("wifii", "channel or manager is null");
                }
                return true;

            case R.id.atn_direct_discover:
                if (!wifi_isEnabled) {
                    Toast.makeText(MyWifiActivity.this, "warning ..wifi is not enabled",Toast.LENGTH_SHORT).show();
                    return true;
                }
                /*final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.onInitiateDiscovery();
*/
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MyWifiActivity.this, "Discovery Initiated",Toast.LENGTH_SHORT).show();
                        // Code for when the discovery initiation is successful goes here.
                        // No services have actually been discovered yet, so this method
                        // can often be left blank.  Code for peer discovery goes in the
                        // onReceive method, detailed below.
                        Log.i("wifii","discover success");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MyWifiActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
                        // Code for when the discovery initiation fails goes here.
                        // Alert the user that something went wrong.
                        Log.i("wifii","discover fail");
                    }
                });

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }
    }


    public void showDetails(WifiP2pDevice device) {
        Log.d("wifii", "showDetails");

/*
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()

                .findFragmentById(R.id.frag_detail);

        fragment.showDetails(device);
*/


    }
    public void connect(WifiP2pConfig config) {

        mManager.connect(mChannel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("wifii", "connect onSuccess");
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }
            @Override
            public void onFailure(int reason) {
                Log.d("wifii", "connect onFailure");
                Toast.makeText(MyWifiActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void disconnect() {
        /*
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()

                .findFragmentById(R.id.frag_detail);

        fragment.resetViews();
*/
        mManager.removeGroup(mChannel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d("wifii", "removeGroup onFailure");

                Log.d("wifii", "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                Log.d("wifii", "removeGroup success");
                //fragment.getView().setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onChannelDisconnected() {

        // we will try once more
        if (mManager != null && !retryChannel) {
            Log.d("wifii", "onChannelDisconnected  Trying again");
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);
        } else {
            Log.d("wifii", "onChannelDisconnected  Channel is probably lost premanently");

            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }



    public void cancelDisconnect() {

        Log.d("wifii", "cancelDisconnect");


        /*

         * A cancel abort request by user. Disconnect i.e. removeGroup if

         * already connected. Else, request WifiP2pManager to abort the ongoing

         * request

         */
/*
        if (mManager != null) {

            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()

                    .findFragmentById(R.id.frag_list);

            if (fragment.getDevice() == null

                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {

                disconnect();

            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE

                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {



                mManager.cancelConnect(mChannel, new ActionListener() {



                    @Override

                    public void onSuccess() {

                        Toast.makeText(MyWifiActivity.this, "Aborting connection",

                                Toast.LENGTH_SHORT).show();

                    }



                    @Override

                    public void onFailure(int reasonCode) {

                        Toast.makeText(MyWifiActivity.this,

                                "Connect abort request failed. Reason Code: " + reasonCode,

                                Toast.LENGTH_SHORT).show();

                    }

                });

            }
*/
        }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Log.i("wifii","onPeersAvailable");
        chooseDeviceFromList(wifiP2pDeviceList);
    }
    private static String getDeviceStatus(int deviceStatus) {

        Log.d("wifii", "Peer status :" + deviceStatus);

        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }
}
