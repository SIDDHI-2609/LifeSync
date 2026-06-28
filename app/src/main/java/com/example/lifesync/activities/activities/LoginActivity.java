package com.example.lifesync.activities.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifesync.R;
import com.example.lifesync.activities.database.FirebaseSyncManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {

    private TextInputEditText etEmail, etPassword;
    private TextView          errEmail, errPassword;
    private TextView          tvForgotPassword, tvSignUp;
    private MaterialButton    btnLogin, btnGoogleSignIn;
    private LinearLayout      layoutError;
    private TextView          tvFirebaseError;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Already signed in — pull fresh data then go to main
        FirebaseUser current = mAuth.getCurrentUser();
        if (current != null) {
            pullThenNavigate();
            return;
        }

        bindViews();
        setupListeners();
    }

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

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> { if (validate()) signIn(); });
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
            finish();
        });
        btnGoogleSignIn.setOnClickListener(v ->
                Toast.makeText(this, "Google Sign-In coming soon", Toast.LENGTH_SHORT).show());
        etEmail.setOnFocusChangeListener((v, f)    -> { if (f) hideErrorBanner(); });
        etPassword.setOnFocusChangeListener((v, f) -> { if (f) hideErrorBanner(); });
    }

    private boolean validate() {
        boolean ok = true;
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(text(etEmail)).matches()) {
            errEmail.setVisibility(View.VISIBLE); ok = false;
        } else errEmail.setVisibility(View.GONE);
        if (text(etPassword).isEmpty()) {
            errPassword.setVisibility(View.VISIBLE); ok = false;
        } else errPassword.setVisibility(View.GONE);
        return ok;
    }

    private void signIn() {
        setLoading(true, "Signing in…");

        mAuth.signInWithEmailAndPassword(text(etEmail), text(etPassword))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // ── KEY FIX ──────────────────────────────────────────────
                        // Pull ALL data from Firestore into Room BEFORE navigating.
                        // Previously we went straight to MainActivity while Room
                        // was still empty — the UI showed nothing.
                        pullThenNavigate();
                    } else {
                        setLoading(false, "Sign In");
                        showError(friendlyError(
                                task.getException() != null
                                        ? task.getException().getMessage() : null));
                    }
                });
    }

    /**
     * Shows "Loading your data..." on the button, pulls from Firestore,
     * then navigates to MainActivity once all 3 collections are in Room.
     */
    private void pullThenNavigate() {
        setLoading(true, "Loading your data…");

        FirebaseSyncManager sync = new FirebaseSyncManager(this);
        sync.syncAll(() -> {
            // Callback fires on main thread when notes + expenses + todos are all pulled
            runOnUiThread(() -> {
                setLoading(false, "Sign In");
                goToMain();
            });
        });
    }

    private void showForgotPasswordDialog() {
        String prefill = text(etEmail);
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
                    mAuth.sendPasswordResetEmail(resetEmail)
                            .addOnSuccessListener(v ->
                                    new AlertDialog.Builder(this)
                                            .setTitle("Email Sent ✓")
                                            .setMessage("Reset link sent to " + resetEmail)
                                            .setPositiveButton("OK", null).show())
                            .addOnFailureListener(e ->
                                    showError(friendlyError(e.getMessage())));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setLoading(boolean loading, String buttonText) {
        if (btnLogin == null) return;
        btnLogin.setEnabled(!loading);
        btnLogin.setText(buttonText);
        if (etEmail    != null) etEmail.setEnabled(!loading);
        if (etPassword != null) etPassword.setEnabled(!loading);
    }

    private void showError(String msg) {
        tvFirebaseError.setText(msg);
        layoutError.setVisibility(View.VISIBLE);
    }

    private void hideErrorBanner() { layoutError.setVisibility(View.GONE); }

    private void goToMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private String text(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String friendlyError(String raw) {
        if (raw == null) return "Something went wrong. Try again.";
        if (raw.contains("no user record") || raw.contains("user-not-found"))
            return "No account found with this email.";
        if (raw.contains("password is invalid") || raw.contains("wrong-password"))
            return "Incorrect password. Please try again.";
        if (raw.contains("too many requests"))
            return "Too many attempts. Please wait and try again.";
        if (raw.contains("network"))
            return "No internet connection. Check your network.";
        return "Sign in failed. Please check your credentials.";
    }
}