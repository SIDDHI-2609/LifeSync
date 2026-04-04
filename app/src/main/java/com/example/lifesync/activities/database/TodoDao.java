package com.example.lifesync.activities.database;
import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.lifesync.activities.models.TodoEntity;

import java.util.List;

@Dao
public interface TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TodoEntity todo);

    @Update
    void update(TodoEntity todo);

    @Delete
    void delete(TodoEntity todo);

    @Query("DELETE FROM todos WHERE id = :todoId AND userId = :userId")
    void deleteById(String todoId, String userId);

    @Query("DELETE FROM todos WHERE userId = :userId")
    void deleteAllByUser(String userId);

    // ── Live queries ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM todos WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<TodoEntity>> getAllTodos(String userId);

    @Query("SELECT * FROM todos WHERE userId = :userId AND isCompleted = 0 ORDER BY alarmTimeMillis ASC")
    LiveData<List<TodoEntity>> getPendingTodos(String userId);

    @Query("SELECT * FROM todos WHERE userId = :userId AND isCompleted = 1 ORDER BY createdAt DESC")
    LiveData<List<TodoEntity>> getCompletedTodos(String userId);

    @Query("SELECT * FROM todos WHERE id = :todoId AND userId = :userId LIMIT 1")
    TodoEntity getTodoById(String todoId, String userId);

    /** Todos with alarms still in the future — used by BootReceiver to reschedule */
    @Query("SELECT * FROM todos WHERE userId = :userId AND alarmTimeMillis > :now AND isCompleted = 0")
    List<TodoEntity> getActiveFutureTodos(String userId, long now);

    // ── Analytics (for Dashboard) ─────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM todos WHERE userId = :userId")
    LiveData<Integer> getTotalCount(String userId);

    @Query("SELECT COUNT(*) FROM todos WHERE userId = :userId AND isCompleted = 1")
    LiveData<Integer> getCompletedCount(String userId);

    // ── Sync helpers ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM todos WHERE userId = :userId AND isSynced = 0")
    List<TodoEntity> getUnsyncedTodos(String userId);

    @Query("UPDATE todos SET isSynced = 1 WHERE id = :todoId")
    void markSynced(String todoId);

    @Query("SELECT * FROM todos WHERE userId = :userId ORDER BY createdAt DESC")
    List<TodoEntity> getAllTodosSync(String userId);
}
