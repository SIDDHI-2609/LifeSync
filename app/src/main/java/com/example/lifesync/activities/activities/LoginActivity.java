package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextInputEditText etEmail, etPassword;
    private TextView          errEmail, errPassword;
    private TextView          tvForgotPassword, tvSignUp;
    private MaterialButton    btnLogin, btnGoogleSignIn;
    private LinearLayout      layoutError;
    private TextView          tvFirebaseError;

    private FirebaseAuth mAuth;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // If user is already signed in, go straight to MainActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMain();
            return;
        }

        bindViews();
        setupListeners();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        errEmail         = findViewById(R.id.errEmail);
        errPassword      = findViewById(R.id.errPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp         = findViewById(R.id.tvSignUp);
        btnLogin         = findViewById(R.id.btnLogin);
        btnGoogleSignIn  = findViewById(R.id.btnGoogleSignIn);
        layoutError      = findViewById(R.id.layoutError);
        tvFirebaseError  = findViewById(R.id.tvFirebaseError);
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {

        // ── Sign In ───────────────────────────────────────────────────────────
        btnLogin.setOnClickListener(v -> {
            if (validate()) signIn();
        });

        // ── Forgot password ───────────────────────────────────────────────────
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // ── Go to Sign Up ─────────────────────────────────────────────────────
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });

        // ── Google Sign-In stub ───────────────────────────────────────────────
        // Replace with actual Google Sign-In flow if needed.
        btnGoogleSignIn.setOnClickListener(v ->
                showError("Google Sign-In coming soon. Use email & password for now."));

        // Clear error banner when user starts typing again
        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) hideErrorBanner();
        });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) hideErrorBanner();
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private boolean validate() {
        boolean ok = true;

        String email = text(etEmail);
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errEmail.setVisibility(View.VISIBLE);
            ok = false;
        } else {
            errEmail.setVisibility(View.GONE);
        }

        String password = text(etPassword);
        if (password.isEmpty()) {
            errPassword.setVisibility(View.VISIBLE);
            ok = false;
        } else {
            errPassword.setVisibility(View.GONE);
        }

        return ok;
    }

    // ── Firebase Sign In ──────────────────────────────────────────────────────

    private void signIn() {
        String email    = text(etEmail);
        String password = text(etPassword);

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        hideErrorBanner();
                        goToMain();
                    } else {
                        String msg = task.getException() != null
                                ? friendlyError(task.getException().getMessage())
                                : "Sign in failed. Please try again.";
                        showError(msg);
                    }
                });
    }

    // ── Forgot Password ───────────────────────────────────────────────────────

    private void showForgotPasswordDialog() {
        // Pre-fill with whatever email is already typed
        String prefill = text(etEmail);

        // Build a simple input dialog
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint("Enter your email");
        input.setText(prefill);
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                | android.text.InputType.TYPE_CLASS_TEXT);
        input.setPadding(48, 32, 48, 16);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We'll send a reset link to your email.")
                .setView(input)
                .setPositiveButton("Send Reset Link", (dialog, which) -> {
                    String resetEmail = input.getText() != null
                            ? input.getText().toString().trim() : "";
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                        showError("Enter a valid email to reset your password.");
                        return;
                    }
                    sendPasswordReset(resetEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendPasswordReset(String email) {
        btnLogin.setEnabled(false);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        new AlertDialog.Builder(this)
                                .setTitle("Email Sent")
                                .setMessage("A password reset link was sent to " + email
                                        + ". Check your inbox.")
                                .setPositiveButton("OK", null)
                                .show();
                    } else {
                        String msg = task.getException() != null
                                ? friendlyError(task.getException().getMessage())
                                : "Could not send reset email. Try again.";
                        showError(msg);
                    }
                });
    }

    // ── Loading state ─────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Signing in…" : "Sign In");
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    // ── Error banner ──────────────────────────────────────────────────────────

    private void showError(String message) {
        tvFirebaseError.setText(message);
        layoutError.setVisibility(View.VISIBLE);
    }

    private void hideErrorBanner() {
        layoutError.setVisibility(View.GONE);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    /**
     * Converts raw Firebase error messages into user-friendly text.
     */
    private String friendlyError(String raw) {
        if (raw == null) return "Something went wrong. Try again.";
        if (raw.contains("no user record") || raw.contains("user-not-found"))
            return "No account found with this email. Please sign up first.";
        if (raw.contains("password is invalid") || raw.contains("wrong-password"))
            return "Incorrect password. Please try again.";
        if (raw.contains("too many requests"))
            return "Too many failed attempts. Please wait a moment and try again.";
        if (raw.contains("network"))
            return "No internet connection. Check your network and retry.";
        if (raw.contains("email address is badly formatted"))
            return "That email address doesn't look right.";
        return "Sign in failed. Please check your credentials.";
    }
}