package com.dasyel.notedplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class Dispatcher extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        String currentNote = sp.getString("currentNote", "");
        if (currentNote != null && !currentNote.equals("")){
            SharedPreferences.Editor spEditor = sp.edit();
            spEditor.putBoolean("goToNote", true);
            spEditor.apply();
        }
        startActivity(new Intent(this, OverviewActivity.class));
    }
}
