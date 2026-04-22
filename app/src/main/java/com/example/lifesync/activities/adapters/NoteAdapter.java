package com.example.lifesync.activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.models.NoteEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    // Accent colors cycling per note card (index % colors.length)
    private static final int[] ACCENT_COLORS = {
            0xFF4C6EF5,  // blue
            0xFFFFB300,  // amber
            0xFF1D9E75,  // green
            0xFFE53935,  // red
            0xFF7E57C2,  // purple
            0xFF26C6DA   // teal
    };

    private final Consumer<NoteEntity> onDelete;
    private final Consumer<NoteEntity> onEdit;
    private       List<NoteEntity>     noteList = new ArrayList<>();
    private final SimpleDateFormat     sdf =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    public NoteAdapter(Consumer<NoteEntity> onDelete, Consumer<NoteEntity> onEdit) {
        this.onDelete = onDelete;
        this.onEdit   = onEdit;
    }

    /** Called by LiveData observer in NotesActivity */
    public void submitList(List<NoteEntity> newList) {
        this.noteList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder h, int position) {
        NoteEntity note = noteList.get(position);

        // Title
        h.tvNoteTitle.setText(note.title != null ? note.title : "Untitled");

        // Content preview
        h.tvNoteContent.setText(note.content != null ? note.content : "");

        // Accent bar color cycles through palette
        int color = ACCENT_COLORS[position % ACCENT_COLORS.length];
        h.viewAccent.setBackgroundColor(color);

        // Date — use createdAt if available, else now
        long timestamp = note.createdAt > 0 ? note.createdAt : System.currentTimeMillis();
        h.tvNoteDate.setText(sdf.format(new Date(timestamp)));

        // Word count
        int words = wordCount(note.content);
        h.tvWordCount.setText(words + " word" + (words == 1 ? "" : "s"));

        // Edit button
        h.btnEdit.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) onEdit.accept(noteList.get(pos));
        });

        // Delete button
        h.btnDelete.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) onDelete.accept(noteList.get(pos));
        });

        // Card click = edit (same as edit button)
        h.cardNote.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) onEdit.accept(noteList.get(pos));
        });
    }

    @Override
    public int getItemCount() { return noteList.size(); }

    private int wordCount(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        CardView    cardNote;
        View        viewAccent;
        TextView    tvNoteTitle, tvNoteContent, tvNoteDate, tvWordCount;
        ImageButton btnEdit, btnDelete;

        NoteViewHolder(@NonNull View v) {
            super(v);
            cardNote      = v.findViewById(R.id.cardNote);
            viewAccent    = v.findViewById(R.id.viewAccent);
            tvNoteTitle   = v.findViewById(R.id.tvNoteTitle);
            tvNoteContent = v.findViewById(R.id.tvNoteContent);
            tvNoteDate    = v.findViewById(R.id.tvNoteDate);
            tvWordCount   = v.findViewById(R.id.tvWordCount);
            btnEdit       = v.findViewById(R.id.btnEdit);
            btnDelete     = v.findViewById(R.id.btnDelete);
        }
    }
}