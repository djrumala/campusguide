package com.common_id.campusguide;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.UUID;

import static com.common_id.campusguide.UserLoginActivity.MyPreferences;

public class UserUploadActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private String IPAddress = "common-id.com";
    private String HttpURL = "http://" + IPAddress + "/campusguide";

    private TextView name, room;
    private ImageView imageView;

    private String IdClass, RoomName, Name, IdUser, Longitude, Latitude, Floor, Address;
    private ImageButton settingButton;

    private Boolean uploaded_file = false;

    private File resizedPath;
    private String CamerafilePath, EncodedURL, imageURL, Extension;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_EX_STORAGE_CODE = 200;
    private int REQUEST_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_upload);

        // setup the broadcast action namespace string which will
        // be used to notify upload status.
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        // Or, you can define it manually.
        UploadService.NAMESPACE = "com.common_id.campusguide";

        name = findViewById(R.id.name);
        room = findViewById(R.id.room);
        imageView = findViewById(R.id.photo);


        SharedPreferences pref = getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        Name = pref.getString(UserLoginActivity.mName, "Anda belum mengisi data");
        IdUser = pref.getString(UserLoginActivity.mId, "Anda belum mengisi data");

        name.setText(Name);

        if (this.getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            Longitude = extras.getString("longitude", "0");
            Latitude = extras.getString("latitude", "0");
            RoomName = extras.getString("name", "");
            Floor = extras.getString("floor", "");
            Address = extras.getString("address", "");
            IdClass = extras.getString("id", "0");
            room.setText(RoomName);
        }
    }

    public void AddCamera(View view) {
        isCameraPermissionGranted();
    }

    public void AddGallery(View view) {
        isStoragePermissionGranted();
    }

    public void AddPhoto(View view) {
//        selectImage();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e("Permit", "PROCESSING");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_EX_STORAGE_CODE);
            } else {
                selectImage();
            }
        } else
            selectImage();
    }


    private void selectImage() {
        final CharSequence[] options = {"Ambil Gambar", "Pilih dari Galeri", "Batal"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Unggah Gambar");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Ambil Gambar")) {
                    isCameraPermissionGranted();
//                    TakePicture();
                } else if (options[item].equals("Pilih dari Galeri")) {
                    isStoragePermissionGranted();
//                    FromGallery();
                } else if (options[item].equals("Batal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_EX_STORAGE_CODE);
                Log.e("Permission", "Storage Permission Not YET");
            } else {
                FromGallery();
            }
        } else FromGallery();
    }

    public void isCameraPermissionGranted() {
        //REQUEST PERMISSION FOR CAMERA
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                Log.e("Permission", "Storage Permission Not YET");
            } else {
                TakePicture();
            }
        } else TakePicture();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_EX_STORAGE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
                selectImage();
            } else {
                Toast.makeText(this, "Oops, you just denied the permission!", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
//                TakePicture();
            } else {
                Toast.makeText(this, "Oops, you just denied the permission!", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void FromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    private void TakePicture() {
        File apkStorage = new File(Environment.getExternalStorageDirectory() + "/CampusGuide"); //Inisiating new directory in user's phone
        if (!apkStorage.exists()) { //Directory hasnt existed
            apkStorage.mkdir(); //make new directory aka folder
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(Environment.getExternalStorageDirectory() + "/CampusGuide/", "temp.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f)); //save the taken picture to the directory FIle f
        CamerafilePath = f.getPath();

        if (Build.VERSION.SDK_INT >= 24) {
            Uri photoURI = FileProvider.getUriForFile(UserUploadActivity.this, BuildConfig.APPLICATION_ID + ".provider", f);
            dispatchTakePictureIntent(f, photoURI);
        } else {
            startActivityForResult(intent, 11);
        }
    }

    private void dispatchTakePictureIntent(File photoFile, Uri photoURI) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 11);
            }
        }
    }

    //HANDLING THE IMAGE CHOOSER AND CAPTURE IMAGE FROM CAMERA ACTIVITY RESULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) { //FILE CHOOSER REQ
            Uri originalPath = data.getData(); //to store uri containing path of the choosen file
            saveCompressedImage(originalPath);
        } else if (requestCode == 11 && resultCode == RESULT_OK) { //CAMERA REQ
            File file = new File(Environment.getExternalStorageDirectory() + "/CampusGuide",
                    "Cam_" + RoomName + "-" + Name + ".jpg"); //path for the compressed picture
            saveCapturedImage(file);
        }
    }

    public void saveCompressedImage(Uri originalPath) {
//        Uri originalPath = data.getData(); //to store uri containing path of the choosen file
        File file = null;
        File apkStorage = new File(Environment.getExternalStorageDirectory() + "/CampusGuide"); //Inisiating new directory in user's phone
        if (!apkStorage.exists()) { //Directory hasnt existed
            apkStorage.mkdir(); //make new directory aka folder
            Log.e("Directory", "Completely not here, and will be made soon");
        }


        String imagetype = getContentResolver().getType(originalPath);
        StringTokenizer tokens = new StringTokenizer(imagetype, "/");
        tokens.nextToken();// this will contain " they taste good"
        String extension = "." + tokens.nextToken();// this will contain " they taste good"

        if (REQUEST_TYPE == 1) { //Deciding where to save the image
            resizedPath = new File(apkStorage, RoomName + "-" + Name + extension); //path for the compressed picture
            file = resizedPath;
        }

        try {
            //Bitmap to get the image from the gallery
            Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), originalPath);

            //Get Image Size
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //original.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            long lengthbmp = imageInByte.length;
            Log.e("Image size", String.valueOf(lengthbmp));

            file.createNewFile(); //to create the compressed file
            FileOutputStream ostream = new FileOutputStream(file); //output goes to the path for compressed image

            //compress image with png or jpeg extension
            if (lengthbmp > 1500000) {
                if (extension.equalsIgnoreCase(".png"))
                    original.compress(Bitmap.CompressFormat.PNG, 10, ostream);

                else if (extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".jpg"))
                    original.compress(Bitmap.CompressFormat.JPEG, 10, ostream);
            } else {
                if (extension.equalsIgnoreCase(".png"))
                    original.compress(Bitmap.CompressFormat.PNG, 60, ostream);

                else if (extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".jpg"))
                    original.compress(Bitmap.CompressFormat.JPEG, 60, ostream);
            }
            ostream.close(); //close ostream

            if (REQUEST_TYPE == 1) {
                //GET THE COMPRESSED IMAGE TO BITMAP TO BE SHOWN ON ImageView
//                Bitmap compressed = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(resizedPath));
//                attachText.setText(getPath(originalPath));
//                attachText.setVisibility(View.GONE);
//                attachText.setClickable(false);
//                imageView.setMaxHeight(280);
//                imageView.setMaxWidth(100);
//                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(original);
//                imageView.setClickable(false);
                uploaded_file = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCapturedImage(File file) {
        try {
            //Bitmap to get the saved image from camera
            Bitmap original = BitmapFactory.decodeFile(CamerafilePath);

            file.createNewFile(); //to create the compressed file
            FileOutputStream ostream = new FileOutputStream(file); //output goes to the path for compressed image
            original.compress(Bitmap.CompressFormat.JPEG, 50, ostream); //compress the image
            ostream.close(); //close ostream

            if (REQUEST_TYPE == 1) {
                resizedPath = file;
//                attachText.setVisibility(View.GONE);
//                attachText.setClickable(false);
//                imageView.setMaxHeight(360);
//                imageView.setMaxWidth(100);
//                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(original);
//                imageView.setClickable(false);
                uploaded_file = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void uploadMultipart(final String filePath) {
        //This is the method responsible for image upload
        //We need the full image path and the name for the image in this methoD

//        Glide.get(getApplicationContext()).clearMemory();
//        Glide.get(getApplicationContext()).clearDiskCache();

        final ProgressDialog[] progressDialog = new ProgressDialog[1];
        //Uploading code
        try {
            String uploadId = UUID.randomUUID().toString();

            //Creating a multi part request
            new MultipartUploadRequest(this, uploadId, HttpURL + "/class.php")

                    .addFileToUpload(filePath, "foto") //Adding file
                    .addParameter("id_user", IdUser) //Adding text parameter to the request
                    .addParameter("id_class", IdClass) //Adding text parameter to the request

                    .setNotificationConfig(new UploadNotificationConfig()) //Notification when upload success
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {
                            Toast.makeText(UserUploadActivity.this, "Gagal menyimpan data", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            String res = String.valueOf(serverResponse);
                            Log.e("Response message", res);
//                            if (res.equals("success"))
//                                Toast.makeText(UserUploadActivity.this, "Data tersimpan", Toast.LENGTH_LONG).show();
//                            else
//                                Toast.makeText(UserUploadActivity.this, "Upload gagal", Toast.LENGTH_LONG).show();

                            finish();
                            Intent intent = new Intent(UserUploadActivity.this, UserDetailActivity.class);
                            intent.putExtra("latitude", Latitude);
                            intent.putExtra("longitude", Longitude);
                            intent.putExtra("name", RoomName);
                            intent.putExtra("floor", Floor);
                            intent.putExtra("id", IdClass);
                            intent.putExtra("address", Address);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {

                        }
                    })
                    .startUpload(); //Starting the upload
        } catch (Exception exc) {
            Toast.makeText(this, exc.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void ClosePage(View view) {
        finish();
    }

    public void SavePost(View view) {
        if (uploaded_file) {
            String filePath = String.valueOf(resizedPath.getAbsolutePath());
            uploadMultipart(filePath);
        } else {
            Toast.makeText(this, "Anda belum memasukkan foto", Toast.LENGTH_LONG).show();
        }
    }
}
