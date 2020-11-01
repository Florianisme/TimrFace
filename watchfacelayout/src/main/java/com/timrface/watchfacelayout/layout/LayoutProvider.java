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
    private final List<Complication> activeComplicationList = new ArrayList<>();
    private final List<Complication> allComplicationsList = new ArrayList<>();
    private boolean inAmbientMode;

    private UnreadNotificationsLayout unreadNotificationComplication;
    private BatteryLayout batteryComplication;
    private StepsLayout stepsComplication;

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

        activeComplicationList.add(unreadNotificationComplication);
        activeComplicationList.add(batteryComplication);

        allComplicationsList.add(unreadNotificationComplication);
        allComplicationsList.add(stepsComplication);
        allComplicationsList.add(batteryComplication);

        return this;
    }

    private BatteryLayout buildBatteryLayout(Configuration configuration, Context context, Typeface robotoLight, Typeface robotoMedium) {
        VectorDrawableCompat battery = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_battery, context.getTheme());
        VectorDrawableCompat batteryOutline = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_battery_outline, context.getTheme());
        return new BatteryLayout(configuration, battery, batteryOutline, robotoMedium, robotoLight);
    }

    private StepsLayout buildStepsComplication(Configuration configuration, Context context, Typeface robotoLight, Typeface robotoMedium) {
        VectorDrawableCompat shoe = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_shoe, context.getTheme());
        return new StepsLayout(configuration, shoe, robotoMedium, robotoLight);
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
        for (Complication complication : allComplicationsList) {
            complication.updateConfiguration(configuration);
        }
    }

    private void rebuildComplications(Configuration configuration) {
        activeComplicationList.clear();
        if (configuration.getLeftComplicationType() == ComplicationType.BATTERY) {
            batteryComplication.setDrawSide(ComplicationSide.LEFT);
            activeComplicationList.add(batteryComplication);
        }
        if (configuration.getMiddleComplicationType() == ComplicationType.BATTERY) {
            batteryComplication.setDrawSide(ComplicationSide.MIDDLE);
            activeComplicationList.add(batteryComplication);
        }

        if (configuration.getLeftComplicationType() == ComplicationType.NOTIFICATIONS) {
            unreadNotificationComplication.setDrawSide(ComplicationSide.LEFT);
            activeComplicationList.add(unreadNotificationComplication);
        }
        if (configuration.getMiddleComplicationType() == ComplicationType.NOTIFICATIONS) {
            unreadNotificationComplication.setDrawSide(ComplicationSide.MIDDLE);
            activeComplicationList.add(unreadNotificationComplication);
        }

        if (configuration.getLeftComplicationType() == ComplicationType.STEPS) {
            stepsComplication.setDrawSide(ComplicationSide.LEFT);
            activeComplicationList.add(stepsComplication);
        }
        if (configuration.getMiddleComplicationType() == ComplicationType.STEPS) {
            stepsComplication.setDrawSide(ComplicationSide.MIDDLE);
            activeComplicationList.add(stepsComplication);
        }
    }

    public void onAmbientModeChanged(final boolean inAmbientMode) {
        this.inAmbientMode = inAmbientMode;
        for (Layout layout : layoutList) {
            layout.updateAmbientMode(inAmbientMode);
        }
        for (Complication complication : allComplicationsList) {
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
        for (Complication complication : activeComplicationList) {
            if (!inAmbientMode || complication.drawWhenInAmbientMode()) {
                complication.update(canvas, centerX, centerY, calendar);
            }
        }
    }

    public void applyWindowInsets(Resources resources) {
        final float infoTextSize = resources.getDimension(R.dimen.info_size);
        final float timeTextSize = resources.getDimension(R.dimen.text_size);
        final float tickTextSize = resources.getDimension(R.dimen.tick_size);

        final float tickHorizontalDistance = resources.getDimension(R.dimen.tick_horizontal_distance);
        final float tickBottomDistance = resources.getDimension(R.dimen.tick_bottom_distance);
        final float thinTickWidth = resources.getDimension(R.dimen.tick_thin_width);
        final float tickWidth = resources.getDimension(R.dimen.tick_width);
        final float shortTickHeight = resources.getDimension(R.dimen.short_tick_height);
        final float tickHeight = resources.getDimension(R.dimen.tick_height);

        WindowInsets windowInsets = new WindowInsets(timeTextSize, infoTextSize, tickTextSize, tickHorizontalDistance, tickBottomDistance, thinTickWidth, tickWidth, shortTickHeight, tickHeight);

        for (Layout layout : layoutList) {
            layout.applyWindowInsets(windowInsets);
        }

        for (Complication complication : allComplicationsList) {
            complication.applyWindowInsets(windowInsets);
        }
    }

    public void updateComplicationData(ComplicationData complicationData, ComplicationType updatedComplication, Context context) {
        for (Complication complication : activeComplicationList) {
            if (complication.getComplicationType() == updatedComplication) {
                complication.onComplicationDataUpdate(complicationData, context);
            }
        }
    }

    public void onSurfaceChanged(int width, int height) {
        for (Complication complication : allComplicationsList) {
            complication.onSurfaceChanged(width, height);
        }
        for (Layout layout : layoutList) {
            layout.onSurfaceChanged(width, height);
        }
    }
}
