package com.example.lifesync.activities.database;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.lifesync.activities.models.ExpenseEntity;
import com.example.lifesync.activities.models.NoteEntity;
import com.example.lifesync.activities.models.TodoEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseSyncManager {

    private static final String TAG = "FirebaseSyncManager";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private ListenerRegistration notesListener;
    private ListenerRegistration expensesListener;
    private ListenerRegistration todosListener;

    private final FirebaseFirestore db;
    private final AppDatabase       roomDb;
    private final String            userId;

    public FirebaseSyncManager(Context context) {
        this.db     = FirebaseFirestore.getInstance();
        this.roomDb = AppDatabase.getInstance(context);
        this.userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    // ── Full sync with callback ───────────────────────────────────────────────

    public void syncAll(Runnable onComplete) {
        if (userId == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        EXECUTOR.execute(() -> {
            pushUnsyncedNotes();
            pushUnsyncedExpenses();
            pushUnsyncedTodos();
        });
        AtomicInteger remaining = new AtomicInteger(3);
        Runnable countDown = () -> {
            if (remaining.decrementAndGet() == 0 && onComplete != null) onComplete.run();
        };
        pullNotes(countDown);
        pullExpenses(countDown);
        pullTodos(countDown);
    }

    public void syncAll() { syncAll(null); }

    // ── Real-time listeners ───────────────────────────────────────────────────

    public void startRealTimeListeners() {
        if (userId == null) return;
        stopRealTimeListeners();

        notesListener = db.collection("users").document(userId).collection("notes")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot doc : snapshots)
                            roomDb.noteDao().insert(mapToNote(doc.getId(), doc.getData()));
                    });
                });

        expensesListener = db.collection("users").document(userId).collection("expenses")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot doc : snapshots)
                            roomDb.expenseDao().insert(mapToExpense(doc.getId(), doc.getData()));
                    });
                });

        todosListener = db.collection("users").document(userId).collection("todos")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot doc : snapshots)
                            roomDb.todoDao().insert(mapToTodo(doc.getId(), doc.getData()));
                    });
                });

        Log.d(TAG, "Real-time listeners started");
    }

    public void stopRealTimeListeners() {
        if (notesListener    != null) { notesListener.remove();    notesListener    = null; }
        if (expensesListener != null) { expensesListener.remove(); expensesListener = null; }
        if (todosListener    != null) { todosListener.remove();    todosListener    = null; }
    }

    // ── Pull ─────────────────────────────────────────────────────────────────

    private void pullNotes(Runnable onDone) {
        db.collection("users").document(userId).collection("notes").get()
                .addOnSuccessListener(s -> {
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot d : s) roomDb.noteDao().insert(mapToNote(d.getId(), d.getData()));
                    });
                    if (onDone != null) onDone.run();
                })
                .addOnFailureListener(e -> { Log.e(TAG, "pullNotes: " + e.getMessage()); if (onDone != null) onDone.run(); });
    }

    private void pullExpenses(Runnable onDone) {
        db.collection("users").document(userId).collection("expenses").get()
                .addOnSuccessListener(s -> {
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot d : s) roomDb.expenseDao().insert(mapToExpense(d.getId(), d.getData()));
                    });
                    if (onDone != null) onDone.run();
                })
                .addOnFailureListener(e -> { Log.e(TAG, "pullExpenses: " + e.getMessage()); if (onDone != null) onDone.run(); });
    }

    private void pullTodos(Runnable onDone) {
        db.collection("users").document(userId).collection("todos").get()
                .addOnSuccessListener(s -> {
                    EXECUTOR.execute(() -> {
                        for (QueryDocumentSnapshot d : s) roomDb.todoDao().insert(mapToTodo(d.getId(), d.getData()));
                    });
                    if (onDone != null) onDone.run();
                })
                .addOnFailureListener(e -> { Log.e(TAG, "pullTodos: " + e.getMessage()); if (onDone != null) onDone.run(); });
    }

    // ── Push unsynced ────────────────────────────────────────────────────────

    private void pushUnsyncedNotes() {
        for (NoteEntity n : roomDb.noteDao().getUnsyncedNotes(userId))
            db.collection("users").document(userId).collection("notes").document(n.id)
                    .set(noteToMap(n))
                    .addOnSuccessListener(v -> EXECUTOR.execute(() -> roomDb.noteDao().markSynced(n.id)));
    }

    private void pushUnsyncedExpenses() {
        for (ExpenseEntity e : roomDb.expenseDao().getUnsyncedExpenses(userId))
            db.collection("users").document(userId).collection("expenses").document(e.id)
                    .set(expenseToMap(e))
                    .addOnSuccessListener(v -> EXECUTOR.execute(() -> roomDb.expenseDao().markSynced(e.id)));
    }

    private void pushUnsyncedTodos() {
        for (TodoEntity t : roomDb.todoDao().getUnsyncedTodos(userId))
            db.collection("users").document(userId).collection("todos").document(t.id)
                    .set(todoToMap(t))
                    .addOnSuccessListener(v -> EXECUTOR.execute(() -> roomDb.todoDao().markSynced(t.id)));
    }

    // ── Single push / delete ─────────────────────────────────────────────────

    public void pushNote(NoteEntity n) {
        if (userId == null) return;
        db.collection("users").document(userId).collection("notes").document(n.id)
                .set(noteToMap(n)).addOnSuccessListener(v -> EXECUTOR.execute(() -> roomDb.noteDao().markSynced(n.id)));
    }
    public void deleteNoteFromFirestore(String id) {
        if (userId == null) return;
        db.collection("users").document(userId).collection("notes").document(id).delete();
    }

    public void pushExpense(ExpenseEntity e) {
        if (userId == null) return;
        db.collection("users").document(userId).collection("expenses").document(e.id)
                .set(expenseToMap(e)).addOnSuccessListener(v -> EXECUTOR.execute(() -> roomDb.expenseDao().markSynced(e.id)));
    }
    public void deleteExpenseFromFirestore(String id) {
        if (userId == null) return;
        db.collection("users").document(userId).collection("expenses").document(id).delete();
    }

    public void pushTodo(TodoEntity t) {
        if (userId == null) return;
        db.collection("users").document(userId).collection("todos").document(t.id)
                .set(todoToMap(t)).addOnSuccessListener(v -> EXECUTOR.execute(() -> roomDb.todoDao().markSynced(t.id)));
    }
    public void deleteTodoFromFirestore(String id) {
        if (userId == null) return;
        db.collection("users").document(userId).collection("todos").document(id).delete();
    }

    // ── Serialization ────────────────────────────────────────────────────────

    private Map<String, Object> noteToMap(NoteEntity n) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", n.userId); m.put("title", n.title); m.put("content", n.content);
        m.put("createdAt", n.createdAt); m.put("updatedAt", n.updatedAt);
        return m;
    }

    private NoteEntity mapToNote(String id, Map<String, Object> m) {
        NoteEntity n = new NoteEntity();
        n.id = id; n.userId = (String) m.get("userId"); n.title = (String) m.get("title");
        n.content = (String) m.get("content"); n.createdAt = toLong(m.get("createdAt"));
        n.updatedAt = toLong(m.get("updatedAt")); n.isSynced = true;
        return n;
    }

    private Map<String, Object> expenseToMap(ExpenseEntity e) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", e.userId); m.put("title", e.title); m.put("category", e.category);
        m.put("amount", e.amount); m.put("note", e.note); m.put("date", e.date); m.put("createdAt", e.createdAt);
        return m;
    }

    private ExpenseEntity mapToExpense(String id, Map<String, Object> m) {
        ExpenseEntity e = new ExpenseEntity();
        e.id = id; e.userId = (String) m.get("userId"); e.title = (String) m.get("title");
        e.category = (String) m.get("category"); e.amount = toDouble(m.get("amount"));
        e.note = (String) m.get("note"); e.date = toLong(m.get("date"));
        e.createdAt = toLong(m.get("createdAt")); e.isSynced = true;
        return e;
    }

    private Map<String, Object> todoToMap(TodoEntity t) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", t.userId); m.put("title", t.title); m.put("description", t.description);
        m.put("isCompleted", t.isCompleted); m.put("alarmTimeMillis", t.alarmTimeMillis); m.put("createdAt", t.createdAt);
        return m;
    }

    private TodoEntity mapToTodo(String id, Map<String, Object> m) {
        TodoEntity t = new TodoEntity();
        t.id = id; t.userId = (String) m.get("userId"); t.title = (String) m.get("title");
        t.description = (String) m.get("description"); t.isCompleted = Boolean.TRUE.equals(m.get("isCompleted"));
        t.alarmTimeMillis = toLong(m.get("alarmTimeMillis")); t.createdAt = toLong(m.get("createdAt")); t.isSynced = true;
        return t;
    }

    private long toLong(Object o) {
        if (o instanceof Long) return (Long) o;
        if (o instanceof Integer) return ((Integer) o).longValue();
        if (o instanceof Double) return ((Double) o).longValue();
        return 0L;
    }

    private double toDouble(Object o) {
        if (o instanceof Double) return (Double) o;
        if (o instanceof Long) return ((Long) o).doubleValue();
        if (o instanceof Integer) return ((Integer) o).doubleValue();
        return 0.0;
    }
}