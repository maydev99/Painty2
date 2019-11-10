package com.droidloft.painty2;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.simplify.ink.InkView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    private static final String version = "0.4", buildDate = "11-10-2019";
    private String colorStr;
    private ImageView colorImageView;
    private InkView inkView;
    private SeekBar brushSeekbar;
    private int seekbarInit = 6;
    private TextView brushSizeTextView, colorTextView;
    private boolean colorIsSetToBrush = true;
    private ArrayList<String> recentColorsArrayList;
    private Set<String> set;
    private boolean isEditingImage = false;
    private String editFilename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        inkView = findViewById(R.id.inkview);
        initializeShakeDetector();

        colorImageView = findViewById(R.id.color_imageview);
        brushSeekbar = findViewById(R.id.brushSizeSeekbar);
        brushSizeTextView = findViewById(R.id.brushSizeTextView);
        colorTextView = findViewById(R.id.colorTextView);

        brushSeekbar.setProgress(seekbarInit);
        brushSizeTextView.setText("Brush Size:" + seekbarInit);
        inkView.setMaxStrokeWidth(6f);
        inkView.setMinStrokeWidth(4f);
        inkView.setBackgroundColor(Color.WHITE);

        colorTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(colorIsSetToBrush) {
                    colorTextView.setText("Eraser");
                    colorIsSetToBrush = false;
                    inkView.setColor(Color.WHITE);
                    colorImageView.setBackgroundColor(Color.WHITE);


                } else {
                    colorTextView.setText("Brush");
                    colorIsSetToBrush = true;
                    inkView.setColor(Color.parseColor(colorStr));
                    colorImageView.setBackgroundColor(Color.parseColor(colorStr));
                }
            }
        });


        colorImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!colorIsSetToBrush) {
                    colorTextView.setText("Brush");
                    colorIsSetToBrush = true;
                }
                startActivity(new Intent(MainActivity.this, ColorPicker.class));
            }
        });

        colorImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showRecentColorsDialog();
                return true;
            }
        });

        brushSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float progressFloat = (float) progress;

                inkView.setMaxStrokeWidth(progressFloat);
                inkView.setMinStrokeWidth(progressFloat - 2);
                brushSizeTextView.setText("Brush Size: " + progress);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




    }

    private void initializeShakeDetector() {
        ShakeDetector.create(this, new ShakeDetector.OnShakeListener() {
            @Override
            public void OnShake() {
                inkView.clear();
            }
        });
        ShakeDetector.updateConfiguration(3.0f,4);
    }

    private void showRecentColorsDialog() {
        final Dialog recentColorDialog = new Dialog(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View recentColorLayout = inflater.inflate(R.layout.recent_colors_layout, (ViewGroup) findViewById(R.id.recent_colors_layout));
        recentColorDialog.setContentView(recentColorLayout);

        recentColorDialog.show();

        final ListView recentColorsListView = recentColorLayout.findViewById(R.id.recentColorsListView);
        ArrayAdapter<String> recentColorsArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.list_item_item, recentColorsArrayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the current item from ListView
                View view = super.getView(position, convertView, parent);
                String myColor = (String) recentColorsListView.getItemAtPosition(position);
                view.setBackgroundColor(Color.parseColor(myColor));

                return view;
            }
        };

        recentColorsListView.setAdapter(recentColorsArrayAdapter);






        recentColorsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                colorStr = (String) recentColorsListView.getItemAtPosition(position);
                recentColorDialog.cancel();
                colorImageView.setBackgroundColor(Color.parseColor(colorStr));
                inkView = findViewById(R.id.inkview);
                inkView.setColor(Color.parseColor(colorStr));
            }
        });
    }


    private void loadColorPrefs() {
        SharedPreferences colorPrefs = getSharedPreferences("color_key", MODE_PRIVATE);
        colorStr = colorPrefs.getString("color_key", "0xffffffff");
        //makeAToast(colorStr);
        colorStr = colorStr.substring(2, 10);
        colorStr = "#" + colorStr;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.about) {
            AlertDialog.Builder aboutAlert = new AlertDialog.Builder(this);
            aboutAlert.setTitle("Painty2 v" + version);
            aboutAlert.setMessage("Build Date: " + buildDate + "\n" + "by Michael May" + "\n" + "DroidLoft");
            aboutAlert.setIcon(R.mipmap.ic_launcher);
            aboutAlert.show();
        }
        
        if(item.getItemId() == R.id.save) {
            saveDrawingToDevice();
        }


        if(item.getItemId() == R.id.clear_canvas) {
            inkView.clear();
        }

        if(item.getItemId() == R.id.share) {
            shareDrawing();

        }

        if(item.getItemId() == R.id.gallery) {
            startActivity(new Intent(MainActivity.this, Gallery.class));
        }



        return super.onOptionsItemSelected(item);
    }

    private void shareDrawing() {

        Permissions.check(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Bitmap myBitmap = inkView.getBitmap();
                myBitmap = inkView.getBitmap(getResources().getColor(R.color.white)); //Sets Background color
                Uri uri = getImageUri(MainActivity.this, myBitmap);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setType("image/jpg");
                startActivity(shareIntent);
            }
        });



    }

    private void saveDrawingToDevice() {
        final String filename = getTimeStamp() + ".jpg";
        Permissions.check(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                File sdCard = Environment.getExternalStorageDirectory();
                String myDir = "/Painty2/";
                OutputStream outputStream = null;
                File dir = new File(sdCard.getAbsolutePath() + myDir);
                dir.mkdir();
                File file = new File(dir, filename);

                try {
                    outputStream = new FileOutputStream(file);
                    Bitmap myBitmap = inkView.getBitmap();
                    myBitmap = inkView.getBitmap(getResources().getColor(R.color.white)); //Sets Background color
                    myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                    makeAToast("Saved");

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG,100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getTimeStamp() {
        String timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + "";
        return timeStamp;
    }

    private void makeAToast(String tMessage) {
        Toast.makeText(MainActivity.this, tMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadEditPrefs();
        recentColorsArrayList = new ArrayList<>();
        colorStr = "#ffffff";
        loadColorPrefs();
        loadRecentColors();
        colorImageView.setBackgroundColor(Color.parseColor(colorStr));
        inkView = findViewById(R.id.inkview);
        inkView.setColor(Color.parseColor(colorStr));
        saveToRecentColors(colorStr);

        if(isEditingImage) {
            loadImage();
            isEditingImage = false;
            saveEditingPrefs();
        }




    }

    private void saveEditingPrefs() {
        SharedPreferences editPrefs = getSharedPreferences("edit_key", MODE_PRIVATE);
        SharedPreferences.Editor editEditor = editPrefs.edit();
        editEditor.putBoolean("edit_key", isEditingImage);
        editEditor.apply();
    }

    private void loadImage() {

        try {
            File file = new File(android.os.Environment.getExternalStorageDirectory(), "Painty2/" + editFilename);
            final Bitmap myBitmap = BitmapFactory.decodeFile(String.valueOf(file));
            inkView.drawBitmap(myBitmap, 0 ,0, null);
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    private void loadEditPrefs() {
        SharedPreferences editPrefs = getSharedPreferences("edit_key", MODE_PRIVATE);
        isEditingImage = editPrefs.getBoolean("edit_key", false);

        SharedPreferences fileNamePrefs = getSharedPreferences("filename_key", MODE_PRIVATE);
        editFilename = fileNamePrefs.getString("filename_key", null);
    }

    private void loadRecentColors() {
        SharedPreferences recentColorPrefs = getSharedPreferences("recent_colors", MODE_PRIVATE);
        set = recentColorPrefs.getStringSet("recent_colors", null);

        if(set != null) {
            recentColorsArrayList.clear();
            recentColorsArrayList = new ArrayList<String>(set);
        } else {
            recentColorsArrayList = new ArrayList<>();
        }
    }

    private void saveToRecentColors(String addedColor) {
        if(recentColorsArrayList.size() < 10) {
            if(recentColorsArrayList.size() > 0){
                recentColorsArrayList.add(recentColorsArrayList.size() -1, addedColor);
            } else {
                recentColorsArrayList.add(addedColor);
            }



        } else {
            recentColorsArrayList.remove(0);
            recentColorsArrayList.add(addedColor);
        }

        set = new HashSet<>();
        set.addAll(recentColorsArrayList);

        SharedPreferences recentColorPrefs = getSharedPreferences("recent_colors", MODE_PRIVATE);
        SharedPreferences.Editor recentColorEditor = recentColorPrefs.edit();
        recentColorEditor.putStringSet("recent_colors", set);
        recentColorEditor.apply();

        Log.d("RECENT COLORS", String.valueOf(recentColorsArrayList));



    }

    @Override
    protected void onResume() {
        super.onResume();
        ShakeDetector.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ShakeDetector.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ShakeDetector.destroy();
    }


}
