package com.timrface.watchfacelayout.layout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.wearable.complications.ComplicationData;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import com.timrface.watchfacelayout.R;
import com.timrface.watchfacelayout.config.ComplicationSide;
import com.timrface.watchfacelayout.config.ComplicationType;
import com.timrface.watchfacelayout.config.Configuration;
import com.timrface.watchfacelayout.layout.components.*;
import com.timrface.watchfacelayout.layout.components.complications.BatteryLayout;
import com.timrface.watchfacelayout.layout.components.complications.Complication;
import com.timrface.watchfacelayout.layout.components.complications.StepsLayout;
import com.timrface.watchfacelayout.layout.components.complications.UnreadNotificationsLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LayoutProvider {

    private final List<Layout> layoutList = new ArrayList<>();
    private final List<Complication> complicationList = new ArrayList<>();
    private boolean inAmbientMode;

    private UnreadNotificationsLayout unreadNotificationComplication;
    private BatteryLayout batteryComplication;
    private StepsLayout stepsComplication;
    private int width;
    private int height;

    public LayoutProvider init(Configuration configuration, Context context) {
        Typeface robotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        Typeface robotoThin = Typeface.createFromAsset(context.getAssets(), "Roboto-Thin.ttf");
        Typeface robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");

        layoutList.add(new ChinLayout(configuration));
        layoutList.add(new TickLayout(configuration, robotoMedium));
        layoutList.add(new BackgroundLayout(configuration));
        layoutList.add(new TimeDigits(configuration, robotoLight, robotoThin));
        layoutList.add(new DateLayout(configuration, robotoLight));
        layoutList.add(new AmPmLayout(configuration, robotoLight));

        unreadNotificationComplication = buildUnreadNotificationComplication(configuration, context, robotoLight, robotoMedium);
        stepsComplication = buildStepsComplication(configuration, context, robotoLight, robotoMedium);
        batteryComplication = buildBatteryLayout(configuration, context, robotoLight, robotoMedium);

        complicationList.add(unreadNotificationComplication);
        complicationList.add(batteryComplication);

        return this;
    }

    private BatteryLayout buildBatteryLayout(Configuration configuration, Context context, Typeface robotoLight, Typeface robotoMedium) {
        VectorDrawableCompat battery = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_battery, context.getTheme());
        VectorDrawableCompat batteryOutline = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_battery_outline, context.getTheme());
        return new BatteryLayout(configuration, context, battery, batteryOutline, robotoMedium, robotoLight);
    }

    private StepsLayout buildStepsComplication(Configuration configuration, Context context, Typeface robotoLight, Typeface robotoMedium) {
        VectorDrawableCompat shoe = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_shoe, context.getTheme());
        return new StepsLayout(configuration, context, shoe, robotoMedium, robotoLight);
    }

    private UnreadNotificationsLayout buildUnreadNotificationComplication(Configuration configuration, Context context, Typeface robotoLight, Typeface robotoMedium) {
        VectorDrawableCompat notifications = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_notifications, context.getTheme());
        VectorDrawableCompat notificationsOutline = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_notifications_outline, context.getTheme());
        return new UnreadNotificationsLayout(configuration, notifications, notificationsOutline, robotoMedium, robotoLight);
    }

    public void onConfigurationChange(final Configuration configuration) {
        for (Layout layout : layoutList) {
            layout.updateConfiguration(configuration);
        }
        rebuildComplications(configuration);
        for (Complication complication : complicationList) {
            complication.updateConfiguration(configuration);
            complication.onSurfaceChanged(width, height);
        }
    }

    private void rebuildComplications(Configuration configuration) {
        complicationList.clear();
        if (configuration.getLeftComplicationType() == ComplicationType.BATTERY) {
            batteryComplication.setDrawSide(ComplicationSide.LEFT);
            complicationList.add(batteryComplication);
        }
        if (configuration.getMiddleComplicationType() == ComplicationType.BATTERY) {
            batteryComplication.setDrawSide(ComplicationSide.MIDDLE);
            complicationList.add(batteryComplication);
        }

        if (configuration.getLeftComplicationType() == ComplicationType.NOTIFICATIONS) {
            unreadNotificationComplication.setDrawSide(ComplicationSide.LEFT);
            complicationList.add(unreadNotificationComplication);
        }
        if (configuration.getMiddleComplicationType() == ComplicationType.NOTIFICATIONS) {
            unreadNotificationComplication.setDrawSide(ComplicationSide.MIDDLE);
            complicationList.add(unreadNotificationComplication);
        }

        if (configuration.getLeftComplicationType() == ComplicationType.STEPS) {
            stepsComplication.setDrawSide(ComplicationSide.LEFT);
            complicationList.add(stepsComplication);
        }
        if (configuration.getMiddleComplicationType() == ComplicationType.STEPS) {
            stepsComplication.setDrawSide(ComplicationSide.MIDDLE);
            complicationList.add(stepsComplication);
        }
    }

    public void onAmbientModeChanged(final boolean inAmbientMode) {
        this.inAmbientMode = inAmbientMode;
        for (Layout layout : layoutList) {
            layout.updateAmbientMode(inAmbientMode);
        }
        for (Complication complication : complicationList) {
            if (!inAmbientMode || complication.drawWhenInAmbientMode()) {
                complication.updateAmbientMode(inAmbientMode);
            }
        }
    }

    public void update(Canvas canvas, float centerX, float centerY, Calendar calendar) {
        if (inAmbientMode) {
            canvas.drawColor(Color.BLACK);
        }
        for (Layout layout : layoutList) {
            if (!inAmbientMode || layout.drawWhenInAmbientMode()) {
                layout.update(canvas, centerX, centerY, calendar);
            }
        }
        for (Complication complication : complicationList) {
            if (!inAmbientMode || complication.drawWhenInAmbientMode()) {
                complication.update(canvas, centerX, centerY, calendar);
            }
        }
    }

    public void applyWindowInsets(Resources resources) {
        final float infoTextSize = resources.getDimension(R.dimen.info_size);
        final float timeTextSize = resources.getDimension(R.dimen.text_size);
        final float tickTextSize = resources.getDimension(R.dimen.tick_size);

        WindowInsets windowInsets = new WindowInsets(timeTextSize, infoTextSize, tickTextSize);

        for (Layout layout : layoutList) {
            layout.applyWindowInsets(windowInsets);
        }

        batteryComplication.applyWindowInsets(windowInsets);
        unreadNotificationComplication.applyWindowInsets(windowInsets);
        stepsComplication.applyWindowInsets(windowInsets);
    }

    public void updateComplicationData(ComplicationData complicationData, ComplicationType updatedComplication, Context context) {
        for (Complication complication : complicationList) {
            if (complication.getComplicationType() == updatedComplication) {
                complication.onComplicationDataUpdate(complicationData, context);
            }
        }
    }

    public void onSurfaceChanged(int width, int height) {
        this.width = width;
        this.height = height;

        for (Complication complication : complicationList) {
            complication.onSurfaceChanged(width, height);
        }
        for (Layout layout : layoutList) {
            layout.onSurfaceChanged(width, height);
        }
    }
}
