package com.example.lifesync.activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.models.Todo;

import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder>{
    List<Todo> list;
    OnDeleteClickListener deleteListener;
    OnCheckClickListener checkListener;

public interface OnDeleteClickListener{
    void onDelete(Todo todo);
}
public interface OnCheckClickListener{
    void onCheck(Todo todo, Boolean isChecked);
}
public TodoAdapter(List<Todo> list, OnDeleteClickListener deleteListener, OnCheckClickListener checkListener){
    this.list = list;
    this.deleteListener = deleteListener;
    this.checkListener = checkListener;

}
public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView task;
    CheckBox checkBox;
    Button delete;

    public ViewHolder(View view) {
        super(view);
        task = view.findViewById(R.id.tvTask);
        checkBox = view.findViewById(R.id.checkDone);
        delete = view.findViewById(R.id.btnDelete);
    }
}
public void onBindViewHolder(ViewHolder holder, int position) {
    Todo todo = list.get(position);
    holder.task.setText(todo.title);
    holder.checkBox.setChecked(todo.isDone);

    holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->{
        if(checkListener != null){
            checkListener.onCheck(todo, isChecked);
        }
    });
    holder.delete.setOnClickListener(v -> {
        if(deleteListener != null){
            deleteListener.onDelete(todo);
        }
    });
}
public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo, parent, false);
    return new ViewHolder(view);
}
public int getItemCount() {
    return list.size();
}

}
