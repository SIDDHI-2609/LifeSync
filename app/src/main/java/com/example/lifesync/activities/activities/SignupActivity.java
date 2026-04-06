package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    // ── Step views ────────────────────────────────────────────────────────────
    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private LinearLayout layoutSuccess, layoutButtons, layoutLoginLink;

    // ── Step indicators ───────────────────────────────────────────────────────
    private TextView dot1, dot2, dot3;
    private View     line1, line2;
    private TextView tvStepTitle, tvStepSub;

    // ── Fields ────────────────────────────────────────────────────────────────
    private TextInputEditText etName, etMobile, etEmail, etPassword, etConfirmPassword;

    // ── Error labels ──────────────────────────────────────────────────────────
    private TextView errName, errMobile, errEmail, errPassword, errConfirm;

    // ── Strength bar ──────────────────────────────────────────────────────────
    private LinearLayout layoutStrength;
    private ProgressBar  progressStrength;
    private TextView     tvStrengthLabel;

    // ── Buttons ───────────────────────────────────────────────────────────────
    private MaterialButton btnNext, btnBack, btnGoToDashboard;

    // ── State ─────────────────────────────────────────────────────────────────
    private int currentStep = 1;
    private static final String[] STEP_TITLES = {
            "Personal details", "Contact info", "Secure your account"};

    private FirebaseAuth mAuth;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        bindViews();
        setupStrengthWatcher();
        setupButtonListeners();
        renderStep(1);
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        layoutStep1     = findViewById(R.id.layoutStep1);
        layoutStep2     = findViewById(R.id.layoutStep2);
        layoutStep3     = findViewById(R.id.layoutStep3);
        layoutSuccess   = findViewById(R.id.layoutSuccess);
        layoutButtons   = findViewById(R.id.layoutButtons);
        layoutLoginLink = findViewById(R.id.layoutLoginLink);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);

        tvStepTitle = findViewById(R.id.tvStepTitle);
        tvStepSub   = findViewById(R.id.tvStepSub);

        etName            = findViewById(R.id.etName);
        etMobile          = findViewById(R.id.etMobile);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        errName     = findViewById(R.id.errName);
        errMobile   = findViewById(R.id.errMobile);
        errEmail    = findViewById(R.id.errEmail);
        errPassword = findViewById(R.id.errPassword);
        errConfirm  = findViewById(R.id.errConfirm);

        layoutStrength  = findViewById(R.id.layoutStrength);
        progressStrength= findViewById(R.id.progressStrength);
        tvStrengthLabel = findViewById(R.id.tvStrengthLabel);

        btnNext          = findViewById(R.id.btnNext);
        btnBack          = findViewById(R.id.btnBack);
        btnGoToDashboard = findViewById(R.id.btnGoToDashboard);

        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // ── Step render ───────────────────────────────────────────────────────────

    private void renderStep(int step) {
        currentStep = step;

        // Show/hide step layouts
        layoutStep1.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        layoutStep2.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        layoutStep3.setVisibility(step == 3 ? View.VISIBLE : View.GONE);

        // Step label
        tvStepTitle.setText(STEP_TITLES[step - 1]);
        tvStepSub.setText("Step " + step + " of 3");

        // Button labels
        btnNext.setText(step == 3 ? "Create Account" : "Continue");
        btnBack.setVisibility(step > 1 ? View.VISIBLE : View.GONE);

        // Update dots
        updateDot(dot1, line1, step, 1);
        updateDot(dot2, line2, step, 2);
        updateDot(dot3, null,  step, 3);
    }

    /** Sets dot appearance: done (checkmark) / active (filled) / future (grey outline) */
    private void updateDot(TextView dot, View lineAfter, int currentStep, int dotNumber) {
        if (dotNumber < currentStep) {
            // Done
            dot.setText("✓");
            dot.setTextColor(Color.WHITE);
            dot.setBackgroundResource(R.drawable.step_active);
            if (lineAfter != null) lineAfter.setBackgroundColor(Color.parseColor("#1B1F3B"));
        } else if (dotNumber == currentStep) {
            // Active
            dot.setText(String.valueOf(dotNumber));
            dot.setTextColor(Color.WHITE);
            dot.setBackgroundResource(R.drawable.step_active);
        } else {
            // Future
            dot.setText(String.valueOf(dotNumber));
            dot.setTextColor(Color.parseColor("#AAAAAA"));
            dot.setBackgroundResource(R.drawable.step_inactive);
        }
    }

    // ── Button listeners ──────────────────────────────────────────────────────

    private void setupButtonListeners() {
        btnNext.setOnClickListener(v -> {
            if (!validate(currentStep)) return;
            if (currentStep < 3) {
                renderStep(currentStep + 1);
            } else {
                registerUser();
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentStep > 1) renderStep(currentStep - 1);
        });

        btnGoToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validate(int step) {
        boolean ok = true;

        if (step == 1) {
            String name = text(etName);
            if (name.isEmpty()) {
                errName.setVisibility(View.VISIBLE);
                ok = false;
            } else {
                errName.setVisibility(View.GONE);
            }

            String mobile = text(etMobile);
            if (!mobile.matches("\\d{10}")) {
                errMobile.setVisibility(View.VISIBLE);
                ok = false;
            } else {
                errMobile.setVisibility(View.GONE);
            }
        }

        if (step == 2) {
            String email = text(etEmail);
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errEmail.setVisibility(View.VISIBLE);
                ok = false;
            } else {
                errEmail.setVisibility(View.GONE);
            }
        }

        if (step == 3) {
            String pw = text(etPassword);
            String cf = text(etConfirmPassword);

            if (pw.length() < 8) {
                errPassword.setVisibility(View.VISIBLE);
                ok = false;
            } else {
                errPassword.setVisibility(View.GONE);
            }

            if (!pw.equals(cf) || cf.isEmpty()) {
                errConfirm.setVisibility(View.VISIBLE);
                ok = false;
            } else {
                errConfirm.setVisibility(View.GONE);
            }
        }

        return ok;
    }

    // ── Password strength watcher ─────────────────────────────────────────────

    private void setupStrengthWatcher() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pw = s.toString();
                if (pw.isEmpty()) {
                    layoutStrength.setVisibility(View.GONE);
                    return;
                }
                layoutStrength.setVisibility(View.VISIBLE);

                int score = 0;
                if (pw.length() >= 8)                  score++;
                if (pw.matches(".*[A-Z].*"))           score++;
                if (pw.matches(".*[0-9].*"))           score++;
                if (pw.matches(".*[^A-Za-z0-9].*"))   score++;

                progressStrength.setProgress(score);

                String[] labels = {"Weak", "Fair", "Good", "Strong"};
                int[]    colors = {
                        Color.parseColor("#E24B4A"),
                        Color.parseColor("#EF9F27"),
                        Color.parseColor("#4C6EF5"),
                        Color.parseColor("#1D9E75")
                };

                int idx = Math.max(0, score - 1);
                tvStrengthLabel.setText(labels[idx]);
                tvStrengthLabel.setTextColor(colors[idx]);
            }
        });
    }

    // ── Firebase register ─────────────────────────────────────────────────────

    private void registerUser() {
        String email    = text(etEmail);
        String password = text(etPassword);
        String name     = text(etName);

        btnNext.setEnabled(false);
        btnNext.setText("Creating account…");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Save display name
                        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.getCurrentUser().updateProfile(profile);
                        }
                        showSuccess();
                    } else {
                        btnNext.setEnabled(true);
                        btnNext.setText("Create Account");
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed. Try again.";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Success state ─────────────────────────────────────────────────────────

    private void showSuccess() {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.GONE);
        layoutButtons.setVisibility(View.GONE);
        layoutLoginLink.setVisibility(View.GONE);

        // Update step indicator — all done
        dot1.setText("✓"); dot1.setBackgroundResource(R.drawable.step_active); dot1.setTextColor(Color.WHITE);
        dot2.setText("✓"); dot2.setBackgroundResource(R.drawable.step_active); dot2.setTextColor(Color.WHITE);
        dot3.setText("✓"); dot3.setBackgroundResource(R.drawable.step_active); dot3.setTextColor(Color.WHITE);
        line1.setBackgroundColor(Color.parseColor("#1B1F3B"));
        line2.setBackgroundColor(Color.parseColor("#1B1F3B"));

        tvStepTitle.setText("All done!");
        tvStepSub.setText("Account created successfully");

        layoutSuccess.setVisibility(View.VISIBLE);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
