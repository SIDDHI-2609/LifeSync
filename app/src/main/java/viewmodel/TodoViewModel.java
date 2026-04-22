package viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.lifesync.activities.models.TodoEntity;

import java.util.List;

import repository.TodoRepository;

public class TodoViewModel extends AndroidViewModel {

    private final TodoRepository repo;

    public final LiveData<List<TodoEntity>> allTodos;

    public TodoViewModel(@NonNull Application app) {
        super(app);
        repo     = new TodoRepository(app);
        allTodos = repo.getAllTodos();   // LiveData — auto-updates RecyclerView
    }

    /** Returns the String UUID used both as Room PK and Firestore doc ID */
    public String addTodo(String title, String description, long alarmTimeMillis) {
        return repo.addTodo(title, description, alarmTimeMillis);
    }

    public void setCompleted(TodoEntity todo, boolean completed) {
        repo.setCompleted(todo, completed);
    }

    public void deleteTodo(TodoEntity todo) {
        repo.deleteTodo(todo);
    }

    public void deleteAllTodos() {
        repo.deleteAllTodos();
    }

    public void sync() {
        repo.syncWithFirebase();
    }
}
