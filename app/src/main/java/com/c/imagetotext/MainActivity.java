package com.c.imagetotext;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {
    EditText mResultEt;
    ImageView mPreViewIV;
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    String cameraPermission[];
    String storagePermission[];
    Uri image_Uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle("Click + Button to Insert Image");
        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setSubtitle("Click + Button To Insert Image");

        // camera permission
        cameraPermission = new String[]{Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //storage permission
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mResultEt = findViewById(R.id.resultEt);
        mPreViewIV = findViewById(R.id.imageView);
    }

    //action barMenu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    // handle action bar item clicks


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addimage)
        {
            showImageImportdailog();


        }
        if (id == R.id.settings)
        {
            Toast.makeText(this,"setting",Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportdailog()
    {
        //item to display in dialog
        String [] item = {" Camera"," Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // set title
        dialog.setTitle("Select Image");
        dialog.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0)
                {
                    //camera option clicked
                    if (!CheckCameraPermission())
                    {
                        //camera permission not allowed, request it

                        requestCameraPermission();
                    }
                    else
                    {
                        // permission allowed take picture
                        PickCamera();
                    }
                }
                if (i == 1)
                {
                    //gallery option clicked
                    if (!checkStoragePermission())
                    {
                        //Storage permission not allowed, request it

                        requestStoragePermissin();
                    }
                    else
                    {
                        // permission allowed take picture
                        PickGallery();
                    }
                }

            }
        });
        dialog.create().show(); //show dialog
    }

    private void PickGallery() {
        // Intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        // set Intent type to image
        intent.setType("image/+");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void PickCamera() {
        // intent to take image from camera: it will also be save to storage to get high quality image
        ContentValues value = new ContentValues();
        value.put(MediaStore.Images.Media.TITLE,"NewPic"); //title of the picture
        value.put(MediaStore.Images.Media.DESCRIPTION,"Image to text"); // description
        image_Uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,value);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_Uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermissin() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean CheckCameraPermission() {
        /*Check permission and return result
         * In order to get high quality image we have to save image to external first
         * before inserting to image that"s why storage will also be required*/
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }
    // handle permission result

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode)
       {
           case CAMERA_REQUEST_CODE:

               if (grantResults.length >0)
               {
                   boolean cameraAccepted = grantResults [0] ==
                           PackageManager.PERMISSION_GRANTED;
                   boolean writeStorageAccepted = grantResults[0] ==
                           PackageManager.PERMISSION_GRANTED;
                   if (cameraAccepted && writeStorageAccepted)
                   {
                       PickCamera();
                   }
                   else
                   {
                       Toast.makeText(this,"Permission deniet",Toast.LENGTH_LONG).show();
                   }
               }
               break;

           case STORAGE_REQUEST_CODE:

               if (grantResults.length >0)
               {

                   boolean writeStorageAccepted = grantResults[0] ==
                           PackageManager.PERMISSION_GRANTED;
                   if (writeStorageAccepted)
                   {
                       PickGallery();
                   }
                   else
                   {
                       Toast.makeText(this,"Permission deniet",Toast.LENGTH_LONG).show();
                   }
               }
               break;
       }
    }
    //handle image result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // got image from camera
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                // got image from gallery now corp it
                CropImage.activity(data.getData())
                        .setGuidelines(CropImageView.Guidelines.ON)// image enable
                .start(this);

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                // got image from camera now corp it
                // got image from gallery now corp it
                CropImage.activity(image_Uri)
                        .setGuidelines(CropImageView.Guidelines.ON)// image enable
                        .start(this);

            }

        }

        //get cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            Log.e("ok","dynamic");


            if (requestCode == RESULT_OK)
            {Log.e("ok","dynamic");
                Uri resultUri = result.getUri();// get image

                //set image to image view
                mPreViewIV.setImageURI(resultUri);
                //get drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreViewIV.getDrawable();

                Bitmap bitmap = bitmapDrawable.getBitmap();
                Log.e("ok","ok");
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if (!recognizer.isOperational())
                {
                    Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();

                }
                else
                {
                    Log.e("ok","shafiq");
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> item = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();
                    // get text from sb until there is no text
                    for (int i=0 ;i<item.size();i++)
                    {
                        TextBlock myItem = item.valueAt(i);
                        sb.append(myItem.getValue());
                        Log.e("find",""+myItem.getValue());
                        sb.append("\n");
                    }

                    // set text to edit text
                    mResultEt.setText(sb.toString());
                }



            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                //if there is any error show it
                Log.e("ok","dd");
                Exception exception = result.getError();
                Toast.makeText(this,""+exception,Toast.LENGTH_LONG).show();

            }
else
            {
                Log.e("ok","dynamic");
                Uri resultUri = result.getUri();// get image

                //set image to image view
                mPreViewIV.setImageURI(resultUri);
                //get drawable bitmap for text recognition
                BitmapDrawable bitmapDrawable = (BitmapDrawable)mPreViewIV.getDrawable();

                Bitmap bitmap = bitmapDrawable.getBitmap();
                Log.e("ok","ok");
                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();

                if (!recognizer.isOperational())
                {
                    Toast.makeText(this,"Error",Toast.LENGTH_LONG).show();

                }
                else
                {
                    Log.e("ok","shafiq");
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> item = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();
                    // get text from sb until there is no text
                    for (int i=0 ;i<item.size();i++)
                    {
                        TextBlock myItem = item.valueAt(i);
                        sb.append(myItem.getValue());
                        Log.e("find",""+myItem.getValue());
                        sb.append("\n");
                    }

                    // set text to edit text
                    mResultEt.setText(sb.toString());
                }



            }

        }
    }
}
