package com.example.boundvision;

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
    private CardView scoreCardView;
    private View circleOne, circleTwo, circleThree;
    private Button sixButton, fourButton, wicketButton;
    private TextView scoreTextView;
    private VideoView sixVideoView;

    // Firebase reference
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference scoreReference;

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
        circleOne = findViewById(R.id.circleOne);
        circleTwo = findViewById(R.id.circleTwo);
        circleThree = findViewById(R.id.circleThree);
        sixButton = findViewById(R.id.sixButton);
        fourButton = findViewById(R.id.fourButton);
        wicketButton = findViewById(R.id.wicketButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        sixVideoView = findViewById(R.id.sixVideoView);
    }

    private void setupVideoView() {
        // Set up media controller for the video view
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(sixVideoView);
        sixVideoView.setMediaController(mediaController);

        // Set completion listener to hide video when playback ends
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

        // Listen for changes in the score
        scoreReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String scoreType = snapshot.child("type").getValue(String.class);

                    // Handle different score types
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
    }

    private void setupDemoButtons() {
        // For testing purposes - in a real app these would update Firebase
        sixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For testing, directly trigger animation
                // In a real app, you'd update Firebase here
                showSixAnimation();

                // Example of how to update Firebase:
                // scoreReference.child("type").setValue("SIX");
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
        // Show the score card with "6"
        showScoreCard("6");

        // Show circle animations
        animateCircles();

        // Play the six video
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
        // Make the score card visible
        scoreCardView.setVisibility(View.VISIBLE);

        // Set the score text
        scoreTextView.setText(score);

        // Animate the score card
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(500);
        scoreCardView.startAnimation(scaleAnimation);

        // Hide the score card after a delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
                fadeOut.setDuration(500);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationRepeat(Animation animation) {}

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
        // Show and animate the circles
        animateCircle(circleOne, 0);
        animateCircle(circleTwo, 200);
        animateCircle(circleThree, 400);
    }

    private void animateCircle(final View circle, long delay) {
        circle.setVisibility(View.VISIBLE);

        // Scale animation
        final ScaleAnimation scaleAnimation = new ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(800);

        // Alpha animation (fade out)
        final AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(800);
        alphaAnimation.setStartOffset(400);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                circle.setVisibility(View.INVISIBLE);
            }
        });

        // Start animations with delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                circle.startAnimation(scaleAnimation);
                circle.startAnimation(alphaAnimation);
            }
        }, delay);
    }

    private void playSixVideo() {
        // Prepare the video path - from raw resources
        // Add your six video to the raw folder
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.six_video;

        sixVideoView.setVideoURI(Uri.parse(videoPath));
        sixVideoView.setVisibility(View.VISIBLE);

        // Start video playback
        sixVideoView.start();
    }
}