package com.project.memeapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    ImageView memeIV;
    ImageButton shareBtn, saveBtn, nextBtn;
    String url;

    String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        memeIV = findViewById(R.id.memeIV);
        shareBtn = findViewById(R.id.shareBtn);
        saveBtn = findViewById(R.id.saveBtn);
        nextBtn = findViewById(R.id.nextBtn);

        apiCall();

        nextBtn.setOnClickListener(v -> apiCall());

        shareBtn.setOnClickListener(v -> {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) memeIV.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();

            String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
            Uri uri = Uri.parse(bitmapPath);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "share to :"));
        });

    }

    public void apiCall() {
        url = "https://meme-api.com/gimme";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        url = response.getString("url");
                        Glide.with(MainActivity.this).load(url).into(memeIV);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }, error -> {
        });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Apps needs External storage permission to share the Memes")
                        .setTitle("Permission required")
                        .setCancelable(false)
                        .setPositiveButton("Ok", (dialog, which) -> {
                            requestPermissions();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        });
                alertDialog.show();
            } else {
                requestPermissions();
            }
        }
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Required Permission")
                        .setMessage("This feature is unavailable because this uses the permission that you denied, please allow the permission from settings to use this feature")
                        .setCancelable(false)
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            dialog.dismiss();
                        });
                builder.show();
            } else {
                requestPermissions();
            }
        }
    }
}