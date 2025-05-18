package com.example.boundvision;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private ConstraintLayout mainLayout;
    private CardView scoreCardView, sensorDataCardView;
    private View circleOne, circleTwo, circleThree;
    private Button sixButton, fourButton, wicketButton;
    private TextView scoreTextView;
    private VideoView sixVideoView;

    // Sensor display components
    private TextView distanceValueText, soundValueText, vibrationValueText, foilContactValueText;
    private View foilIndicator, vibrationIndicator, soundIndicator;
    private TextView monitoringStatusText, monitoringTimeText;
    private CardView eventNotificationCard;
    private TextView eventNotificationText;

    // Firebase reference
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference scoreReference;
    private DatabaseReference sensorReference;
    private DatabaseReference systemReference;
    private DatabaseReference detectionReference;

    // Monitoring state
    private boolean isMonitoring = false;
    private Handler monitoringHandler = new Handler();
    private Runnable monitoringRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        initViews();

        // Initialize Firebase
        setupFirebase();

        // Set up VideoView
        setupVideoView();

        // Set click listeners for demo buttons
        setupDemoButtons();
    }

    private void initViews() {
        mainLayout = findViewById(R.id.mainLayout);
        scoreCardView = findViewById(R.id.scoreCardView);
        sensorDataCardView = findViewById(R.id.sensorDataCardView);
        circleOne = findViewById(R.id.circleOne);
        circleTwo = findViewById(R.id.circleTwo);
        circleThree = findViewById(R.id.circleThree);
        sixButton = findViewById(R.id.sixButton);
        fourButton = findViewById(R.id.fourButton);
        wicketButton = findViewById(R.id.wicketButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        sixVideoView = findViewById(R.id.sixVideoView);

        // Sensor display elements
        distanceValueText = findViewById(R.id.distanceValueText);
        soundValueText = findViewById(R.id.soundValueText);
        vibrationValueText = findViewById(R.id.vibrationValueText);
        foilContactValueText = findViewById(R.id.foilContactValueText);

        foilIndicator = findViewById(R.id.foilIndicator);
        vibrationIndicator = findViewById(R.id.vibrationIndicator);
        soundIndicator = findViewById(R.id.soundIndicator);

        monitoringStatusText = findViewById(R.id.monitoringStatusText);
        monitoringTimeText = findViewById(R.id.monitoringTimeText);

        eventNotificationCard = findViewById(R.id.eventNotificationCard);
        eventNotificationText = findViewById(R.id.eventNotificationText);

        // Initially hide notification
        eventNotificationCard.setVisibility(View.GONE);
    }

    private void setupVideoView() {
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(sixVideoView);
        sixVideoView.setMediaController(mediaController);

        sixVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                sixVideoView.setVisibility(View.GONE);
            }
        });
    }

    private void setupFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        scoreReference = firebaseDatabase.getReference("cricket_score");
        sensorReference = firebaseDatabase.getReference("sensor");
        systemReference = firebaseDatabase.getReference("system");
        detectionReference = firebaseDatabase.getReference("detection");

        // Score listener
        scoreReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String scoreType = snapshot.child("type").getValue(String.class);
                    if (scoreType != null) {
                        switch (scoreType) {
                            case "SIX":
                                showSixAnimation();
                                break;
                            case "FOUR":
                                showFourAnimation();
                                break;
                            case "WICKET":
                                showWicketAnimation();
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Sensor data listener
        sensorReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Distance
                    if (snapshot.hasChild("distance")) {
                        Object distanceObj = snapshot.child("distance").getValue();
                        if (distanceObj != null) {
                            try {
                                double distance;
                                if (distanceObj instanceof Long) {
                                    distance = ((Long) distanceObj).doubleValue();
                                } else if (distanceObj instanceof Double) {
                                    distance = (Double) distanceObj;
                                } else {
                                    distance = Double.parseDouble(distanceObj.toString());
                                }
                                distanceValueText.setText(String.format("%.1f cm", distance));
                                distanceValueText.setTextColor(distance < 15 ? Color.RED : Color.BLACK);
                            } catch (Exception e) {
                                distanceValueText.setText("N/A cm");
                            }
                        }
                    }

                    // Sound
                    if (snapshot.hasChild("sound")) {
                        Object soundObj = snapshot.child("sound").getValue();
                        if (soundObj != null) {
                            try {
                                long sound;
                                if (soundObj instanceof Long) {
                                    sound = (Long) soundObj;
                                } else if (soundObj instanceof Double) {
                                    sound = ((Double) soundObj).longValue();
                                } else {
                                    sound = Long.parseLong(soundObj.toString());
                                }
                                soundValueText.setText(String.valueOf(sound));
                                soundValueText.setTextColor(sound > 1000 ? Color.RED : Color.BLACK);
                            } catch (Exception e) {
                                soundValueText.setText("N/A");
                            }
                        }
                    }

                    // Vibration
                    if (snapshot.hasChild("vibration")) {
                        Boolean vibration = snapshot.child("vibration").getValue(Boolean.class);
                        if (vibration != null) {
                            vibrationValueText.setText(vibration ? "ACTIVE" : "Inactive");
                            vibrationValueText.setTextColor(vibration ? Color.RED : Color.BLACK);
                        }
                    }

                    // Foil Contact
                    if (snapshot.hasChild("foil_contact")) {
                        Boolean foilContact = snapshot.child("foil_contact").getValue(Boolean.class);
                        if (foilContact != null) {
                            foilContactValueText.setText(foilContact ? "CONTACT" : "No Contact");
                            foilContactValueText.setTextColor(foilContact ? Color.RED : Color.BLACK);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // System status listener
        systemReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("monitoring_active")) {
                        Boolean active = snapshot.child("monitoring_active").getValue(Boolean.class);
                        if (active != null) {
                            isMonitoring = active;
                            updateMonitoringStatus(active);

                            if (active) {
                                if (snapshot.hasChild("monitoring_remaining_ms")) {
                                    Object timeObj = snapshot.child("monitoring_remaining_ms").getValue();
                                    if (timeObj != null) {
                                        try {
                                            long remainingMs;
                                            if (timeObj instanceof Long) {
                                                remainingMs = (Long) timeObj;
                                            } else if (timeObj instanceof Double) {
                                                remainingMs = ((Double) timeObj).longValue();
                                            } else {
                                                remainingMs = Long.parseLong(timeObj.toString());
                                            }
                                            updateMonitoringCountdown(remainingMs);
                                        } catch (Exception e) {
                                            monitoringTimeText.setText("Error");
                                        }
                                    }
                                }
                            } else {
                                if (monitoringRunnable != null) {
                                    monitoringHandler.removeCallbacks(monitoringRunnable);
                                }
                                monitoringTimeText.setText("");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Detection listener
        detectionReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean foilDetected = false;
                    boolean vibrationDetected = false;
                    boolean soundDetected = false;

                    if (snapshot.hasChild("foil_contact")) {
                        Boolean detected = snapshot.child("foil_contact").getValue(Boolean.class);
                        foilDetected = detected != null && detected;
                    }

                    if (snapshot.hasChild("vibration")) {
                        Boolean detected = snapshot.child("vibration").getValue(Boolean.class);
                        vibrationDetected = detected != null && detected;
                    }

                    if (snapshot.hasChild("sound")) {
                        Boolean detected = snapshot.child("sound").getValue(Boolean.class);
                        soundDetected = detected != null && detected;
                    }

                    updateDetectionIndicators(foilDetected, vibrationDetected, soundDetected);

                    if (foilDetected && !vibrationDetected && !soundDetected) {
                        showEventNotification("Possible SIX RUNS: Player caught ball on boundary!", "#FF5722");
                    } else if (vibrationDetected && soundDetected) {
                        showEventNotification("Possible FOUR RUNS: Ball hit boundary rope!", "#2196F3");
                    } else if (vibrationDetected && !soundDetected) {
                        showEventNotification("Possible OUT: Player touched boundary!", "#F44336");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateMonitoringStatus(boolean active) {
        monitoringStatusText.setText(active ? "MONITORING ACTIVE" : "MONITORING INACTIVE");
        monitoringStatusText.setTextColor(active ? Color.GREEN : Color.GRAY);
    }

    private void updateMonitoringCountdown(final long remainingMs) {
        if (monitoringRunnable != null) {
            monitoringHandler.removeCallbacks(monitoringRunnable);
        }

        monitoringRunnable = new Runnable() {
            long remaining = remainingMs;

            @Override
            public void run() {
                double seconds = remaining / 1000.0;
                runOnUiThread(() -> monitoringTimeText.setText(String.format("%.1f sec remaining", seconds)));

                remaining -= 100;
                if (remaining > 0 && isMonitoring) {
                    monitoringHandler.postDelayed(this, 100);
                }
            }
        };

        monitoringHandler.post(monitoringRunnable);
    }

    private void updateDetectionIndicators(boolean foil, boolean vibration, boolean sound) {
        foilIndicator.setBackgroundColor(foil ? Color.GREEN : Color.GRAY);
        vibrationIndicator.setBackgroundColor(vibration ? Color.GREEN : Color.GRAY);
        soundIndicator.setBackgroundColor(sound ? Color.GREEN : Color.GRAY);
    }

    private void showEventNotification(String message, String colorHex) {
        eventNotificationText.setText(message);
        eventNotificationCard.setCardBackgroundColor(Color.parseColor(colorHex));

        eventNotificationCard.setVisibility(View.VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(500);
        eventNotificationCard.startAnimation(fadeIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
                fadeOut.setDuration(1000);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        eventNotificationCard.setVisibility(View.GONE);
                    }
                });
                eventNotificationCard.startAnimation(fadeOut);
            }
        }, 4000);
    }

    private void setupDemoButtons() {
        sixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSixAnimation();
            }
        });

        fourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFourAnimation();
            }
        });

        wicketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWicketAnimation();
            }
        });
    }

    private void showSixAnimation() {
        showScoreCard("6");
        animateCircles();
        playSixVideo();
    }

    private void showFourAnimation() {
        showScoreCard("4");
        animateCircles();
    }

    private void showWicketAnimation() {
        showScoreCard("W");
        animateCircles();
    }

    private void showScoreCard(String score) {
        scoreCardView.setVisibility(View.VISIBLE);
        scoreTextView.setText(score);

        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(500);
        scoreCardView.startAnimation(scaleAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
                fadeOut.setDuration(500);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        scoreCardView.setVisibility(View.INVISIBLE);
                    }
                });
                scoreCardView.startAnimation(fadeOut);
            }
        }, 2000);
    }

    private void animateCircles() {
        animateCircle(circleOne, 0);
        animateCircle(circleTwo, 200);
        animateCircle(circleThree, 400);
    }

    private void animateCircle(final View circle, long delay) {
        circle.setVisibility(View.VISIBLE);

        final ScaleAnimation scaleAnimation = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(800);

        final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(800);
        alphaAnimation.setStartOffset(400);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                circle.setVisibility(View.INVISIBLE);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                circle.startAnimation(scaleAnimation);
                circle.startAnimation(alphaAnimation);
            }
        }, delay);
    }

    private void playSixVideo() {
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.six_video;
        sixVideoView.setVideoURI(Uri.parse(videoPath));
        sixVideoView.setVisibility(View.VISIBLE);
        sixVideoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (monitoringRunnable != null) {
            monitoringHandler.removeCallbacks(monitoringRunnable);
        }
    }
}