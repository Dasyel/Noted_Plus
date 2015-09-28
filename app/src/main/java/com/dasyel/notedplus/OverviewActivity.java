package com.dasyel.notedplus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class OverviewActivity extends ActionBarActivity {
    ListView listView ;
    ArrayAdapter<String> adapter;
    ArrayList<String> noteNames;
    SQLiteDatabase myDB;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        sp = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = sp.edit();

        noteNames = new ArrayList<>();
        myDB = openOrCreateDatabase("NotedDB", MODE_PRIVATE, null);
        myDB.execSQL("CREATE TABLE IF NOT EXISTS Notes(Name VARCHAR,Body VARCHAR, Image VARCHAR);");
        Cursor resultSet = myDB.rawQuery("Select * from Notes", null);
        while (resultSet.moveToNext()){
            noteNames.add(resultSet.getString(0));
        }
        resultSet.close();
        String noteName = sp.getString("currentNote", "");
        if (sp.getBoolean("goToNote", false) && noteNames.contains(noteName)){
            spEditor.putBoolean("goToNote", false);
            spEditor.apply();
            openNote(noteName);
        } else {

            listView = (ListView) findViewById(R.id.listView);
            adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, noteNames);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String itemValue = (String) listView.getItemAtPosition(position);
                    openNote(itemValue);
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long arg3) {
                    final int position = pos;
                    new AlertDialog.Builder(OverviewActivity.this)
                            .setTitle(R.string.delete_title)
                            .setMessage(R.string.delete_message)
                            .setPositiveButton(R.string.delete_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String itemValue = (String) listView.getItemAtPosition(position);
                                    deleteNote(itemValue);
                                }
                            })
                            .setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return true;
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putString("currentNote", "");
        spEditor.apply();
    }

    public void newNote(View button){
        openNote(null);
    }

    public void openNote(String noteName){
        Intent intent = new Intent(this, NoteActivity.class);
        ArrayList<String> nn = new ArrayList<>(noteNames);
        nn.remove(noteName);
        intent.putStringArrayListExtra("noteNames", nn);
        intent.putExtra("noteName", noteName);
        startActivity(intent);
    }

    public void deleteNote(String noteName){
        myDB.execSQL("DELETE FROM Notes WHERE Name = '"+noteName+"';");
        noteNames.remove(noteName);
        adapter.notifyDataSetChanged();
    }


}
