package com.droidloft.painty2;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class Gallery extends AppCompatActivity {

    private ImageAdapter imageAdapter;
    private ArrayList<String> f = new ArrayList<>();
    private ArrayList<String> fname = new ArrayList<>();
    private File[] listFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getFromSDCard();
        GridView imageGrid = findViewById(R.id.galleryGridView);
        imageAdapter = new ImageAdapter();
        imageGrid.setAdapter(imageAdapter);

        imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog shareDialog = new Dialog(Gallery.this);
                LayoutInflater inflater = (LayoutInflater) Gallery.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View galleryShareLayout = inflater.inflate(R.layout.gallery_share_layout, (ViewGroup)findViewById(R.id.gallery_share_layout));
                shareDialog.setContentView(galleryShareLayout);
                shareDialog.show();

                ImageView imageView = galleryShareLayout.findViewById(R.id.selectedImageImageView);
                Button shareButton = galleryShareLayout.findViewById(R.id.shareButton);

                File file = new File(android.os.Environment.getExternalStorageDirectory(), "Painty2/" + fname.get(position));
                final Bitmap myBitmap = BitmapFactory.decodeFile(String.valueOf(file));
                imageView.setImageBitmap(myBitmap);

                shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareDrawing(myBitmap);
                        shareDialog.cancel();
                    }
                });
            }
        });

        imageGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Dialog deleteDialog = new Dialog(Gallery.this);
                LayoutInflater inflater = (LayoutInflater) Gallery.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View galleryDeleteLayout = inflater.inflate(R.layout.gallery_delete_layout, (ViewGroup)findViewById(R.id.gallery_delete_layout));
                deleteDialog.setContentView(galleryDeleteLayout);
                deleteDialog.show();

                ImageView imageView = galleryDeleteLayout.findViewById(R.id.deleteImageView);
                Button deleteButton = galleryDeleteLayout.findViewById(R.id.deleteButton);

                String filename = String.valueOf(fname.get(position));
                final File file = new File(android.os.Environment.getExternalStorageDirectory(), "Painty2/" + filename);
                Bitmap myBitmap = BitmapFactory.decodeFile(String.valueOf(file));
                imageView.setImageBitmap(myBitmap);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeAToast("Drawing Deleted");
                        deleteDialog.cancel();
                        file.delete();
                        f.clear();
                        fname.clear();
                        getFromSDCard();
                        imageAdapter.notifyDataSetChanged();
                    }
                });



                return true;
            }
        });



    }

    private void makeAToast(String tMessage) {
        Toast.makeText(Gallery.this, tMessage, Toast.LENGTH_SHORT).show();
    }

    private void shareDrawing(final Bitmap myBitmap) {
        Permissions.check(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Uri uri = getImageUri(Gallery.this, myBitmap);
                shareImageUri(uri);

            }
        });
    }

    private void shareImageUri(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/jpg");
        startActivity(intent);

    }

    private Uri getImageUri(Context context, Bitmap inImage) {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void getFromSDCard() {

        Permissions.check(this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                File file = new File(android.os.Environment.getExternalStorageDirectory(), "Painty2");
                if (file.isDirectory()) {
                    listFile = file.listFiles();

                    for (int i = 0; i < listFile.length; i++) {
                        f.add(listFile[i].getAbsolutePath());
                        fname.add(listFile[i].getName());
                    }
                }
            }
        });
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return f.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.gallery_item, null);
                holder.imageView = convertView.findViewById(R.id.gallery_item_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Bitmap myBitmap = BitmapFactory.decodeFile(f.get(position));
            holder.imageView.setImageBitmap(myBitmap);
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageView;
    }
}
