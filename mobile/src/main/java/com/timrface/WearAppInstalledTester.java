/*
 * Copyright (C) 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.timrface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Checks if the sample's Wear app is installed on remote Wear device(s). If it is not, allows the
 * user to open the app listing on the Wear devices' Play Store.
 */
public class WearAppInstalledTester implements CapabilityClient.OnCapabilityChangedListener {

    // Name of capability listed in Wear app's wear.xml.
    // IMPORTANT NOTE: This should be named differently than your Phone app's capability.
    private static final String CAPABILITY_WEAR_APP = "verify_com_timrface_installed";

    // Links to Wear app (Play Store).
    // TODO: Replace with your links/packages.
    private static final String PLAY_STORE_APP_URI =
            "market://details?id=com.timrface";

    // Result from sending RemoteIntent to wear device(s) to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == RemoteIntent.RESULT_OK) {
                Toast toast = Toast.makeText(
                        activity, R.string.wear_tester_opening_playstore,
                        Toast.LENGTH_SHORT);
                toast.show();

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                Toast toast = Toast.makeText(
                        activity,
                        "Play Store Request Failed. Wear device(s) may not support Play Store, "
                                + " that is, the Wear device may be version 1.0.",
                        Toast.LENGTH_LONG);
                toast.show();

            } else {
                Log.e("WeatAppInstalledTester", "Unexpected result: " + resultCode);
            }
        }
    };

    private final Activity activity;

    private Set<Node> mWearNodesWithApp;
    private List<Node> mAllConnectedNodes;

    public WearAppInstalledTester(Activity activity) {
        this.activity = activity;
        Wearable.getCapabilityClient(activity).addListener(this, CAPABILITY_WEAR_APP);
        findWearDevicesWithApp();
        findAllWearDevices();
    }


    /*
     * Updates UI when capabilities change (install/uninstall wear app).
     */
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        mWearNodesWithApp = capabilityInfo.getNodes();

        // Because we have an updated list of devices with/without our app, we need to also update
        // our list of active Wear devices.
        findAllWearDevices();

        verifyNodeAndUpdateUI();
    }

    private void findWearDevicesWithApp() {
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(activity)
                .getCapability(CAPABILITY_WEAR_APP, CapabilityClient.FILTER_ALL);

        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(Task<CapabilityInfo> task) {

                if (task.isSuccessful()) {
                    CapabilityInfo capabilityInfo = task.getResult();
                    mWearNodesWithApp = capabilityInfo.getNodes();

                    verifyNodeAndUpdateUI();
                }
            }
        });
    }

    private void findAllWearDevices() {
        Task<List<Node>> NodeListTask = Wearable.getNodeClient(activity).getConnectedNodes();

        NodeListTask.addOnCompleteListener(new OnCompleteListener<List<Node>>() {
            @Override
            public void onComplete(Task<List<Node>> task) {
                if (task.isSuccessful()) {
                    mAllConnectedNodes = task.getResult();
                }

                verifyNodeAndUpdateUI();
            }
        });
    }

    private void verifyNodeAndUpdateUI() {
        if (mAllConnectedNodes == null || mWearNodesWithApp == null) {
            return;
        }

        if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
            showInstallDialog();
        }
    }

    private void showInstallDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_app_missing_title)
                .setMessage(R.string.dialog_app_missing_message)
                .setPositiveButton(R.string.dialog_app_missing_okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openPlayStoreOnWearDevicesWithoutApp();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_app_missing_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast toast = new Toast(activity);
                        toast.setText(R.string.dialog_app_missing_later_message);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.show();
                    }
                }).create();
        alertDialog.show();
    }

    private void openPlayStoreOnWearDevicesWithoutApp() {
        // Create a List of Nodes (Wear devices) without your app.
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();

        for (Node node : mAllConnectedNodes) {
            if (!mWearNodesWithApp.contains(node)) {
                nodesWithoutApp.add(node);
            }
        }

        if (!nodesWithoutApp.isEmpty()) {
            Intent intent =
                    new Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(PLAY_STORE_APP_URI));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(
                        activity,
                        intent,
                        mResultReceiver,
                        node.getId());
            }
        }
    }
}