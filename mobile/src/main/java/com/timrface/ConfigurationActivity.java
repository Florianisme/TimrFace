package com.timrface;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.NodeClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.timrface.helper.CanvasView;
import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.ConfigUpdateFinished;
import com.timrface.watchfacelayout.config.ConfigUpdater;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.config.ConfigurationBuilder;
import com.timrface.watchfacelayout.config.ConfigurationConstant;
import com.timrface.watchfacelayout.config.StoredConfigurationFetcher;
import com.timrface.watchfacelayout.util.DayNightBroadcastReceiver;
import com.timrface.watchfacelayout.util.FilteredBroadcastReceiver;
import com.timrface.watchfacelayout.util.TimeZoneBroadcastReceiver;

import java.util.ArrayList;
import java.util.TimeZone;

public class ConfigurationActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {

    static ArrayList<String> list = new ArrayList<>();
    String[] colors;
    ColorPicker colorPicker;
    Handler mUpdateTimeHandler;
    private Configuration configuration;

    private FilteredBroadcastReceiver dayNightBroadcastReceiver;
    private FilteredBroadcastReceiver timeFormatChangedReceiver;

    CanvasView canvasView;
    SwitchCompat smoothSecondsCheckBox;
    AppCompatSpinner leftComplicationSpinner;
    AppCompatSpinner middleComplicationSpinner;
    SwitchCompat showZeroDigitCheckBox;
    SwitchCompat useStrokeDigitsInAmbientMode;

    private void updateTimer() {
        mUpdateTimeHandler.removeMessages(0);
        mUpdateTimeHandler.sendEmptyMessage(0);
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

        canvasView = (CanvasView) findViewById(R.id.canvas_layout);
        smoothSecondsCheckBox = findViewById(R.id.seconds);
        leftComplicationSpinner = findViewById(R.id.spinner_left_complication);
        middleComplicationSpinner = findViewById(R.id.spinner_middle_complication);
        showZeroDigitCheckBox = findViewById(R.id.zero_digit);
        useStrokeDigitsInAmbientMode = findViewById(R.id.strokeDigitsInAmbientMode);

        ArrayAdapter<String> leftComplicationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.complications));
        ArrayAdapter<String> middleComplicationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.complications));
        leftComplicationSpinner.setAdapter(leftComplicationAdapter);
        middleComplicationSpinner.setAdapter(middleComplicationAdapter);

        configuration = ConfigurationBuilder.getDefaultConfiguration(this);
        Wearable.getDataClient(this).addListener(this);

        new WearAppInstalledTester(this);

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
                leftComplicationSpinner.setSelection(configuration.getLeftComplicationType().getId());
                middleComplicationSpinner.setSelection(configuration.getMiddleComplicationType().getId());
                showZeroDigitCheckBox.setChecked(configuration.isShowZeroDigit());
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

        leftComplicationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendDataItem(ConfigurationConstant.LEFT_COMPLICATION_ID, position);
                configuration.setLeftComplicationType(ComplicationType.getComplicationForId(position));
                canvasView.updateConfig(configuration);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        middleComplicationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sendDataItem(ConfigurationConstant.MIDDLE_COMPLICATION_ID, position);
                configuration.setMiddleComplicationType(ComplicationType.getComplicationForId(position));
                canvasView.updateConfig(configuration);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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


        useStrokeDigitsInAmbientMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendDataItem(ConfigurationConstant.STROKE_DIGITS, isChecked);
                configuration.setUseStrokeDigitsInAmbientMode(isChecked);
                canvasView.updateConfig(configuration);
            }
        });

        dayNightBroadcastReceiver = new DayNightBroadcastReceiver(configuration, canvasView::updateConfig);
        timeFormatChangedReceiver = new TimeZoneBroadcastReceiver(configuration, this::updateTimezone);
        dayNightBroadcastReceiver.register(this);
        timeFormatChangedReceiver.register(this);
    }

    private void updateTimezone(Configuration configuration) {
        canvasView.updateTimezone(TimeZone.getDefault());
        canvasView.updateConfig(configuration);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.open_on_watch) {
            RemoteIntent.startRemoteActivity(this, new Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(Uri.parse("market://details?id=com.timrface")), null);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.configuration_activity, menu);
        return true;
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
                    dayNightBroadcastReceiver.updateInternalConfigurationState(configuration);
                } else if (key == 15) {
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

    private void sendDataItem(ConfigurationConstant configurationConstant, int value) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(ConfigurationConstant.CONFIG_PATH.toString() + configurationConstant.toString());
        putDataMapRequest.getDataMap().putInt(configurationConstant.toString(), value);
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
        dayNightBroadcastReceiver.unregister(this);
        timeFormatChangedReceiver.unregister(this);
    }
}