package com.example.lifesync.activities.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.NoteAdapter;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.NoteEntity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import repository.NoteRepository;

public class NotesActivity extends BaseActivity {

    private AppDatabase db;
    private RecyclerView recyclerView;
    private NoteRepository noteRepo;
    private NoteAdapter adapter;

    NoteEntity selectedNote = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        noteRepo = new NoteRepository(this);
        setupToolbar();

        //Database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "lifesync-db").allowMainThreadQueries().build();

        //Views
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etContent = findViewById(R.id.etContent);
        Button btnAdd = findViewById(R.id.btnAdd);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoteAdapter(new ArrayList<>(), this::deleteNote, this::editNote);
        recyclerView.setAdapter(adapter);

        noteRepo.getAllNotes().observe(this, notesList -> {
            if (notesList != null) {
                adapter.submitList(notesList);
            }
        });

        //Button click
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etContent.getText().toString();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please enter title and content", Toast.LENGTH_SHORT).show();
                return;
            }

            //Add notes
            if(selectedNote==null) {
                noteRepo.addNote(title, content);
                Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
            }
            else {
                //update notes
                selectedNote.title = title;
                selectedNote.content = content;
                noteRepo.updateNote(selectedNote);

                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
                selectedNote = null;
                btnAdd.setText("Add");
            }
            etTitle.setText("");
            etContent.setText("");
        });
    }

    private void deleteNote(NoteEntity note) {
        noteRepo.deleteNote(note);
    }

    private void editNote(NoteEntity note) {
        selectedNote = note;

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etContent = findViewById(R.id.etContent);
        Button btnAdd = findViewById(R.id.btnAdd);

        etTitle.setText(note.title);
        etContent.setText(note.content);

        btnAdd.setText("Update");
    }
}
