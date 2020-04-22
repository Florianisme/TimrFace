package com.timrface.watchfacelayout.config;

import android.net.Uri;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.*;

import java.util.List;

public class StoredConfigurationFetcher {

    public void updateConfig(NodeClient nodeClient, final DataClient dataClient, final Configuration configuration, final ConfigUpdateFinished configUpdateFinished) {
        nodeClient.getConnectedNodes().addOnSuccessListener(new OnSuccessListener<List<Node>>() {
            @Override
            public void onSuccess(List<Node> nodes) {
                for (Node node : nodes) {
                    if (!node.isNearby()) {
                        continue;
                    }
                    String nodeId = node.getId();

                    getDataByNodeId(nodeId, dataClient, configuration, configUpdateFinished);
                }
            }
        });
    }

    private void getDataByNodeId(String nodeId, DataClient dataClient, final Configuration configuration, final ConfigUpdateFinished configUpdateFinished) {
        for (ConfigurationConstant configurationConstant : ConfigurationConstant.values()) {
            Uri uri = new Uri.Builder()
                    .scheme(PutDataRequest.WEAR_URI_SCHEME)
                    .path(ConfigurationConstant.CONFIG_PATH.toString() + configurationConstant.toString())
                    .authority(nodeId)
                    .build();
            dataClient.getDataItems(uri).addOnSuccessListener(new OnSuccessListener<DataItemBuffer>() {
                @Override
                public void onSuccess(DataItemBuffer dataItems) {
                    for (DataItem item : dataItems) {
                        ConfigUpdater.updateConfig(configuration, item);
                    }
                    dataItems.release();
                    configUpdateFinished.onUpdateFinished(configuration);
                }
            });
        }
    }
}
