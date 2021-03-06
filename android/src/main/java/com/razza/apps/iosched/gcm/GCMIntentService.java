/*
 * Copyright 2014 Google Inc. All rights reserved.
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
package com.razza.apps.iosched.gcm;

import com.razza.apps.iosched.BuildConfig;
import com.razza.apps.iosched.gcm.command.*;
import com.google.android.gcm.GCMBaseIntentService;
import com.razza.apps.iosched.util.LogUtils;

import android.content.Context;
import android.content.Intent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.razza.apps.iosched.util.LogUtils.LOGD;
import static com.razza.apps.iosched.util.LogUtils.LOGE;
import static com.razza.apps.iosched.util.LogUtils.LOGI;
import static com.razza.apps.iosched.util.LogUtils.LOGW;
import static com.razza.apps.iosched.util.LogUtils.makeLogTag;

/**
 * {@link android.app.IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = LogUtils.makeLogTag("GCM");

    private static final Map<String, GCMCommand> MESSAGE_RECEIVERS;
    static {
        // Known messages and their GCM message receivers
        Map <String, GCMCommand> receivers = new HashMap<String, GCMCommand>();
        receivers.put("test", new TestCommand());
        receivers.put("announcement", new AnnouncementCommand());
        receivers.put("sync_schedule", new SyncCommand());
        receivers.put("sync_user", new SyncUserCommand());
        receivers.put("notification", new NotificationCommand());
        MESSAGE_RECEIVERS = Collections.unmodifiableMap(receivers);
    }

    public GCMIntentService() {
        super(BuildConfig.GCM_SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String regId) {
        LogUtils.LOGI(TAG, "Device registered: regId=" + regId);
    }

    @Override
    protected void onUnregistered(Context context, String regId) {
        LogUtils.LOGI(TAG, "Device unregistered");
        ServerUtilities.unregister(context, regId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        String extraData = intent.getStringExtra("extraData");
        LogUtils.LOGD(TAG, "Got GCM message, action=" + action + ", extraData=" + extraData);

        if (action == null) {
            LogUtils.LOGE(TAG, "Message received without command action");
            return;
        }

        action = action.toLowerCase();
        GCMCommand command = MESSAGE_RECEIVERS.get(action);
        if (command == null) {
            LogUtils.LOGE(TAG, "Unknown command received: " + action);
        } else {
            command.execute(this, action, extraData);
        }

    }

    @Override
    public void onError(Context context, String errorId) {
        LogUtils.LOGE(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        LogUtils.LOGW(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }
}
