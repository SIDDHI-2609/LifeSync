package com.example.lifesync.activities;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        //Database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "lifesync-db").allowMainThreadQueries().build();

        //Views
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etContent = findViewById(R.id.etContent);
        Button btnAdd = findViewById(R.id.btnAdd);

        //Display data
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Note> notes = db.noteDao().getAllNotes();

        //Button click
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString();
            String content = etContent.getText().toString();

            Note note = new Note();
            note.title = title;
            note.content = content;

            db.noteDao().insert(note);

            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();

            //Refresh
            List<Note> updatedNotes = db.noteDao().getAllNotes();
            NoteAdapter newAdapter = new NoteAdapter(notes, new NoteAdapter.OnDeleteClickListener() {
                @Override
                public void onDeleteClick(Note note) {
                    db.noteDao().delete(note);
                    List<Note> notes = db.noteDao().getAllNotes();
                    recyclerView.setAdapter(new NoteAdapter(updatedNotes, this));
                }
            });
            recyclerView.setAdapter(newAdapter);
        });
    }
}
