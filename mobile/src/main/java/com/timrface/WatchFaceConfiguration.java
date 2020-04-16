package com.timrface;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.timrface.helper.CanvasView;
import com.timrface.helper.SharedPreferences;
import com.timrface.util.IabHelper;
import com.timrface.util.IabResult;
import com.timrface.util.Inventory;
import com.timrface.util.Purchase;
import com.timrface.watchfacelayout.Configuration;

import java.util.ArrayList;

public class WatchFaceConfiguration extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    static ArrayList<String> list = new ArrayList<>();
    private static String ITEM_SKU = "com.timrface.donate";
    final String TAG = "WatchFaceConfiguration";
    String[] colors;
    int oldCheckedId = -1;
    Drawable oldCheckedDrawable;
    int oldCheckedBackgroundId = -1;
    Drawable oldCheckedBackgroundDrawable;
    CanvasView canvasView;
    ColorPicker colorPicker;
    Handler mUpdateTimeHandler;
    private Configuration configuration = new Configuration();
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        Log.d("Billing", "Billing succed! Icon can be bought again");
                    } else {
                        Log.d("Billing", "Billing failed");
                    }
                }
            };
    private IabHelper mHelper;
    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {


            if (result.isFailure()) {
                Log.d("Billing", "Billing failed");
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase) {
            if (result.isFailure()) {
                Log.d("Billing", "Billing failed");
                return;
            } else if (purchase.getSku().equals(ITEM_SKU)) {
                Log.d("Billing", "Bought item");
                consume();
            }
            SharedPreferences.saveBoolean("donation", true, getApplicationContext());

        }
    };
    private GoogleApiClient googleApiClient;
    private String base64EncodedPublicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_face_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.settings);

        canvasView = (CanvasView) findViewById(R.id.canvas_layout);
        mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 0:
                        canvasView.invalidate();
                        long timeMs = System.currentTimeMillis();
                        long delayMs =
                                canvasView.INTERACTIVE_UPDATE_RATE_MS - (timeMs % canvasView.INTERACTIVE_UPDATE_RATE_MS);
                        mUpdateTimeHandler.sendEmptyMessageDelayed(0, delayMs);
                        break;
                }
            }
        };
        updateTimer();

        colorPicker = new ColorPicker(WatchFaceConfiguration.this);

        colors = getResources().getStringArray(R.array.colors);
        for (int i = 0; i < colors.length; i++) {
            list.add(i, colors[i]);
        }

        if (!SharedPreferences.getBoolean("donation", false, getApplicationContext())) {
            dialog();
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        configuration.setSmoothScrolling(SharedPreferences.getBoolean("button", true, getApplicationContext()));
        String backgroundColor = SharedPreferences.getString("background_color", "#FAFAFA", getApplicationContext());
        boolean isBackgroundColorWhite = !backgroundColor.equals("#FAFAFA");
        configuration.setBackgroundColor(backgroundColor);
        configuration.setTextColor(isBackgroundColorWhite ? Color.parseColor("#424242") : Color.parseColor("#FAFAFA"));
        configuration.setArrowResourceId(getArrowDrawableResourceIdByBackgroundColor(backgroundColor));
        configuration.setInteractiveColor(SharedPreferences.getString("color", "#FF9800", getApplicationContext()));
        configuration.setShowBatteryLevel(SharedPreferences.getBoolean("battery", true, getApplicationContext()));
        configuration.setShowZeroDigit(SharedPreferences.getBoolean("zero_digit", true, getApplicationContext()));
        canvasView.updateConfig(configuration);

        setUpAllColors();

        CheckBox seconds = (CheckBox) findViewById(R.id.seconds);
        seconds.setChecked(SharedPreferences.getBoolean("button", true, getApplicationContext()));
        seconds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");
                putDataMapRequest.getDataMap().putBoolean("SMOOTH_SECONDS", isChecked);
                PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                SharedPreferences.saveBoolean("button", isChecked, getApplicationContext());
                configuration.setSmoothScrolling(isChecked);
                canvasView.updateConfig(configuration);
            }
                                           }
        );

        CheckBox battery = (CheckBox) findViewById(R.id.battery);
        battery.setChecked(SharedPreferences.getBoolean("battery", true, getApplicationContext()));
        battery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");
                putDataMapRequest.getDataMap().putBoolean("BATTERY_INDICATOR", isChecked);
                PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                SharedPreferences.saveBoolean("battery", isChecked, getApplicationContext());
                configuration.setShowBatteryLevel(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        CheckBox zeroDigit = (CheckBox) findViewById(R.id.zero_digit);
        zeroDigit.setChecked(SharedPreferences.getBoolean("zero_digit", true, getApplicationContext()));
        zeroDigit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");
                putDataMapRequest.getDataMap().putBoolean("ZERO_DIGIT", isChecked);
                PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                SharedPreferences.saveBoolean("zero_digit", isChecked, getApplicationContext());
                configuration.setShowZeroDigit(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        base64EncodedPublicKey = getResources().getString(R.string.key);
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("Billing", "Problem setting up In-app Billing: " + result);
                }
            }
        });

    }

    private void updateTimer() {
        mUpdateTimeHandler.removeMessages(0);
        mUpdateTimeHandler.sendEmptyMessage(0);
    }

    private int getArrowDrawableResourceIdByBackgroundColor(String color) {
        switch (color) {
            case "#424242":
                return R.drawable.indicator_grey;
            case "#000000":
                return R.drawable.indicator_black;
            default:
                return R.drawable.indicator;
        }
    }

    private void dialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(WatchFaceConfiguration.this);

        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);

        builder.setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                buy();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuItem menuItem = menu.add(0, 0, 0, "Donate").setIcon(
                R.drawable.coin);
        MenuItemCompat.setShowAsAction(menuItem,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case 0:
                buy();
                break;
        }
        return true;
    }

    private void buy() {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "token");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void consume() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    private void setUpAllColors() {
        setUpColorListener(R.id.white, 0, colors[0], R.drawable.white);
        setUpColorListener(R.id.dark, 1, colors[1], R.drawable.grey);
        setUpColorListener(R.id.black, 2, colors[2], R.drawable.black);

        setUpColorListener(R.id.orange, 3, colors[3], R.drawable.orange);
        setUpColorListener(R.id.pink, 4, colors[4], R.drawable.pink);
        setUpColorListener(R.id.purple, 5, colors[5], R.drawable.purple);
        setUpColorListener(R.id.deep_blue, 6, colors[6], R.drawable.deep_blue);
        setUpColorListener(R.id.blue, 7, colors[7], R.drawable.blue);
        setUpColorListener(R.id.light_blue, 8, colors[8], R.drawable.light_blue);
        setUpColorListener(R.id.teal, 9, colors[9], R.drawable.teal);
        setUpColorListener(R.id.green, 10, colors[10], R.drawable.green);
        setUpColorListener(R.id.deep_orange, 11, colors[11], R.drawable.deep_orange);
        setUpColorListener(R.id.red, 12, colors[12], R.drawable.red);
        setUpColorListener(R.id.amber, 13, colors[13], R.drawable.amber);
        setUpColorListener(R.id.wheel, 14, colors[14], R.drawable.wheel);
    }


    private void setUpColorListener(final int id, final int key, final String color, int original) {
        final Button imgButton = (Button) findViewById(id);
        final Drawable drawable = getResources().getDrawable(original);
        imgButton.setBackground(drawable);

        if (key == SharedPreferences.getInteger("id_background", -1, getApplicationContext())) {
            Drawable[] layers = new Drawable[2];
            layers[0] = drawable;
            layers[1] = getResources().getDrawable(R.drawable.ic_check);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            imgButton.setBackground(layerDrawable);
            oldCheckedBackgroundId = id;
            oldCheckedBackgroundDrawable = layers[0];

        }

        if (key == SharedPreferences.getInteger("id", -1, getApplicationContext())) {
            Drawable[] layers = new Drawable[2];
            layers[0] = drawable;
            layers[1] = getResources().getDrawable(R.drawable.ic_check);
            LayerDrawable layerDrawable = new LayerDrawable(layers);
            imgButton.setBackground(layerDrawable);
            oldCheckedId = id;
            oldCheckedDrawable = layers[0];
        }

        imgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (key < 3) {
                    Log.d(TAG, "Color: " + color);
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");
                    putDataMapRequest.getDataMap().putString("BACKGROUND_COLOR", color);
                    PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
                    Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                    configuration.setBackgroundColor(color);
                    boolean isBackgroundColorWhite = Color.parseColor("#FAFAFA") == Color.parseColor(color);
                    String textColor = isBackgroundColorWhite ? "#424242" : "#FAFAFA";
                    configuration.setTextColor(textColor);
                    configuration.setArrowResourceId(getArrowDrawableResourceIdByBackgroundColor(color));
                    canvasView.updateConfig(configuration);
                    SharedPreferences.saveInteger("id_background", key, getApplicationContext());
                    SharedPreferences.saveString("background_color", color, getApplicationContext());
                } else if (key == 14) {
                    colorPicker.show();
                    Button okColor = (Button) colorPicker.findViewById(R.id.okColorButton);
                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");
                            putDataMapRequest.getDataMap().putInt("COLOR_MANUAL", colorPicker.getColor());
                            PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
                            Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                            colorPicker.dismiss();
                            configuration.setInteractiveColor(colorPicker.getColor());
                            canvasView.updateConfig(configuration);
                            SharedPreferences.saveInteger("id", key, getApplicationContext());
                            SharedPreferences.saveString("color", color, getApplicationContext());
                        }
                    });
                } else {
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");
                    putDataMapRequest.getDataMap().putString("COLOR", color);
                    PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
                    Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                    configuration.setInteractiveColor(color);
                    canvasView.updateConfig(configuration);
                    SharedPreferences.saveInteger("id", key, getApplicationContext());
                    SharedPreferences.saveString("color", color, getApplicationContext());
                }

                if (key < 3) {
                    if (oldCheckedBackgroundId != -1) {
                        Button button = (Button) findViewById(oldCheckedBackgroundId);
                        button.setBackground(oldCheckedBackgroundDrawable);
                    }
                }
                else {
                    if (oldCheckedId != -1) {
                        Button button = (Button) findViewById(oldCheckedId);
                        button.setBackground(oldCheckedDrawable);
                    }
                }

                Drawable[] layers = new Drawable[2];
                layers[0] = drawable;
                layers[1] = getResources().getDrawable(R.drawable.ic_check);
                LayerDrawable layerDrawable = new LayerDrawable(layers);
                imgButton.setBackground(layerDrawable);
                if (key < 3) {
                    oldCheckedBackgroundId = id;
                    oldCheckedBackgroundDrawable = layers[0];
                }
                else {
                    oldCheckedId = id;
                    oldCheckedDrawable = layers[0];
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/watch_face_config");

        putDataMapRequest.getDataMap().putBoolean("SMOOTH_SECONDS", SharedPreferences.getBoolean("button", true, getApplicationContext()));
        putDataMapRequest.getDataMap().putString("BACKGROUND_COLOR", SharedPreferences.getString("background_color", "#FF9800", getApplicationContext()));
        putDataMapRequest.getDataMap().putString("COLOR", SharedPreferences.getString("color", "#FAFAFA", getApplicationContext()));
        putDataMapRequest.getDataMap().putBoolean("BATTERY_INDICATOR", SharedPreferences.getBoolean("battery", true, getApplicationContext()));
        putDataMapRequest.getDataMap().putBoolean("ZERO_DIGIT", SharedPreferences.getBoolean("zero_digit", true, getApplicationContext()));

        PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }
}