package com.example.lifesync.activities.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.NoteAdapter;
import com.example.lifesync.activities.models.NoteEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import repository.NoteRepository;

public class NotesActivity extends BaseActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private TextInputEditText etTitle, etContent;
    private MaterialButton    btnAdd;
    private ImageButton       btnCancelEdit;
    private RecyclerView      recyclerView;
    private TextView          tvNoteCount, tvFormTitle, tvFormSubtitle;
    private View              layoutEmpty;
    private FloatingActionButton fabScrollTop;

    // ── Data ──────────────────────────────────────────────────────────────────
    private NoteRepository noteRepo;
    private NoteAdapter    adapter;
    private NoteEntity     selectedNote = null; // non-null means edit mode

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        noteRepo = new NoteRepository(this);

        bindViews();
        setupToolbar();          // from BaseActivity — uses toolbar id R.id.toolbar
        setupRecyclerView();
        observeNotes();
        setupListeners();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    private void bindViews() {
        etTitle        = findViewById(R.id.etTitle);
        etContent      = findViewById(R.id.etContent);
        btnAdd         = findViewById(R.id.btnAdd);
        btnCancelEdit  = findViewById(R.id.btnCancelEdit);
        recyclerView   = findViewById(R.id.recyclerView);
        tvNoteCount    = findViewById(R.id.tvNoteCount);
        tvFormTitle    = findViewById(R.id.tvFormTitle);
        tvFormSubtitle = findViewById(R.id.tvFormSubtitle);
        layoutEmpty    = findViewById(R.id.layoutEmpty);
        fabScrollTop   = findViewById(R.id.fabScrollTop);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new NoteAdapter(this::deleteNote, this::editNote);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        // Show FAB when scrolled down
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (dy > 0) fabScrollTop.setVisibility(View.VISIBLE);
                else if (!rv.canScrollVertically(-1)) fabScrollTop.setVisibility(View.GONE);
            }
        });
    }

    // ── LiveData observer — shows saved notes from Room ───────────────────────

    private void observeNotes() {
        noteRepo.getAllNotes().observe(this, notesList -> {
            if (notesList != null) {
                adapter.submitList(notesList);

                // Update count in toolbar subtitle
                int count = notesList.size();
                tvNoteCount.setText(count + " note" + (count == 1 ? "" : "s"));

                // Show empty state or list
                boolean hasNotes = count > 0;
                recyclerView.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
                layoutEmpty.setVisibility(hasNotes ? View.GONE : View.VISIBLE);
            }
        });
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {

        // Add / Update button — same logic as before, just tidied up
        btnAdd.setOnClickListener(v -> {
            String title   = etTitle.getText() != null
                    ? etTitle.getText().toString().trim() : "";
            String content = etContent.getText() != null
                    ? etContent.getText().toString().trim() : "";

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this,
                        "Please enter title and content", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedNote == null) {
                // ── ADD ───────────────────────────────────────────────────────
                noteRepo.addNote(title, content);
                Toast.makeText(this, "Note saved ✓", Toast.LENGTH_SHORT).show();
            } else {
                // ── UPDATE ────────────────────────────────────────────────────
                selectedNote.title   = title;
                selectedNote.content = content;
                noteRepo.updateNote(selectedNote);
                Toast.makeText(this, "Note updated ✓", Toast.LENGTH_SHORT).show();
                exitEditMode();
            }

            clearForm();
        });

        // Cancel edit — return to add mode
        btnCancelEdit.setOnClickListener(v -> {
            exitEditMode();
            clearForm();
        });

        // FAB scroll to top
        fabScrollTop.setOnClickListener(v ->
                recyclerView.smoothScrollToPosition(0));
    }

    // ── Edit / Delete callbacks passed to adapter ─────────────────────────────

    private void editNote(NoteEntity note) {
        selectedNote = note;

        // Pre-fill form
        etTitle.setText(note.title);
        etContent.setText(note.content);
        etTitle.requestFocus();

        // Switch form to edit mode
        tvFormTitle.setText("Edit Note");
        tvFormSubtitle.setText("Tap Update when done");
        btnAdd.setText("Update Note");
        btnAdd.setIconResource(android.R.drawable.ic_menu_edit);
        btnCancelEdit.setVisibility(View.VISIBLE);

        // Scroll up so the form is visible
        recyclerView.smoothScrollToPosition(0);
    }

    private void deleteNote(NoteEntity note) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Delete \"" + note.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    noteRepo.deleteNote(note);
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void exitEditMode() {
        selectedNote = null;
        tvFormTitle.setText("New Note");
        tvFormSubtitle.setText("Write something to remember");
        btnAdd.setText("Add Note");
        btnAdd.setIconResource(android.R.drawable.ic_input_add);
        btnCancelEdit.setVisibility(View.GONE);
    }

    private void clearForm() {
        etTitle.setText("");
        etContent.setText("");
        etTitle.clearFocus();
        etContent.clearFocus();
    }
}