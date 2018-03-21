package jp.techacademy.hojun.bun.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.StaleDataException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  {

    Button mStartPauseButton;
    Button mForwardButton;
    Button mBackButton;
    Cursor cursor;
    Timer mTimer;


    Handler mHandler = new Handler();

    private static final int PERMISSIONS_REQUEST_CODE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartPauseButton = findViewById(R.id.button1);
        mForwardButton = findViewById(R.id.button2);
        mBackButton = findViewById(R.id.button3);

            mStartPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int p = getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getPackageName());
                    if(p == PackageManager.PERMISSION_GRANTED) {

                        if (mTimer == null) {
                            TextView textView = findViewById(R.id.button1);
                            textView.setText("停止");
                            mForwardButton.setEnabled(false);
                            mBackButton.setEnabled(false);
                            mTimer = new Timer();
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            getNextInfo();
                                        }
                                    });
                                }
                            }, 2000, 2000);
                        }
                        else {
                            TextView textView = findViewById(R.id.button1);
                            textView.setText("再生");
                            mForwardButton.setEnabled(true);
                            mBackButton.setEnabled(true);
                            mTimer.cancel();
                            mTimer = null;
                        }
                    }
                    if(p == PackageManager.PERMISSION_DENIED) {
                        showAlertDialog();

                    }



                }
            });

            mForwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNextInfo();
                }
            });

            mBackButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPreviousInfo();
                }
            });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            getContentsInfo();
        }


        }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
        {
            switch (requestCode) {
                case PERMISSIONS_REQUEST_CODE:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getContentsInfo();
                    }
                    break;
                default:
                    break;
            }

    }


    private void getContentsInfo() {


        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);

}

    private void getNextInfo() {

        try {
            if (cursor.moveToNext()) {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView imageVIew = findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);
            } else {
                cursor.moveToFirst();
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView imageVIew = findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);
            }
        } catch (NullPointerException e) {
            showAlertDialog();
        } catch (StaleDataException e) {

        }
    }

    private void getPreviousInfo() {
        try {


            if (cursor.moveToPrevious()) {
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView imageVIew = findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);
            } else {
                cursor.moveToLast();
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                ImageView imageVIew = findViewById(R.id.imageView);
                imageVIew.setImageURI(imageUri);
            }
        } catch (NullPointerException e) {
            showAlertDialog();
        }
    }


    private void showAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("アクセス許可をしてください");
        alertDialogBuilder.setMessage("端末内の写真、メディア、ファイルへのアクセスを許可しなければいけません。");

        alertDialogBuilder.setPositiveButton("了解", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

}
