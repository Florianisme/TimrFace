package com.timrface;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.BoxInsetLayout;
import androidx.wear.widget.WearableRecyclerView;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.timrface.watchfacelayout.config.ConfigurationConstant;
import com.timrface.watchfacelayout.config.StoredConfigurationFetcher;

public class WatchFaceConfiguration extends Activity {

    private WearableRecyclerView mColorSelectionRecyclerView;
    private DigitalColorRecyclerViewAdapter mDigitalColorRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_face_config);

        BoxInsetLayout content = (BoxInsetLayout) findViewById(R.id.content);

        // BoxInsetLayout adds padding by default on round devices. Add some on square devices.
        content.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                if (!insets.isRound()) {
                    v.setPaddingRelative(
                            (int) getResources().getDimensionPixelSize(R.dimen.content_padding_start),
                            v.getPaddingTop(),
                            v.getPaddingEnd(),
                            v.getPaddingBottom());
                }
                return v.onApplyWindowInsets(insets);
            }
        });

        mColorSelectionRecyclerView = findViewById(R.id.color_picker_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mColorSelectionRecyclerView.setEdgeItemsCenteringEnabled(true);

        mColorSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        String[] colorsValues = getResources().getStringArray(R.array.colors);
        String[] colorsNames = getResources().getStringArray(R.array.color_array);


        mDigitalColorRecyclerViewAdapter = new DigitalColorRecyclerViewAdapter(colorsValues, colorsNames);
        mColorSelectionRecyclerView.setAdapter(mDigitalColorRecyclerViewAdapter);
    }

    private void updateConfigDataItem(final String accentColor) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest
                .create(ConfigurationConstant.CONFIG_PATH.toString() + ConfigurationConstant.INTERACTIVE_COLOR.toString())
                .setUrgent();
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putString(ConfigurationConstant.INTERACTIVE_COLOR.toString(), accentColor);
        Wearable.getDataClient(this).putDataItem(putDataMapRequest.asPutDataRequest());

        StoredConfigurationFetcher.deleteInteractiveColorSetByOtherDevice(Wearable.getNodeClient(this), Wearable.getDataClient(this));
        finish();
    }

    private class DigitalColorRecyclerViewAdapter extends
            RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final String[] colorValues;
        private final String[] colorNames;

        public DigitalColorRecyclerViewAdapter(String[] colorValues, String[] colorNames) {
            this.colorValues = colorValues;
            this.colorNames = colorNames;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new DigitalColorViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.color_picker_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            String colorValue = colorValues[position];
            String colorName = colorNames[position];

            Integer color = Color.parseColor(colorValue);

            DigitalColorViewHolder colorViewHolder = (DigitalColorViewHolder) viewHolder;
            colorViewHolder.setColor(color);
            colorViewHolder.setColorName(colorName);
        }

        @Override
        public int getItemCount() {
            return colorValues.length;
        }

        public class DigitalColorViewHolder extends RecyclerView.ViewHolder implements
                View.OnClickListener {

            private CircledImageView colorValue;
            private TextView colorName;

            public DigitalColorViewHolder(final View view) {
                super(view);
                colorValue = view.findViewById(R.id.color);
                colorName = view.findViewById(R.id.label);

                view.setOnClickListener(this);
            }

            public void setColor(int color) {
                colorValue.setCircleColor(color);
            }

            public void setColorName(String name) {
                colorName.setText(name);
            }

            @Override
            public void onClick (View view) {
                int position = getAdapterPosition();

                updateConfigDataItem(colorValues[position]);
            }
        }
    }
}