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

import com.razza.apps.iosched.R;
import com.razza.apps.iosched.framework.QueryEnum;
import com.razza.apps.iosched.framework.UpdatableView;
import com.razza.apps.iosched.ui.widget.NumberRatingBar;
import com.razza.apps.iosched.util.AnalyticsHelper;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment that lets the user submit feedback about a given session.
 */
public class SessionFeedbackFragment extends Fragment
        implements UpdatableView<SessionFeedbackModel> {

    private TextView mTitle;

    private TextView mSpeakers;

    private RatingBar mOverallFeedbackBar;

    private NumberRatingBar mSessionRelevantFeedbackBar;

    private NumberRatingBar mContentFeedbackBar;

    private NumberRatingBar mSpeakerFeedbackBar;

    private EditText txtComments;

    private List<UserActionListener> listeners = new ArrayList<UserActionListener>();

    public SessionFeedbackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.session_feedback_frag, container, false);

        mTitle = (TextView) rootView.findViewById(R.id.feedback_header_session_title);
        mSpeakers = (TextView) rootView.findViewById(R.id.feedback_header_session_speakers);
        mOverallFeedbackBar = (RatingBar) rootView.findViewById(R.id.rating_bar_0);
        mSessionRelevantFeedbackBar = (NumberRatingBar) rootView.findViewById(
                R.id.session_relevant_feedback_bar);
        mContentFeedbackBar = (NumberRatingBar) rootView.findViewById(R.id.content_feedback_bar);
        mSpeakerFeedbackBar = (NumberRatingBar) rootView.findViewById(R.id.speaker_feedback_bar);
        txtComments = (EditText) rootView.findViewById(R.id.txtComments);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Helps accessibility services determine the importance of this view.
            mOverallFeedbackBar.setImportantForAccessibility(RatingBar.IMPORTANT_FOR_ACCESSIBILITY_YES);

            // Automatically notifies the user about changes to the view's content description.
            mOverallFeedbackBar.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);
        }

        // When the rating changes, update the content description. In TalkBack mode, this
        // informs the user about the selected rating.
        mOverallFeedbackBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingBar.setContentDescription(
                        getString(R.string.updated_session_feedback_rating_bar_content_description, (int) rating));
            }
        });

        rootView.findViewById(R.id.submit_feedback_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submitFeedback();
                    }
                }
        );
        return rootView;
    }

    private void submitFeedback() {
        int overallAnswer = (int) mOverallFeedbackBar.getRating();
        int sessionRelevantAnswer = mSessionRelevantFeedbackBar.getProgress();
        int contentAnswer = mContentFeedbackBar.getProgress();
        int speakerAnswer = mSpeakerFeedbackBar.getProgress();
        String comments = "";
        comments = txtComments.getEditableText().toString();
        Bundle args = new Bundle();
        args.putInt(SessionFeedbackModel.DATA_RATING_INT, overallAnswer);
        args.putInt(SessionFeedbackModel.DATA_SESSION_RELEVANT_ANSWER_INT, sessionRelevantAnswer);
        args.putInt(SessionFeedbackModel.DATA_CONTENT_ANSWER_INT, contentAnswer);
        args.putInt(SessionFeedbackModel.DATA_SPEAKER_ANSWER_INT, speakerAnswer);
        args.putString(SessionFeedbackModel.DATA_COMMENT_STRING, comments);

        for (UserActionListener h1 : listeners) {
            h1.onUserAction(SessionFeedbackModel.SessionFeedbackUserActionEnum.SUBMIT, args);
        }

        getActivity().finish();
    }

    @Override
    public void displayData(SessionFeedbackModel model, QueryEnum query) {
        if (SessionFeedbackModel.SessionFeedbackQueryEnum.SESSION == query) {
            mTitle.setText(model.getSessionTitle());
            if (!TextUtils.isEmpty(model.getSessionSpeakers())) {
                mSpeakers.setText(model.getSessionSpeakers());
            } else {
                mSpeakers.setVisibility(View.GONE);
            }

            // ANALYTICS SCREEN: View Send Session Feedback screen
            // Contains: Session title
            AnalyticsHelper.sendScreenView("Feedback: " + model.getSessionTitle());
        }
    }

    @Override
    public void displayErrorMessage(QueryEnum query) {
        //Close the Activity
        getActivity().finish();
    }

    @Override
    public Uri getDataUri(QueryEnum query) {
        if (SessionFeedbackModel.SessionFeedbackQueryEnum.SESSION == query) {
            return ((SessionFeedbackActivity) getActivity()).getSessionUri();
        } else {
            return null;
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void addListener(UserActionListener listener) {
        listeners.add(listener);
    }
}
