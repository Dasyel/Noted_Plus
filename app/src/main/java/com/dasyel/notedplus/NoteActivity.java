package com.dasyel.notedplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class NoteActivity extends ActionBarActivity {
    ArrayList<String> noteNames;
    SQLiteDatabase myDB;
    String noteName;
    SharedPreferences sp;
    String imageString;
    String imagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        sp = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        noteNames = intent.getStringArrayListExtra("noteNames");
        noteName = intent.getStringExtra("noteName");
        myDB = openOrCreateDatabase("NotedDB", MODE_PRIVATE, null);
        if (noteName != null) {
            Cursor resultSet = myDB.rawQuery(
                    "Select * from Notes WHERE Name = '" + noteName + "'", null);
            resultSet.moveToFirst();
            String noteBody = resultSet.getString(1);
            imageString = resultSet.getString(2);
            resultSet.close();

            EditText name = (EditText) findViewById(R.id.noteName);
            EditText body = (EditText) findViewById(R.id.noteBody);

            name.setText(noteName);
            body.setText(noteBody);
            reloadImageButton();
        }
    }

    public void reloadImageButton(){
        final Button imageButton = (Button) findViewById(R.id.imageButton);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        if (imageString != null && !imageString.equals("")){
            File imgFile = new  File(imageString);
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                try {
                    ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    Log.d("EXIF", "Exif: " + orientation);
                    Matrix matrix = new Matrix();
                    if (orientation == 6) {
                        matrix.postRotate(90);
                    }
                    else if (orientation == 3) {
                        matrix.postRotate(180);
                    }
                    else if (orientation == 8) {
                        matrix.postRotate(270);
                    }
                    myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true); // rotating bitmap
                }
                catch (Exception e) {
                    Log.d("ImageLoader", "Something went wrong!");
                }
                Bitmap bMapScaled = Bitmap.createScaledBitmap(myBitmap, myBitmap.getWidth()/4, myBitmap.getHeight()/4, true);
                imageView.setImageBitmap(bMapScaled);
            }

            imageButton.setText(R.string.view_image);
            imageButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v){
                    imageString = "";
                    myDB.execSQL("UPDATE Notes SET Image = '' WHERE Name = '" + noteName + "';");
                    imageButton.setOnClickListener(new Button.OnClickListener() {
                        public void onClick(View v) {
                            addImage(v);
                        }
                    });
                    imageView.setImageBitmap(null);
                    imageButton.setText(R.string.add_image);
                }
            });
            imageView.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    File imageFile = new File(imageString);
                    Intent i = new Intent();
                    i.setAction(android.content.Intent.ACTION_VIEW);
                    i.setDataAndType(Uri.fromFile(imageFile), "image/*");
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EditText name = (EditText) findViewById(R.id.noteName);
        EditText body = (EditText) findViewById(R.id.noteBody);
        String nameString = name.getText().toString();
        String bodyString = body.getText().toString();
        SharedPreferences.Editor spEditor = sp.edit();
        if (!sp.contains("nameCount")) {
            spEditor.putInt("nameCount", 0);
            spEditor.apply();
        }
        if (nameString.equals("")) {
            nameString = "Note " + Integer.toString(sp.getInt("nameCount", 0));
            spEditor.putInt("nameCount", sp.getInt("nameCount", 0) + 1);
            spEditor.apply();
            while (noteNames.contains(nameString)) {
                nameString = "Note " + Integer.toString(sp.getInt("nameCount", 0));
                spEditor.putInt("nameCount", sp.getInt("nameCount", 0) + 1);
                spEditor.apply();
            }
        }
        if (noteName == null || !nameString.equals(noteName)) {
            int i = 0;
            String newName = nameString;
            while (noteNames.contains(newName)) {
                newName = nameString + Integer.toString(i);
                i++;
            }
            nameString = newName;
        }
        if (noteName == null) {
            myDB.execSQL("INSERT INTO Notes VALUES('" + nameString + "','" + bodyString + "','');");
        } else {
            myDB.execSQL("UPDATE Notes SET Name = '" + nameString + "', " +
                    "Body= '" + bodyString + "' WHERE Name = '" + noteName + "';");
        }

        noteName = nameString;
        spEditor.putString("currentNote", nameString);
        spEditor.apply();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addImage(View button) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = getOutputMediaFileUri(); // create a file to save the image
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, 100);
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NotedPlus");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        imagePath = mediaFile.getAbsolutePath();
        return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                myDB.execSQL("UPDATE Notes SET Image = '" + imagePath +
                        "' WHERE Name = '" + noteName + "';");
                imageString = imagePath;
                reloadImageButton();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }
}