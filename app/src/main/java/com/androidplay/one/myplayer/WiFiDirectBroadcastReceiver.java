package com.androidplay.one.myplayer;

/**
 * Created by Rahul on 09-02-2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.androidplay.one.myplayer.activities.MyWifiActivity;

/**
  * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
  */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

     private WifiP2pManager mManager;
     private WifiP2pManager.Channel mChannel;
     private MyWifiActivity mActivity;
     PeerListListener myPeerListListener;

     public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MyWifiActivity activity) {
         super();
         this.mManager = manager;
         this.mChannel = channel;
         this.mActivity = activity;

     }

     @Override
     public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();

         if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
             // Check to see if Wi-Fi is enabled and notify appropriate activity
             int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
              if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                 // Wifi P2P is enabled
                  mActivity.setWifi_isEnabled(true);
                  } else {
                  // Wi-Fi P2P is not enabled
                  mActivity.setWifi_isEnabled(false);
              }

         } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
             // Call WifiP2pManager.requestPeers() to get a list of current peers
             // request available peers from the wifi p2p manager. This is an
             // asynchronous call and the calling activity is notified with a
             // callback on PeerListListener.onPeersAvailable()
             if (mManager != null) {
                  mManager.requestPeers(mChannel,mActivity);
             }

         } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
             // Respond to new connection or disconnections
             Log.i("wifii","WIFI_P2P_CONNECTION_CHANGED_ACTION  ");

         } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
             Log.i("wifii","WIFI_P2P_THIS_DEVICE_CHANGED_ACTION  ");
            // Respond to this device's wifi state changing
         }

     }

}