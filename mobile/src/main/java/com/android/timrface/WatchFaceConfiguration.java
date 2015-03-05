package com.android.timrface;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.support.wearable.companion.WatchFaceCompanion;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class WatchFaceConfiguration extends ActionBarActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {

    // TODO: use the shared constants (needs covering all the samples with Gradle build model)
    private static final String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    private static final String KEY_MAIN_COLOR = "MAIN_COLOR";
    private static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";

    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    private NodeApi.NodeListener nodeListener;
    private MessageApi.MessageListener messageListener;
    private String remoteNodeId;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_face_config);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.settings);


        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        nodeListener = new NodeApi.NodeListener() {
            @Override
            public void onPeerConnected(Node node) {
                remoteNodeId = node.getId();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        displayNoConnectedDeviceDialog();
                    }
                });
            }

            @Override
            public void onPeerDisconnected(Node node) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        displayNoConnectedDeviceDialog();
                    }
                });
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {

        if (mPeerId != null) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(PATH_WITH_FEATURE).authority(mPeerId).build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
        } else {
            displayNoConnectedDeviceDialog();
        }
    }

    @Override // ResultCallback<DataApi.DataItemResult>
    public void onResult(DataApi.DataItemResult dataItemResult) {
            DataItem configDataItem = dataItemResult.getDataItem();
            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
            DataMap config = dataMapItem.getDataMap();
            setUpAllPickers();
    }

    @Override // GoogleApiClient.ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
    }

    @Override // GoogleApiClient.OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
    }

    private void displayNoConnectedDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String messageText = getResources().getString(R.string.title_no_device_connected);
        String okText = getResources().getString(R.string.ok_no_device_connected);
        builder.setMessage(messageText)
                .setCancelable(false)
                .setPositiveButton(okText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setUpAllPickers() {
        String[] array = getResources().getStringArray(R.array.colors);
        setUpColorListener(R.id.white, "M", array[0]);
        setUpColorListener(R.id.dark, "M", array[1]);
        setUpColorListener(R.id.black, "M", array[2]);

        setUpColorListener(R.id.orange, "B", array[3]);
        setUpColorListener(R.id.pink, "B", array[4]);
        setUpColorListener(R.id.purple, "B", array[5]);
        setUpColorListener(R.id.deep_blue, "B", array[6]);
        setUpColorListener(R.id.blue, "B", array[7]);
        setUpColorListener(R.id.light_blue, "B", array[8]);
        setUpColorListener(R.id.teal, "B", array[9]);
        setUpColorListener(R.id.green, "B", array[10]);
        setUpColorListener(R.id.deep_orange, "B", array[11]);
        setUpColorListener(R.id.red, "B", array[12]);
        setUpColorListener(R.id.amber, "B", array[13]);

    }


    private void setUpColorListener(int spinnerId, final String configKey, final String color) {
        ImageView img = (ImageView) findViewById(spinnerId);
        ColorDrawable drawable = (ColorDrawable) img.getBackground();
        img.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {

                sendConfigUpdateMessage(configKey, color);
            }
        });
    }

    private void sendConfigUpdateMessage(String configKey, String color) {
        System.out.println("Sending...");
        if (mPeerId != null) {
            DataMap config = new DataMap();
            config.putString(configKey, color);
            byte[] rawData = config.toByteArray();
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, color, rawData);
            System.out.println("Sent color: "+ color);
        }
    }

    private void sendMessage(final String key, final String color) {
        {

            if (remoteNodeId != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mGoogleApiClient.blockingConnect(30, TimeUnit.MILLISECONDS);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, remoteNodeId, color, null);
                        mGoogleApiClient.disconnect();

                        System.out.println("Sent color: " + color);
                    }
                }).start();
            }
        }
    }}