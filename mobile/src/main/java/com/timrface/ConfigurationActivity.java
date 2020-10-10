package com.timrface;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.NodeClient;
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
import com.timrface.watchfacelayout.config.ConfigUpdateFinished;
import com.timrface.watchfacelayout.config.ConfigUpdater;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.config.ConfigurationBuilder;
import com.timrface.watchfacelayout.config.ConfigurationConstant;
import com.timrface.watchfacelayout.config.StoredConfigurationFetcher;

import java.util.ArrayList;
import java.util.Calendar;

public class ConfigurationActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {

    static ArrayList<String> list = new ArrayList<>();
    private static String ITEM_SKU = "com.timrface.donate";
    String[] colors;
    ColorPicker colorPicker;
    Handler mUpdateTimeHandler;
    private Configuration configuration;

    CanvasView canvasView;
    CheckBox smoothSecondsCheckBox;
    CheckBox showBatteryLevelCheckBox;
    CheckBox showZeroDigitCheckBox;
    CheckBox showUnreadNotificationsCounter;
    CheckBox useStrokeDigitsInAmbientMode;

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

    private void updateTimer() {
        mUpdateTimeHandler.removeMessages(0);
        mUpdateTimeHandler.sendEmptyMessage(0);
    }

    private IabHelper mHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        final MenuItem menuItem = menu.add(0, 0, 0, "Donate").setIcon(
                R.drawable.coin);
        MenuItemCompat.setShowAsAction(menuItem,
                MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
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
        setUpColorListener(R.id.automatic, 3, colors[3], R.drawable.automatic);

        setUpColorListener(R.id.orange, 4, colors[4], R.drawable.orange);
        setUpColorListener(R.id.pink, 5, colors[5], R.drawable.pink);
        setUpColorListener(R.id.purple, 6, colors[6], R.drawable.purple);
        setUpColorListener(R.id.deep_blue, 7, colors[7], R.drawable.deep_blue);
        setUpColorListener(R.id.blue, 8, colors[8], R.drawable.blue);
        setUpColorListener(R.id.light_blue, 9, colors[9], R.drawable.light_blue);
        setUpColorListener(R.id.teal, 10, colors[10], R.drawable.teal);
        setUpColorListener(R.id.green, 11, colors[11], R.drawable.green);
        setUpColorListener(R.id.deep_orange, 12, colors[12], R.drawable.deep_orange);
        setUpColorListener(R.id.red, 13, colors[13], R.drawable.red);
        setUpColorListener(R.id.amber, 14, colors[14], R.drawable.amber);
        setUpColorListener(R.id.wheel, 15, colors[15], R.drawable.wheel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_face_config);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.settings);

        canvasView = (CanvasView) findViewById(R.id.canvas_layout);
        smoothSecondsCheckBox = findViewById(R.id.seconds);
        showBatteryLevelCheckBox = findViewById(R.id.battery);
        showZeroDigitCheckBox = findViewById(R.id.zero_digit);
        showUnreadNotificationsCounter = findViewById(R.id.unreadNotifications);
        useStrokeDigitsInAmbientMode = findViewById(R.id.strokeDigitsInAmbientMode);

        configuration = ConfigurationBuilder.getDefaultConfiguration(this);
        Wearable.getDataClient(this).addListener(this);

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

        colorPicker = new ColorPicker(ConfigurationActivity.this);

        colors = getResources().getStringArray(R.array.colors);
        for (int i = 0; i < colors.length; i++) {
            list.add(i, colors[i]);
        }

        NodeClient nodeClient = Wearable.getNodeClient(this);
        DataClient dataClient = Wearable.getDataClient(this);
        new StoredConfigurationFetcher().updateConfig(nodeClient, dataClient, configuration, new ConfigUpdateFinished() {
            @Override
            public void onUpdateFinished(Configuration configuration) {
                canvasView.updateConfig(configuration);
                smoothSecondsCheckBox.setChecked(configuration.isSmoothScrolling());
                showBatteryLevelCheckBox.setChecked(configuration.isShowBatteryLevel());
                showZeroDigitCheckBox.setChecked(configuration.isShowZeroDigit());
                showUnreadNotificationsCounter.setChecked(configuration.isShowUnreadNotificationsCounter());
                useStrokeDigitsInAmbientMode.setChecked(configuration.isUseStrokeDigitsInAmbientMode());
            }
        });

        setUpAllColors();

        smoothSecondsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                             @Override
                                                             public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                                 sendDataItem(ConfigurationConstant.SMOOTH_SECONDS, isChecked);
                                                                 configuration.setSmoothScrolling(isChecked);
                                                                 canvasView.updateConfig(configuration);
                                                             }
                                                         }
        );

        showBatteryLevelCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendDataItem(ConfigurationConstant.BATTERY_INDICATOR, isChecked);
                configuration.setShowBatteryLevel(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        showZeroDigitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendDataItem(ConfigurationConstant.ZERO_DIGIT, isChecked);
                configuration.setShowZeroDigit(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        showUnreadNotificationsCounter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendDataItem(ConfigurationConstant.UNREAD_NOTIFICATIONS, isChecked);
                configuration.setShowUnreadNotificationsCounter(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        useStrokeDigitsInAmbientMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendDataItem(ConfigurationConstant.STROKE_DIGITS, isChecked);
                configuration.setUseStrokeDigitsInAmbientMode(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        String base64EncodedPublicKey = getResources().getString(R.string.key);
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("Billing", "Problem setting up In-app Billing: " + result);
                }
            }
        });

        BroadcastReceiver tickReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    if (configuration.isAutomaticLightDarkMode()) {
                        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        if (hour > 19) {
                            configuration.setBackgroundColor("#000000");
                            configuration.setTextColor("#FAFAFA");
                            canvasView.updateConfig(configuration);
                        } else if (hour < 6) {
                            configuration.setBackgroundColor("#FAFAFA");
                            configuration.setTextColor("#424242");
                            canvasView.updateConfig(configuration);
                        }
                    }
                }
            }
        };
        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void showDonationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConfigurationActivity.this);

        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);

        builder.setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                launchPurchaseFlow();
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            showDonationDialog();
        }
        return true;
    }

    private void launchPurchaseFlow() {
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "token");
    }

    private void setUpColorListener(final int id, final int key, final String color, int original) {
        final Button imgButton = (Button) findViewById(id);
        final Drawable drawable = getResources().getDrawable(original);
        imgButton.setBackground(drawable);

        imgButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (key < 3) {
                    sendDataItem(ConfigurationConstant.BACKGROUND_COLOR, color);
                    sendDataItem(ConfigurationConstant.AUTOMATIC_DARK_LIGHT, false);
                    configuration.setBackgroundColor(color);
                    boolean isBackgroundColorWhite = Color.parseColor("#FAFAFA") == Color.parseColor(color);
                    String textColor = isBackgroundColorWhite ? "#424242" : "#FAFAFA";
                    configuration.setTextColor(textColor);
                    configuration.setAutomaticDarkLightMode(false);
                    canvasView.updateConfig(configuration);
                } else if (key == 3) {
                    sendDataItem(ConfigurationConstant.AUTOMATIC_DARK_LIGHT, true);
                    configuration.setAutomaticDarkLightMode(true);
                    canvasView.updateConfig(configuration);
                } else if (key == 14) {
                    colorPicker.show();
                    Button okColor = (Button) colorPicker.findViewById(R.id.okColorButton);
                    okColor.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String hexColor = String.format("#%06X", (0xFFFFFF & colorPicker.getColor()));
                            sendDataItem(ConfigurationConstant.INTERACTIVE_COLOR, hexColor);
                            colorPicker.dismiss();
                            configuration.setInteractiveColor(colorPicker.getColor());
                            canvasView.updateConfig(configuration);
                        }
                    });
                } else {
                    sendDataItem(ConfigurationConstant.INTERACTIVE_COLOR, color);
                    configuration.setInteractiveColor(color);
                    canvasView.updateConfig(configuration);
                }
            }
        });
    }

    private void sendDataItem(ConfigurationConstant configurationConstant, String value) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(ConfigurationConstant.CONFIG_PATH.toString() + configurationConstant.toString());
        putDataMapRequest.getDataMap().putString(configurationConstant.toString(), value);
        PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest().setUrgent();
        Wearable.getDataClient(ConfigurationActivity.this).putDataItem(putDataReq);

        if (configurationConstant == ConfigurationConstant.INTERACTIVE_COLOR) {
            StoredConfigurationFetcher.deleteInteractiveColorSetByOtherDevice(Wearable.getNodeClient(this), Wearable.getDataClient(this));
        }
    }

    private void sendDataItem(ConfigurationConstant configurationConstant, boolean value) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(ConfigurationConstant.CONFIG_PATH.toString() + configurationConstant.toString());
        putDataMapRequest.getDataMap().putBoolean(configurationConstant.toString(), value);
        PutDataRequest putDataReq = putDataMapRequest.asPutDataRequest();
        Wearable.getDataClient(ConfigurationActivity.this).putDataItem(putDataReq);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                ConfigUpdater.updateConfig(configuration, item);
            }
        }
        canvasView.updateConfig(configuration);
        dataEventBuffer.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.getDataClient(this).removeListener(this);
        mUpdateTimeHandler.removeMessages(0);
    }
}