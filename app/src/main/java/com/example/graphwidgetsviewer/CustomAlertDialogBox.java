package com.example.graphwidgetsviewer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class CustomAlertDialogBox {

    final String TAG = CustomAlertDialogBox.class.getSimpleName();

    private AlertDialog mDialog;
    private String mTitle;
    private String mMessage;
    private String mCancel;
    private String mOk;
    private ProgressBar progressBar;
    private Activity mActivity;

    final private Handler mHandler = new Handler(Looper.getMainLooper());

    private IActionResult mResponse;

    public CustomAlertDialogBox(final Activity activity,
                                final String title,
                                final String message,
                                final String cancel,
                                final String ok,
                                final IActionResult response) {
        mActivity = activity;
        mTitle = title;
        mMessage = message;
        mCancel = cancel;
        mOk = ok;
        mResponse = response;

        createAndRender();
    }

    private void createAndRender() {

        View dialogView = LayoutInflater.from(mActivity).inflate(R.layout.custom_alert_dialog, null);

// Initialize components
        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        TextView messageTextView = dialogView.findViewById(R.id.dialogMessage);
        progressBar = dialogView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        cancelButton.setText(mCancel);
        Button doneButton = dialogView.findViewById(R.id.doneButton);
        doneButton.setText(mOk);

        titleTextView.setText(mTitle);
        messageTextView.setText(mMessage);

// Set up the dialog
        Context context = new ContextThemeWrapper(mActivity, R.style.Theme_GraphWidgetsViewer);
        MaterialAlertDialogBuilder
            builder = new MaterialAlertDialogBuilder(context);
        builder.setView(dialogView);
        builder.setCancelable(false); // Set this to true if you want to dismiss the dialog by tapping outside

// Create the dialog

        mDialog = builder.create();
        mDialog.setCancelable(true);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                close(false);
            }
        });


// Set up click listener for the cancel button
        cancelButton.setOnClickListener(v -> close(false));

        doneButton.setOnClickListener(v -> {
            doneButton.setEnabled(false);
            start();
        });

//  Make rounded
        mDialog.getWindow().getDecorView().setBackgroundResource(R.drawable.rounded_dialog_background); // setting the background

//  Show the dialog
        mDialog.show();

    }

    public void close(final boolean result) {
        if (mResponse != null) {
            if (result) {
                mResponse.onSuccess();
            }
            else {
                mHandler.removeCallbacksAndMessages(null);
                mResponse.onFailed();
            }
        }
        mDialog.dismiss();
        mActivity = null;
    }

    public void start() {
        progressBar.setVisibility(View.VISIBLE);
        mHandler.postDelayed(() -> {
            boolean cuccess = true;
            if (cuccess) {
                close(true);
            }
            else {
                close(false);
            }
        }, 1000);

    }



}
