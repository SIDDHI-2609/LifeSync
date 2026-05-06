package repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.database.FirebaseSyncManager;
import com.example.lifesync.activities.database.TodoDao;
import com.example.lifesync.activities.models.TodoEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoRepository {

    private final TodoDao dao;
    private final FirebaseSyncManager sync;
    private final String              userId;
    private final ExecutorService     executor = Executors.newSingleThreadExecutor();

    public TodoRepository(Context context) {
        dao    = AppDatabase.getInstance(context).todoDao();
        sync   = new FirebaseSyncManager(context);
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    public LiveData<List<TodoEntity>> getAllTodos()       { return dao.getAllTodos(userId); }
    public LiveData<List<TodoEntity>> getPendingTodos()   { return dao.getPendingTodos(userId); }
    public LiveData<List<TodoEntity>> getCompletedTodos() { return dao.getCompletedTodos(userId); }
    public LiveData<Integer> getTotalCount()              { return dao.getTotalCount(userId); }
    public LiveData<Integer> getCompletedCount()          { return dao.getCompletedCount(userId); }

    public List<TodoEntity> getActiveFutureTodosSync() {
        return dao.getActiveFutureTodos(userId, System.currentTimeMillis());
    }

    public String addTodo(String title, String description, long alarmTimeMillis) {
        String id = UUID.randomUUID().toString();
        TodoEntity todo = new TodoEntity(id, userId, title, description, alarmTimeMillis);
        executor.execute(() -> { dao.insert(todo); sync.pushTodo(todo); });
        return id;
    }

    public void setCompleted(TodoEntity todo, boolean completed) {
        todo.isCompleted = completed;
        todo.isSynced    = false;
        executor.execute(() -> { dao.update(todo); sync.pushTodo(todo); });
    }

    public void updateTodo(TodoEntity todo) {
        todo.isSynced = false;
        executor.execute(() -> { dao.update(todo); sync.pushTodo(todo); });
    }

    public void deleteTodo(TodoEntity todo) {
        executor.execute(() -> { dao.delete(todo); sync.deleteTodoFromFirestore(todo.id); });
    }

    public void deleteAllTodos() {
        executor.execute(() -> {
            List<TodoEntity> all = dao.getAllTodosSync(userId);
            for (TodoEntity t : all) sync.deleteTodoFromFirestore(t.id);
            dao.deleteAllByUser(userId);
        });
    }

    public void syncWithFirebase() { sync.syncAll(); }
}