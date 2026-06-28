package com.example.lifesync.activities.activities;

import static android.media.MediaFormat.KEY_LANGUAGE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lifesync.R;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.repository.ExpenseRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import repository.NoteRepository;
import repository.TodoRepository;

public class SettingActivity extends  BaseActivity {

    private FirebaseAuth mAuth;

    private TextView tvUserName, tvUserEmail;

    private LinearLayout rowEditProfile;
    private LinearLayout rowClearData;
    private LinearLayout rowChangePassword;
    private LinearLayout rowPrivacy;
    private LinearLayout rowRateApp;

    private SwitchCompat switchNotifications;

    private MaterialButton btnLogout;
    private NoteRepository noteRepository;
    private ExpenseRepository expenseRepository;
    private TodoRepository todoRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        rowEditProfile = findViewById(R.id.rowEditProfile);
        rowClearData = findViewById(R.id.rowClearData);
        rowChangePassword = findViewById(R.id.rowChangePassword);
        rowPrivacy = findViewById(R.id.rowPrivacy);
        rowRateApp = findViewById(R.id.rowRateApp);
        noteRepository = new NoteRepository(this);
        expenseRepository = new ExpenseRepository(this);
        todoRepository = new TodoRepository(this);

        switchNotifications = findViewById(R.id.switchNotifications);

        btnLogout = findViewById(R.id.btnLogout);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {

            String name = user.getDisplayName();
            String email = user.getEmail();

            if (name != null && !name.isEmpty()) {
                tvUserName.setText(name);
            }

            tvUserEmail.setText(email);
        }



        SharedPreferences prefs =
                getSharedPreferences("SettingsPrefs", MODE_PRIVATE);

        switchNotifications.setChecked(
                prefs.getBoolean("notifications", true));

        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    prefs.edit()
                            .putBoolean("notifications", isChecked)
                            .apply();
                    Toast.makeText(
                            this,
                            isChecked ?
                                    "Notifications Enabled" :
                                    "Notifications Disabled",
                            Toast.LENGTH_SHORT
                    ).show();
                });

        rowEditProfile.setOnClickListener(v -> {



            if (user == null) return;

            EditText editName = new EditText(this);

            editName.setHint("Enter New Name");

            if (user.getDisplayName() != null) {
                editName.setText(user.getDisplayName());
            }

            new AlertDialog.Builder(this)
                    .setTitle("Edit Profile")
                    .setView(editName)

                    .setPositiveButton("Save", (dialog, which) -> {

                        String newName =
                                editName.getText().toString().trim();

                        if (!newName.isEmpty()) {

                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(newName)
                                            .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(task -> {

                                        if (task.isSuccessful()) {

                                            tvUserName.setText(newName);

                                            Toast.makeText(
                                                    SettingActivity.this,
                                                    "Profile Updated Successfully",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                        }
                    })

                    .setNegativeButton("Cancel", null)
                    .show();
        });

        rowChangePassword.setOnClickListener(v -> {

            FirebaseUser currentUser =
                    FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null &&
                    currentUser.getEmail() != null) {

                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(
                                currentUser.getEmail())
                        .addOnSuccessListener(unused ->
                                Toast.makeText(
                                        this,
                                        "Password reset email sent",
                                        Toast.LENGTH_LONG
                                ).show());
            }
        });

        rowPrivacy.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Data & Privacy")
                    .setMessage(
                            "Your data is stored securely. " +
                                    "Personal information is not shared with third parties."
                    )
                    .setPositiveButton("OK", null)
                    .show();
        });

        rowRateApp.setOnClickListener(v -> {

            String packageName = getPackageName();

            try {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "market://details?id="
                                                + packageName)));

            } catch (Exception e) {

                startActivity(
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://play.google.com/store/apps/details?id="
                                                + packageName)));
            }
        });
        rowClearData.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Clear All Data")
                    .setMessage("Are you sure? This will delete all Notes, Expenses and Todos.")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        noteRepository.deleteAllNotes();
                        expenseRepository.deleteAllExpenses();
                        todoRepository.deleteAllTodos();

                        Toast.makeText(
                                SettingActivity.this,
                                "All Data Deleted Successfully",
                                Toast.LENGTH_SHORT
                        ).show();

                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Intent intent =
                    new Intent(
                            SettingActivity.this,
                            LoginActivity.class);

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish();
        });
    }
    private void deleteCollection(CollectionReference collection) {

        collection.get().addOnSuccessListener(queryDocumentSnapshots -> {

            for (DocumentSnapshot document :
                    queryDocumentSnapshots.getDocuments()) {

                document.getReference().delete();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}


















