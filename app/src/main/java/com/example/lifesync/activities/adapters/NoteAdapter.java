package com.example.lifesync.activities.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;

import java.util.List;
import com.example.lifesync.activities.models.Note;
public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<Note> notes;
    OnDeleteClickListener listener;

    public NoteAdapter(List<Note> notes, OnDeleteClickListener listener) {
        this.notes = notes;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, content;
        Button btnDelete;
        public NoteViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.tvTitle);
            content = view.findViewById(R.id.tvContent);
            btnDelete = view.findViewById(R.id.btnDelete);

        }
    }
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.title);
        holder.content.setText(note.content);

        holder.btnDelete.setOnClickListener(v -> {
            listener.onDeleteClick(note);
        });
    }
    public int getItemCount() {
        return notes.size();
    }
    public interface OnDeleteClickListener {
        void onDeleteClick(Note note);
    }
}
