package repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.database.FirebaseSyncManager;
import com.example.lifesync.activities.database.NoteDao;
import com.example.lifesync.activities.models.NoteEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * All Note operations go through here.
 * Activity/Fragment → ViewModel → Repository → Room + Firebase
 */
public class NoteRepository {

    private final NoteDao dao;
    private final FirebaseSyncManager sync;
    private final String            userId;
    private final ExecutorService   executor = Executors.newSingleThreadExecutor();

    public NoteRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.dao    = db.noteDao();
        this.sync   = new FirebaseSyncManager(context);
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // ── Read (LiveData — auto-updates UI) ─────────────────────────────────────

    public LiveData<List<NoteEntity>> getAllNotes() {
        return dao.getAllNotes(userId);
    }

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        return dao.searchNotes(userId, query);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    public void addNote(String title, String content) {
        NoteEntity note = new NoteEntity(
                UUID.randomUUID().toString(),   // unique ID used in both Room & Firestore
                userId, title, content,
                System.currentTimeMillis(),
                System.currentTimeMillis());
        executor.execute(() -> {
            dao.insert(note);
            sync.pushNote(note);               // immediate cloud push
        });
    }

    public void updateNote(NoteEntity note) {
        note.updatedAt = System.currentTimeMillis();
        note.isSynced  = false;
        executor.execute(() -> {
            dao.update(note);
            sync.pushNote(note);
        });
    }

    // ── Delete (only when user explicitly requests) ───────────────────────────

    public void deleteNote(NoteEntity note) {
        executor.execute(() -> {
            dao.delete(note);
            sync.deleteNoteFromFirestore(note.id);
        });
    }

    public void deleteAllNotes() {
        executor.execute(() -> {
            // Fetch IDs first, then delete from Firestore one by one
            List<NoteEntity> all = dao.getAllNotesSync(userId);
            for (NoteEntity n : all) sync.deleteNoteFromFirestore(n.id);
            dao.deleteAllByUser(userId);
        });
    }

    // ── Sync ──────────────────────────────────────────────────────────────────

    public void syncWithFirebase() {
        sync.syncAll();
    }
}
