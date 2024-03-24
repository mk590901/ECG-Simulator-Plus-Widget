package com.example.graphwidgetsviewer;

import static com.example.graphwidgetsviewer.R.string.exit_app;
import static com.example.graphwidgetsviewer.Utils.randomInRange;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    final String TAG = MainActivity.class.getSimpleName();
    private FloatingActionButton mFab = null;
    private ScrollView mScrollView = null;
    private LinearLayout mContainerLayout = null;

    private OnBackPressedCallback onBackPressedCallback;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollView = findViewById(R.id.verticalScrollView);
        mContainerLayout = findViewById(R.id.containerLayout);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(v -> mHandler.post(() -> addCard()));

        createAndAddBackDispatcher();

    }

    private void createAndAddBackDispatcher() {
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle back button press here
                // For example, you can navigate back or perform any desired action
                // You can also call isEnabled() to check if callback is enabled or not
                Log.d(TAG, "MainActivity.handleOnBackPressed");

                showDialog();
            }
        };
        // Adding the callback to the OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    private void showDialog() {
        new CustomAlertDialogBox(MainActivity.this,
                getString(exit_app),
                getString(R.string.are_you_sure),
                getString(R.string.cancel),
                getString(R.string.ok),
                new IActionResult() {
                    @Override
                    public void onSuccess() {
                        stopAll();
                        onBackPressedCallback.remove();
                        finish();
                    }

                    @Override
                    public void onFailed() {
                    }
                });
    }

    private void stopAll() {
        int childCount = getCardsNumber();
        if (childCount == 0) {
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View view = mContainerLayout.getChildAt(i);
            if (view instanceof CardView) {
                CardView cardView = (CardView) view;
                GraphWidget widget = cardView.findViewById(R.id.painting_view);
                widget.stop();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the callback when fragment is destroyed
        if (onBackPressedCallback != null) {
            onBackPressedCallback.remove();
        }
        Log.d(TAG, "onDestroy!");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop!");
    }

    @Override
    protected void onPause() {
        super.onPause();
    //  --- some code --
        Log.d(TAG,"onPause!");
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume!");
    }

    private void addCard() {
        Log.d(TAG, "addCard");
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        CardView cardView = createWidget(inflater);
    //  Add CardView to the container
        mContainerLayout.addView(cardView);
        scrollToViewTop(cardView);
    }

    private void scrollToViewTop(final View view) {
        if (view == null) {
            return;
        }
        int heightInDp = 8;
        int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                heightInDp, getResources().getDisplayMetrics());

        mScrollView.post(() -> mScrollView.smoothScrollTo(0, view.getTop() - heightInPixels));
    }
    private CardView createWidget(LayoutInflater inflater) {
        CardView cardView = (CardView)inflater.inflate(R.layout.card_view_layout, mContainerLayout, false);
        String mUuid = UUID.randomUUID().toString();
        cardView.setTag(mUuid);
        TextView title = cardView.findViewById(R.id.title_widget);
        title.setText( "ECG #" + (getCardsNumber() + 1));
        GraphWidget paintingView = cardView.findViewById(R.id.painting_view);

        StoreWrapper.GraphMode
            mode = (getCardsNumber() % 2 == 0) ? StoreWrapper.GraphMode.overlay : StoreWrapper.GraphMode.flowing;
        paintingView.setMode(randomInRange(128, 320), mode, false);

        SwitchCompat switchSimulationButton = cardView.findViewById(R.id.switch_simulation_button);

        // Set an OnCheckedChangeListener
        switchSimulationButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Perform actions when the switch state changes
            if (isChecked) {
                // Switch is ON
                switchSimulationButton.setText(getString(R.string.simulation_on));
                paintingView.setSimulationMode(true);
                Log.d(TAG, "SIMULATION ON");
            } else {
                // Switch is OFF
                switchSimulationButton.setText(getString(R.string.simulation_off));
                paintingView.setSimulationMode(false);
                Log.d(TAG, "SIMULATION OFF");
            }
        });

        switchSimulationButton.setText(paintingView.isSimulationMode() ? getString(R.string.simulation_on) : getString(R.string.simulation_off));
        switchSimulationButton.setChecked(paintingView.isSimulationMode() ? true : false);

        SwitchCompat switchGraphModeButton = cardView.findViewById(R.id.switch_mode_button);

        // Set an OnCheckedChangeListener
        switchGraphModeButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Perform actions when the switch state changes
            if (isChecked) {
                // Switch is ON
                switchGraphModeButton.setText(getString(R.string.flowing));
                paintingView.setMode(StoreWrapper.GraphMode.flowing);

                Log.d(TAG, "FLOWING");
            } else {
                // Switch is OFF
                switchGraphModeButton.setText(getString(R.string.overlay));
                paintingView.setMode(StoreWrapper.GraphMode.overlay);

                Log.d(TAG, "OVERLAY");
            }
        });

        switchGraphModeButton.setText(mode == StoreWrapper.GraphMode.flowing ? getString(R.string.flowing) : getString(R.string.overlay));
        switchGraphModeButton.setChecked(mode == StoreWrapper.GraphMode.flowing ? true : false);

        ImageView delete = cardView.findViewById(R.id.cancel_button);
        delete.setOnClickListener(v -> {
            confirmDeleteWidget(cardView);
        });
        paintingView.start();
        return cardView;
    }

    private void confirmDeleteWidget(CardView cardView) {
        TextView title = cardView.findViewById(R.id.title_widget);
        String widgetName = title.getText().toString();
        new CustomAlertDialogBox(MainActivity.this,
                getString(R.string.delete_widget) + " [" + widgetName + "]",
                getString(R.string.are_you_sure),
                getString(R.string.cancel),
                getString(R.string.ok),
                new IActionResult() {
                    @Override
                    public void onSuccess() {
                        GraphWidget widget = cardView.findViewById(R.id.painting_view);
                        widget.stop();
                        deleteCard((String)cardView.getTag());
                    }

                    @Override
                    public void onFailed() {
                    }
                });
    }

    private void deleteCard(final String tag) {
        Log.d(TAG, "Delete [" + tag + "]");
        int index = getIndex(tag);
        if (index < 0 || index >= getCardsNumber()) {
            return;
        }
        removeElementAt(index);
        scrollToFirst();
    }

    private void scrollToFirst() {
        int childCount = getCardsNumber();
        if (childCount == 0) {
            return;
        }

        int heightInDp = 8;
        int heightInPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                heightInDp, getResources().getDisplayMetrics());

        mScrollView.post(() -> {
            View firstChild = mContainerLayout.getChildAt(0);
            if (firstChild != null) {
                //  No top, getTop() - (android:layout_margin="16dp" in card_view_layout, I hope)
                mScrollView.smoothScrollTo(0, firstChild.getTop() - heightInPixels);
            }
        });
    }

    private void removeElementAt(int index) {
        int childCount = getCardsNumber();
        if (index < 0 || index >= childCount) {
            return;
        }
        mContainerLayout.removeViewAt(index);
    }

    private int getIndex(final String tag) {
        int childCount = getCardsNumber();
        if (childCount == 0) {
            return -1;
        }
        for (int i = 0; i < childCount; i++) {
            View view = mContainerLayout.getChildAt(i);
            if (view instanceof CardView) {
                CardView cardView = (CardView) view;
                if (cardView.getTag() != null && tag.equalsIgnoreCase((String) cardView.getTag())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void scrollToLast() {
        int childCount = getCardsNumber();
        mScrollView.post(() -> {
            View lastChild = mContainerLayout.getChildAt(childCount - 1);
            if (lastChild != null) {
                mScrollView.smoothScrollTo(0, lastChild.getBottom());
            }
        });
    }

    private int getCardsNumber() {
        return mContainerLayout.getChildCount();
    }

}