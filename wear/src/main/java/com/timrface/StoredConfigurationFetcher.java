package com.timrface;

import android.net.Uri;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.*;
import com.timrface.watchfacelayout.Configuration;

import java.util.List;

public class StoredConfigurationFetcher {

    public void updateConfig(NodeClient nodeClient, DataClient dataClient, Configuration configuration, ConfigUpdateFinished configUpdateFinished) {
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

    private void getDataByNodeId(String nodeId, DataClient dataClient, Configuration configuration, ConfigUpdateFinished configUpdateFinished) {
        String basePath = "/watch_face_config";
        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(basePath)
                .authority(nodeId)
                .build();
        dataClient.getDataItems(uri).addOnSuccessListener(new OnSuccessListener<DataItemBuffer>() {
            @Override
            public void onSuccess(DataItemBuffer dataItems) {
                for (DataItem item : dataItems) {
                    ConfigUpdater.updateConfig(configuration, item);
                }
                dataItems.release();
                configUpdateFinished.onUpdateFinished();
            }
        });
    }
}
