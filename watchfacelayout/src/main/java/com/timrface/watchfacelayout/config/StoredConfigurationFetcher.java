package com.timrface.watchfacelayout.config;

import android.net.Uri;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.*;

import java.util.List;

public class StoredConfigurationFetcher {

    private static String getConnectedNodeId(List<Node> nodes) {
        for (Node node : nodes) {
            if (!node.isNearby()) {
                continue;
            }
            return node.getId();
        }
        throw new IllegalArgumentException("No connected node found");
    }

    public static void deleteInteractiveColorSetByOtherDevice(NodeClient nodeClient, final DataClient dataClient) {
        nodeClient.getConnectedNodes().addOnSuccessListener(new OnSuccessListener<List<Node>>() {
            @Override
            public void onSuccess(List<Node> nodes) {
                String connectedNodeId = StoredConfigurationFetcher.getConnectedNodeId(nodes);
                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(ConfigurationConstant.CONFIG_PATH.toString() + ConfigurationConstant.INTERACTIVE_COLOR.toString())
                        .authority(connectedNodeId)
                        .build();
                dataClient.deleteDataItems(uri);
            }
        });
    }

    public void updateConfig(NodeClient nodeClient, final DataClient dataClient, final Configuration configuration, final ConfigUpdateFinished configUpdateFinished) {
        nodeClient.getConnectedNodes().addOnSuccessListener(new OnSuccessListener<List<Node>>() {
            @Override
            public void onSuccess(List<Node> nodes) {
                getDataByNodeId(getConnectedNodeId(nodes), dataClient, configuration, configUpdateFinished);
            }
        });
    }

    private void getDataByNodeId(String nodeId, DataClient dataClient, final Configuration configuration, final ConfigUpdateFinished configUpdateFinished) {
        for (ConfigurationConstant configurationConstant : ConfigurationConstant.values()) {
            Uri uri = new Uri.Builder()
                    .scheme(PutDataRequest.WEAR_URI_SCHEME)
                    .path(ConfigurationConstant.CONFIG_PATH.toString() + configurationConstant.toString())
                    .authority("*")
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
