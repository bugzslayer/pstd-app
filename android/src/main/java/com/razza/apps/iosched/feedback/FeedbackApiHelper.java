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

package com.razza.apps.iosched.feedback;

import android.os.UserManager;

import com.razza.apps.iosched.provider.ScheduleContract;
import com.razza.apps.iosched.sync.userdata.util.UserDataHelper;
import com.razza.apps.iosched.util.LogUtils;
import com.turbomanage.httpclient.BasicHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;

/**
 * Sends feedback data to the server Feedback API.
 */
public class FeedbackApiHelper {

    private static final String TAG = LogUtils.makeLogTag(FeedbackApiHelper.class);

    private final String mUrl;

    private BasicHttpClient mHttpClient;

    /*Web Ideas Custom*/
    private static final HashMap<String, String> QUESTION_KEYS = new HashMap<String, String>();
    static {
        QUESTION_KEYS.put("Q10",ScheduleContract.Feedback.SESSION_RATING);
        QUESTION_KEYS.put("Q20",ScheduleContract.Feedback.ANSWER_RELEVANCE);
        QUESTION_KEYS.put("Q30",ScheduleContract.Feedback.ANSWER_CONTENT);
        QUESTION_KEYS.put("Q40",ScheduleContract.Feedback.ANSWER_SPEAKER);
        QUESTION_KEYS.put("Q50",ScheduleContract.Feedback.COMMENTS);
    }

    private static final HashMap<String, String> ANSWERS = new HashMap<String, String>();
    static {
        ANSWERS.put("aece21ff-2cbe-e411-b87f-00155d5066d7","1" );
        ANSWERS.put("afce21ff-2cbe-e411-b87f-00155d5066d7","2");
        ANSWERS.put("b0ce21ff-2cbe-e411-b87f-00155d5066d7","3" );
        ANSWERS.put("b1ce21ff-2cbe-e411-b87f-00155d5066d7","4");
        ANSWERS.put("b2ce21ff-2cbe-e411-b87f-00155d5066d7","5");
        ANSWERS.put("9bce21ff-2cbe-e411-b87f-00155d5066d7","1" );
        ANSWERS.put("9cce21ff-2cbe-e411-b87f-00155d5066d7","2");
        ANSWERS.put("9dce21ff-2cbe-e411-b87f-00155d5066d7","3");
        ANSWERS.put("9ece21ff-2cbe-e411-b87f-00155d5066d7","4");
        ANSWERS.put("9fce21ff-2cbe-e411-b87f-00155d5066d7","5");
        ANSWERS.put("a1ce21ff-2cbe-e411-b87f-00155d5066d7","1");
        ANSWERS.put("a2ce21ff-2cbe-e411-b87f-00155d5066d7","2");
        ANSWERS.put("a3ce21ff-2cbe-e411-b87f-00155d5066d7","3");
        ANSWERS.put("a4ce21ff-2cbe-e411-b87f-00155d5066d7","4");
        ANSWERS.put("a5ce21ff-2cbe-e411-b87f-00155d5066d7","5");
        ANSWERS.put("a8ce21ff-2cbe-e411-b87f-00155d5066d7","1");
        ANSWERS.put("a9ce21ff-2cbe-e411-b87f-00155d5066d7","2");
        ANSWERS.put("aace21ff-2cbe-e411-b87f-00155d5066d7","3" );
        ANSWERS.put("abce21ff-2cbe-e411-b87f-00155d5066d7","4");
        ANSWERS.put("acce21ff-2cbe-e411-b87f-00155d5066d7","5");
    }

    private static final HashMap<String, String> SPEAKER_ANSWERS = new HashMap<String, String>();
    static {


    }
    /*Web Ideas Custom*/

    public FeedbackApiHelper(BasicHttpClient httpClient, String url) {
        mHttpClient = httpClient;
        mUrl = url;
    }

    /**
     * Posts session feedback to the server. This method does network I/O and should run on
     * a background thread, do not call from the UI thread.
     *
     * @param sessionId The ID of the session that was reviewed.
     * @param questions
     * @return true if successful.
     */
    public boolean sendSessionToServer(String sessionId, HashMap<String, String> questions) {
        checkState(sessionId != null && !sessionId.isEmpty() && questions != null
                && questions.size() > 0, "Error posting session: some of the data is"
                + " invalid. SessionId " + sessionId + " Questions: " + questions);
        // TODO: Implement custom survey handling code here
        LogUtils.LOGE(TAG, "Survey handler implemented!");
        StringBuffer logValues = new StringBuffer();
        boolean firstEntry = true;
        try {
            for (Map.Entry<String, String> entry : questions.entrySet()) {
                if(firstEntry){
                    if(!entry.getKey().equals("Q50"))
                     logValues.append(QUESTION_KEYS.get(entry.getKey()) + ";" + ANSWERS.get(entry.getValue()));
                    else
                     logValues.append(QUESTION_KEYS.get(entry.getKey()) + ";" + entry.getValue().replace(" ","%20"));
                     firstEntry = false;
                } else {
                    if(!entry.getKey().equals("Q50"))
                        logValues.append("|" + QUESTION_KEYS.get(entry.getKey()) + ";" + ANSWERS.get(entry.getValue()));
                    else
                        logValues.append("|" + QUESTION_KEYS.get(entry.getKey()) + ";" + entry.getValue().replace(" ","%20"));
                }
            }
            /* webideas custom */
            LogUtils.LOGD(TAG, sendGet("http://www.webideas.ph/PSTD/recordFeedback.php?sid=" + sessionId + "&values=" + logValues.toString()));
        } catch (Exception e){
            LogUtils.LOGE(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    // HTTP GET request
    private String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();

    }


}
