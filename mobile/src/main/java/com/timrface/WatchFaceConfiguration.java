package com.timrface;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.timrface.helper.SharedPreferences;
import com.timrface.helper.TeleportClient;
import com.timrface.util.IabHelper;
import com.timrface.util.IabResult;
import com.timrface.util.Inventory;
import com.timrface.util.Purchase;

import java.util.ArrayList;

public class WatchFaceConfiguration extends ActionBarActivity {

    private IabHelper mHelper;
    TeleportClient mTeleportClient;

    static ArrayList<String> list = new ArrayList<>();
    String[] colors;

    int oldCheckedId = -1;
    Drawable oldCheckedDrawable;
    int oldCheckedBackgroundId = -1;
    Drawable oldCheckedBackgroundDrawable;

    private String base64EncodedPublicKey;
    private static String ITEM_SKU = "com.timrface.donate";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_face_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.settings);


        colors = getResources().getStringArray(R.array.colors);
        for (int i = 0; i < colors.length; i++) {
            list.add(i, colors[i]);
        }

        if (!SharedPreferences.getBoolean("donation", false, getApplicationContext())) {
            dialog();
        }

        mTeleportClient = new TeleportClient(this);
        mTeleportClient.connect();
        mTeleportClient.setOnGetMessageTask(new MessageTask());
        String test = "bla";
        mTeleportClient.sendMessage("seconds"+String.valueOf(SharedPreferences.getBoolean("button", true, getApplicationContext())), test.getBytes());
        mTeleportClient.sendMessage(SharedPreferences.getString("background_color", "#FF9800", getApplicationContext()), test.getBytes());
        mTeleportClient.sendMessage(SharedPreferences.getString("color", "#FAFAFA", getApplicationContext()), test.getBytes());
        mTeleportClient.sendMessage("battery" + String.valueOf(SharedPreferences.getBoolean("battery", true, getApplicationContext())), test.getBytes());
        setUpAllColors();

        CheckBox seconds = (CheckBox) findViewById(R.id.seconds);
        seconds.setChecked(SharedPreferences.getBoolean("button", true, getApplicationContext()));
        seconds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                               @Override
                                               public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                   mTeleportClient.sendMessage("seconds"+String.valueOf(isChecked), String.valueOf(isChecked).getBytes());
                                                   SharedPreferences.saveBoolean("button", isChecked, getApplicationContext());
                                               }
                                           }
        );

        CheckBox battery = (CheckBox) findViewById(R.id.battery);
        battery.setChecked(SharedPreferences.getBoolean("battery", true, getApplicationContext()));
        battery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTeleportClient.sendMessage("battery"+String.valueOf(isChecked), String.valueOf(isChecked).getBytes());
                SharedPreferences.saveBoolean("battery", isChecked, getApplicationContext());
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

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            if (result.isFailure()) {
                Log.d("Billing", "Billing failed");
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                Log.d("Billing", "Bought item");
                consume();
            }
            SharedPreferences.saveBoolean("donation", true, getApplicationContext());

        }
    };

    public void consume() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
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
                mTeleportClient.sendMessage(color, color.getBytes());

                if (key < 3) {
                    SharedPreferences.saveInteger("id_background", key, getApplicationContext());
                    SharedPreferences.saveString("background_color", color, getApplicationContext());
                }
                else {
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


    public class MessageTask extends TeleportClient.OnGetMessageTask {

        @Override
        protected void onPostExecute(String path) {
            if (path.equals("sendData")) {
                mTeleportClient.sendMessage("seconds"+String.valueOf(SharedPreferences.getBoolean("button", true, getApplicationContext())), path.getBytes());
                mTeleportClient.sendMessage(SharedPreferences.getString("background_color", "#FF9800", getApplicationContext()), path.getBytes());
                mTeleportClient.sendMessage(SharedPreferences.getString("color", "#FAFAFA", getApplicationContext()), path.getBytes());
                mTeleportClient.sendMessage("battery"+String.valueOf(SharedPreferences.getBoolean("battery", true, getApplicationContext())), path.getBytes());
            }

            else {
                if (path.equals("#424242") || path.equals("#FAFAFA") || path.equals("#000000")) {
                    SharedPreferences.saveInteger("id_background", list.indexOf(path), getApplicationContext());
                } else {
                    SharedPreferences.saveInteger("id", list.indexOf(path), getApplicationContext());
                }
                setUpAllColors();
            }
            mTeleportClient.setOnGetMessageTask(new MessageTask());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mTeleportClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTeleportClient.disconnect();
    }
}