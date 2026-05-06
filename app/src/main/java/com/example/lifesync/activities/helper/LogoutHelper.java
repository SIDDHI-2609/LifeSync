package com.example.lifesync.activities.helper;

import android.content.Context;
import android.content.Intent;

import com.example.lifesync.activities.activities.LoginActivity;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.database.FirebaseSyncManager;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executors;

/**
 * ALWAYS use this instead of calling FirebaseAuth.signOut() directly.
 *
 * What it does in order:
 *  1. Stops real-time Firestore listeners (prevents callbacks after logout)
 *  2. Clears ALL local Room data for this user
 *  3. Destroys the Room database singleton (forces fresh instance on next login)
 *  4. Signs out of Firebase Auth
 *  5. Navigates to LoginActivity with a clean back stack
 *
 * Why step 3 matters:
 *  Without destroyInstance(), the next user to log in on this device reuses
 *  the old database singleton. Their userId is different so DAO queries
 *  return nothing — they see an empty app even though their data is in Firestore.
 */
public class LogoutHelper {

    public static void logout(Context context, FirebaseSyncManager syncManager) {
        // 1. Stop real-time Firestore listeners
        if (syncManager != null) syncManager.stopRealTimeListeners();

        // 2. Clear local Room data on background thread, then navigate
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        Executors.newSingleThreadExecutor().execute(() -> {
            if (userId != null) {
                AppDatabase db = AppDatabase.getInstance(context);
                db.noteDao().deleteAllByUser(userId);
                db.expenseDao().deleteAllByUser(userId);
                db.todoDao().deleteAllByUser(userId);
            }

            // 3. Destroy Room singleton so next login gets a fresh instance
            AppDatabase.destroyInstance();

            // 4. Sign out Firebase (must be on main thread)
            ((android.app.Activity) context).runOnUiThread(() -> {
                FirebaseAuth.getInstance().signOut();

                // 5. Navigate to login with clean stack
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            });
        });
    }
}