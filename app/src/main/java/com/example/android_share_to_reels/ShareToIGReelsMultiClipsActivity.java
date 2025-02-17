/* Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.android_share_to_reels;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class ShareToIGReelsMultiClipsActivity extends AppCompatActivity {
    final int videoRequestCode = 42;
    final String igAppName = "Instagram";
    final String igReelsIntentName = "com.instagram.share.ADD_TO_REEL_MULTIPLE";
    final String sourceApplicationKey = "source_application";
    final String appIDKey = "com.instagram.platform.extra.APPLICATION_ID";
    final String mediaType = "video/*";

    private VideoView videoToShare;
    private Button btnLoadVideo, btnShareToIGReels, btnShareToIGReelsWithSticker;
    private ArrayList<Button> buttons;
    private ArrayList<Uri> videoList;
    private Uri targetUri;
    private String appID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to_ig_reels_multi_clips);

        final Activity activity = this;

        videoList = new ArrayList();
        appID = getString(R.string.facebook_app_id);
        getTargetElements();
        buttons = new ArrayList<>(Arrays.asList(btnLoadVideo, btnShareToIGReels, btnShareToIGReelsWithSticker));
        loadInitialState();
        Uri stickerAssetUri = Utils.getSampleSticker(getApplicationContext());

        btnLoadVideo.setOnClickListener(view -> {
            Intent intent = Utils.getVideoUploaderIntent();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            activity.startActivityForResult(intent, videoRequestCode);
        });

        btnShareToIGReels.setOnClickListener(view -> {
            Intent intent = buildIGReelsIntent();

            Utils.startActivityWithSharedContent(activity, intent, igAppName);
        });

        btnShareToIGReelsWithSticker.setOnClickListener(view -> {
            Intent intent = buildIGReelsIntent();
            Utils.configureStickerURI(activity, intent, stickerAssetUri, Utils.igPackageName);

            Utils.startActivityWithSharedContent(activity, intent, igAppName);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Utils.loadVideoPreview(videoToShare, targetUri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && data != null && requestCode == videoRequestCode) {
            for (Button b : buttons) {
                Utils.changeButtonStatus(b, true);
            }

            videoList.clear();

            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    if (i == 0) {
                        targetUri = uri;
                    }
                    videoList.add(uri);
                }
            } else {
                Uri uri = data.getData();
                videoList.add(uri);
                targetUri = uri;
            }
        }

        Utils.loadVideoPreview(videoToShare, targetUri);
    }

    private void getTargetElements() {
        videoToShare = findViewById(R.id.videoToShare);
        btnLoadVideo = findViewById(R.id.btnLoadVideo);
        btnShareToIGReels = findViewById(R.id.btnShareToIGReels);
        btnShareToIGReelsWithSticker = findViewById(R.id.btnShareToIGReelsWithSticker);
    }

    private void loadInitialState() {
        for (Button b : buttons) {
            Utils.changeButtonStatus(b, true);
        }

        loadSampleVideo();
    }

    private void loadSampleVideo() {
        targetUri = Utils.getSampleVideo(getApplicationContext());
        videoList.add(targetUri);

        Utils.loadVideoPreview(videoToShare, targetUri);
    }

    private Intent buildIGReelsIntent() {
        Intent intent = Utils.getInitialIntent(igReelsIntentName);
        intent.setDataAndType(targetUri, mediaType);
        intent.setPackage(Utils.igPackageName);

        intent.putExtra(sourceApplicationKey, this.getPackageName());
        intent.putExtra(appIDKey, appID);

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, videoList);
        for (Uri uri : videoList) {
            Utils.giveUriPermissions(this, intent, uri, Utils.igPackageName);
        }

        return intent;
    }
}
