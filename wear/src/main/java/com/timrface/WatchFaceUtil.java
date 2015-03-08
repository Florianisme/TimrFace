package com.timrface;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public final class WatchFaceUtil {
    public static final String PATH_WITH_FEATURE = "/watch_face_config/Digital";
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_BACKGROUND = "#FF9800";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_BACKGROUND =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_BACKGROUND);
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_MAIN = "#FAFAFA";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_MAIN =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_MAIN);
    public static final String COLOR_NAME_DEFAULT_AND_AMBIENT_TEXT = "#424242";
    public static final int COLOR_VALUE_DEFAULT_AND_AMBIENT_TEXT =
            parseColor(COLOR_NAME_DEFAULT_AND_AMBIENT_TEXT);
    private static final String TAG = "DigitalWatchFaceUtil";
    public static String KEY_BACKGROUND_COLOR = "BACKGROUND_COLOR";
    public static String KEY_MAIN_COLOR = "MAIN_COLOR";
    public static String KEY_TEXT_COLOR = "TEXT_COLOR";
    public static boolean SMOOTH_SECONDS = true;

    private WatchFaceUtil() {
    }

    private static int parseColor(String colorName) {
        return Color.parseColor(colorName);
    }

    public static void fetchConfigDataMap(final GoogleApiClient client,
                                          final FetchConfigDataMapCallback callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
                new ResultCallback<NodeApi.GetLocalNodeResult>() {
                    @Override
                    public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme("wear")
                                .path(WatchFaceUtil.PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build();
                        Wearable.DataApi.getDataItem(client, uri)
                                .setResultCallback(new DataItemResultCallback(callback));
                    }
                }
        );
    }

    public static void overwriteKeysInConfigDataMap(final GoogleApiClient googleApiClient,
                                                    final DataMap configKeysToOverwrite) {

        WatchFaceUtil.fetchConfigDataMap(googleApiClient,
                new FetchConfigDataMapCallback() {
                    @Override
                    public void onConfigDataMapFetched(DataMap currentConfig) {
                        DataMap overwrittenConfig = new DataMap();
                        overwrittenConfig.putAll(currentConfig);
                        overwrittenConfig.putAll(configKeysToOverwrite);
                        WatchFaceUtil.putConfigDataItem(googleApiClient, overwrittenConfig);
                    }
                }
        );
    }

    public static void overwriteKeys(String key) {
        if (key.equals("true") || key.equals("false")) {
            SMOOTH_SECONDS = Boolean.valueOf(key);
        } else if (key.equals("#FAFAFA") || key.equals("#424242") || key.equals("#000000")) {
            KEY_MAIN_COLOR = key;
            if (key.equals("#FAFAFA")) {
                KEY_TEXT_COLOR = "#424242";
            } else {
                KEY_TEXT_COLOR = "#FAFAFA";
            }
        } else {
            KEY_BACKGROUND_COLOR = key;
        }
    }

    /**
     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
     * If the config DataItem doesn't exist, it's created.
     */
    public static void putConfigDataItem(GoogleApiClient googleApiClient, DataMap newConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "putDataItem result status: " + dataItemResult.getStatus());
                        }
                    }
                });
    }

    public interface FetchConfigDataMapCallback {
        void onConfigDataMapFetched(DataMap config);
    }

    private static class DataItemResultCallback implements ResultCallback<DataApi.DataItemResult> {

        private final FetchConfigDataMapCallback mCallback;

        public DataItemResultCallback(FetchConfigDataMapCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResult(DataApi.DataItemResult dataItemResult) {
            if (dataItemResult.getStatus().isSuccess()) {
                if (dataItemResult.getDataItem() != null) {
                    DataItem configDataItem = dataItemResult.getDataItem();
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                    DataMap config = dataMapItem.getDataMap();
                    mCallback.onConfigDataMapFetched(config);
                } else {
                    mCallback.onConfigDataMapFetched(new DataMap());
                }
            }
        }
    }
}