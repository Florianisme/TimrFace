package com.timrface.helper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.timrface.ConfigurationActivity;
import com.timrface.R;
import com.timrface.watchfacelayout.config.ComplicationType;

public class DisableItemArrayAdapter extends ArrayAdapter<String> {

    private int disabledId = -1;

    public DisableItemArrayAdapter(ConfigurationActivity configurationActivity, int simple_spinner_item, String[] stringArray) {
        super(configurationActivity, simple_spinner_item, stringArray);
    }

    public void setDisabledId(int id) {
        this.disabledId = id;
    }

    public void resetDisabledId() {
        disabledId = -1;
    }

    @Override
    public boolean isEnabled(int position) {
        if (disabledId == -1 || disabledId == ComplicationType.NONE.getId()) {
            return true;
        }
        return position != disabledId;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View mView = super.getDropDownView(position, convertView, parent);
        mView.setEnabled(isEnabled(position));
        return mView;
    }
}
