package com.example.lifesync.activities.database;

import android.content.Context;
import android.util.Log;

import com.example.lifesync.activities.models.ExpenseEntity;
import com.example.lifesync.activities.models.NoteEntity;
import com.example.lifesync.activities.models.TodoEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles two-way sync between Room (local) and Firestore (cloud).
 *
 * Firestore structure:
 *   users/{userId}/notes/{noteId}
 *   users/{userId}/expenses/{expenseId}
 *   users/{userId}/todos/{todoId}
 *
 * Call syncAll() on app start / when network reconnects.
 * Each save/update in Repository calls pushToFirestore() immediately.
 */
public class FirebaseSyncManager {

    private static final String TAG = "FirebaseSyncManager";

    private final FirebaseFirestore db;
    private final AppDatabase       roomDb;
    private final String            userId;
    private final ExecutorService   executor;

    public FirebaseSyncManager(Context context) {
        this.db       = FirebaseFirestore.getInstance();
        this.roomDb   = AppDatabase.getInstance(context);
        this.userId   = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  FULL SYNC — call on login and on network reconnect
    // ═════════════════════════════════════════════════════════════════════════

    public void syncAll() {
        if (userId == null) { Log.w(TAG, "No user, skip sync"); return; }
        pushUnsyncedToFirestore();   // local → cloud
        pullFromFirestore();         // cloud → local
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PUSH: unsynced local records → Firestore
    // ─────────────────────────────────────────────────────────────────────────

    private void pushUnsyncedToFirestore() {
        executor.execute(() -> {
            pushNotes();
            pushExpenses();
            pushTodos();
        });
    }

    private void pushNotes() {
        NoteDao dao = roomDb.noteDao();
        List<NoteEntity> unsynced = dao.getUnsyncedNotes(userId);
        for (NoteEntity note : unsynced) {
            Map<String, Object> data = noteToMap(note);
            db.collection("users").document(userId)
                    .collection("notes").document(note.id)
                    .set(data)
                    .addOnSuccessListener(v -> executor.execute(() -> dao.markSynced(note.id)))
                    .addOnFailureListener(e -> Log.e(TAG, "Push note failed: " + e.getMessage()));
        }
    }

    private void pushExpenses() {
        ExpenseDao dao = roomDb.expenseDao();
        List<ExpenseEntity> unsynced = dao.getUnsyncedExpenses(userId);
        for (ExpenseEntity expense : unsynced) {
            db.collection("users").document(userId)
                    .collection("expenses").document(expense.id)
                    .set(expenseToMap(expense))
                    .addOnSuccessListener(v -> executor.execute(() -> dao.markSynced(expense.id)))
                    .addOnFailureListener(e -> Log.e(TAG, "Push expense failed: " + e.getMessage()));
        }
    }

    private void pushTodos() {
        TodoDao dao = roomDb.todoDao();
        List<TodoEntity> unsynced = dao.getUnsyncedTodos(userId);
        for (TodoEntity todo : unsynced) {
            db.collection("users").document(userId)
                    .collection("todos").document(todo.id)
                    .set(todoToMap(todo))
                    .addOnSuccessListener(v -> executor.execute(() -> dao.markSynced(todo.id)))
                    .addOnFailureListener(e -> Log.e(TAG, "Push todo failed: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PULL: Firestore → Room (merges, won't delete local-only records)
    // ─────────────────────────────────────────────────────────────────────────

    private void pullFromFirestore() {
        pullNotes();
        pullExpenses();
        pullTodos();
    }

    private void pullNotes() {
        db.collection("users").document(userId).collection("notes")
                .get()
                .addOnSuccessListener(snapshots -> executor.execute(() -> {
                    NoteDao dao = roomDb.noteDao();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        NoteEntity note = mapToNote(doc.getId(), doc.getData());
                        dao.insert(note); // REPLACE if exists
                    }
                    Log.d(TAG, "Pulled " + snapshots.size() + " notes");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Pull notes failed: " + e.getMessage()));
    }

    private void pullExpenses() {
        db.collection("users").document(userId).collection("expenses")
                .get()
                .addOnSuccessListener(snapshots -> executor.execute(() -> {
                    ExpenseDao dao = roomDb.expenseDao();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ExpenseEntity expense = mapToExpense(doc.getId(), doc.getData());
                        dao.insert(expense);
                    }
                    Log.d(TAG, "Pulled " + snapshots.size() + " expenses");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Pull expenses failed: " + e.getMessage()));
    }

    private void pullTodos() {
        db.collection("users").document(userId).collection("todos")
                .get()
                .addOnSuccessListener(snapshots -> executor.execute(() -> {
                    TodoDao dao = roomDb.todoDao();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        TodoEntity todo = mapToTodo(doc.getId(), doc.getData());
                        dao.insert(todo);
                    }
                    Log.d(TAG, "Pulled " + snapshots.size() + " todos");
                }))
                .addOnFailureListener(e -> Log.e(TAG, "Pull todos failed: " + e.getMessage()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SINGLE ITEM PUSH — call from Repository after every save/update/delete
    // ─────────────────────────────────────────────────────────────────────────

    public void pushNote(NoteEntity note) {
        if (userId == null) return;
        db.collection("users").document(userId)
                .collection("notes").document(note.id)
                .set(noteToMap(note))
                .addOnSuccessListener(v ->
                        executor.execute(() -> roomDb.noteDao().markSynced(note.id)));
    }

    public void deleteNoteFromFirestore(String noteId) {
        if (userId == null) return;
        db.collection("users").document(userId)
                .collection("notes").document(noteId).delete();
    }

    public void pushExpense(ExpenseEntity expense) {
        if (userId == null) return;
        db.collection("users").document(userId)
                .collection("expenses").document(expense.id)
                .set(expenseToMap(expense))
                .addOnSuccessListener(v ->
                        executor.execute(() -> roomDb.expenseDao().markSynced(expense.id)));
    }

    public void deleteExpenseFromFirestore(String expenseId) {
        if (userId == null) return;
        db.collection("users").document(userId)
                .collection("expenses").document(expenseId).delete();
    }

    public void pushTodo(TodoEntity todo) {
        if (userId == null) return;
        db.collection("users").document(userId)
                .collection("todos").document(todo.id)
                .set(todoToMap(todo))
                .addOnSuccessListener(v ->
                        executor.execute(() -> roomDb.todoDao().markSynced(todo.id)));
    }

    public void deleteTodoFromFirestore(String todoId) {
        if (userId == null) return;
        db.collection("users").document(userId)
                .collection("todos").document(todoId).delete();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SERIALIZATION HELPERS
    // ═════════════════════════════════════════════════════════════════════════

    private Map<String, Object> noteToMap(NoteEntity n) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId",    n.userId);
        m.put("title",     n.title);
        m.put("content",   n.content);
        m.put("createdAt", n.createdAt);
        m.put("updatedAt", n.updatedAt);
        return m;
    }

    private NoteEntity mapToNote(String id, Map<String, Object> m) {
        NoteEntity n = new NoteEntity();
        n.id        = id;
        n.userId    = (String)  m.get("userId");
        n.title     = (String)  m.get("title");
        n.content   = (String)  m.get("content");
        n.createdAt = toLong(m.get("createdAt"));
        n.updatedAt = toLong(m.get("updatedAt"));
        n.isSynced  = true;
        return n;
    }

    private Map<String, Object> expenseToMap(ExpenseEntity e) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId",    e.userId);
        m.put("title",     e.title);
        m.put("category",  e.category);
        m.put("amount",    e.amount);
        m.put("note",      e.note);
        m.put("date",      e.date);
        m.put("createdAt", e.createdAt);
        return m;
    }

    private ExpenseEntity mapToExpense(String id, Map<String, Object> m) {
        ExpenseEntity e = new ExpenseEntity();
        e.id        = id;
        e.userId    = (String) m.get("userId");
        e.title     = (String) m.get("title");
        e.category  = (String) m.get("category");
        e.amount    = toDouble(m.get("amount"));
        e.note      = (String) m.get("note");
        e.date      = toLong(m.get("date"));
        e.createdAt = toLong(m.get("createdAt"));
        e.isSynced  = true;
        return e;
    }

    private Map<String, Object> todoToMap(TodoEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId",          t.userId);
        m.put("title",           t.title);
        m.put("description",     t.description);
        m.put("isCompleted",     t.isCompleted);
        m.put("alarmTimeMillis", t.alarmTimeMillis);
        m.put("createdAt",       t.createdAt);
        return m;
    }

    private TodoEntity mapToTodo(String id, Map<String, Object> m) {
        TodoEntity t = new TodoEntity();
        t.id              = id;
        t.userId          = (String)  m.get("userId");
        t.title           = (String)  m.get("title");
        t.description     = (String)  m.get("description");
        t.isCompleted     = Boolean.TRUE.equals(m.get("isCompleted"));
        t.alarmTimeMillis = toLong(m.get("alarmTimeMillis"));
        t.createdAt       = toLong(m.get("createdAt"));
        t.isSynced        = true;
        return t;
    }

    private long toLong(Object o) {
        if (o instanceof Long)    return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Double)  return ((Double) o).longValue();
        return 0L;
    }

    private double toDouble(Object o) {
        if (o instanceof Double)  return (Double) o;
        if (o instanceof Long)    return ((Long) o).doubleValue();
        if (o instanceof Integer) return ((Integer) o).doubleValue();
        return 0.0;
    }
}
