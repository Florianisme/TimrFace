package com.timrface.watchfacelayout.util;

import com.timrface.watchfacelayout.config.Configuration;

public interface ConfigurationAwareBroadcastReceiver {

    void updateInternalConfigurationState(Configuration configuration);

}
