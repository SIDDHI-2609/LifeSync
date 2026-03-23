package com.example.lifesync.activities.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.example.lifesync.R;
import com.example.lifesync.activities.adapters.NoteAdapter;
import com.example.lifesync.activities.database.AppDatabase;
import com.example.lifesync.activities.models.Note;

import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView recyclerView;

    Note selectedNote = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        //Database
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "lifesync-db").allowMainThreadQueries().build();

        //Views
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etContent = findViewById(R.id.etContent);
        Button btnAdd = findViewById(R.id.btnAdd);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadNotes();

        //Button click
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etContent.getText().toString();

            //Add notes
            if(selectedNote==null) {
                Note note = new Note();
                note.title = title;
                note.content = content;

                db.noteDao().insert(note);

                Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
            }
            else {
                //update notes
                selectedNote.title = title;
                selectedNote.content = content;

                db.noteDao().update(selectedNote);

                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
                selectedNote = null;
                btnAdd.setText("Add");

            }
            etTitle.setText("");
            etContent.setText("");
            loadNotes();
        });
    }

    private void loadNotes() {
        if (db != null) {
            List<Note> notes = db.noteDao().getAllNotes();
            NoteAdapter adapter = new NoteAdapter(notes, this::deleteNote, this::editNote);
            recyclerView.setAdapter(adapter);
        }
    }

    private void deleteNote(Note note) {
        if (db != null) {
            db.noteDao().delete(note);
            loadNotes();
        }
    }
    private void editNote(Note note) {
        selectedNote = note;

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etContent = findViewById(R.id.etContent);
        Button btnAdd = findViewById(R.id.btnAdd);

        etTitle.setText(note.title);
        etContent.setText(note.content);

        btnAdd.setText("Update");
    }
}
