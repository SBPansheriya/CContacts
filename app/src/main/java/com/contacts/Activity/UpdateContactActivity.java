package com.contacts.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.contacts.Class.Constant;
import com.contacts.Fragment.ContactsFragment;
import com.contacts.Model.Users;
import com.contacts.R;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class UpdateContactActivity extends AppCompatActivity {

    ImageView cancel;
    TextView show_person_name;
    EditText update_firstname, update_lastname, update_pphone, update_ophone;
    ImageView personimage;
    String imagename, imagepath;
    Uri newUri;
    Button update_contact;
    private static final int CAMERA_REQUEST = 100;
    Users user;
    Bitmap bitmap;
    ActivityResultLauncher<Intent> launchSomeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_contact);
        Window window = UpdateContactActivity.this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(UpdateContactActivity.this, R.color.white));
        init();

        user = (Users) getIntent().getSerializableExtra("user");

        if (user.image == null) {
            personimage.setImageResource(R.drawable.person_placeholder);
        } else {
            Picasso.get().load(user.image).into(personimage);
        }

        update_firstname.setText(user.first);
        update_lastname.setText(user.last);
        update_pphone.setText(user.personPhone);
        update_ophone.setText(user.officePhone);
        show_person_name.setText(user.first + " " + user.last);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        personimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsForCamera();
            }
        });

        update_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissionsForSave();
            }
        });

        launchSomeActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    newUri = data.getData();
                    if (newUri != null) {
                        Glide.with(UpdateContactActivity.this).load(newUri).into(personimage);
                    } else {
                        if (user.image != null) {
                            Glide.with(UpdateContactActivity.this).load(user.image).into(personimage);
                        }
                    }
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
//        launchSomeActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Uri selectedImageUri = result.getData().getData();
//                        personimage.setImageURI(selectedImageUri);
////                        bitmap = null;
////                        try {
////                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
////
////                        if (bitmap != null) {
////                            personimage.setImageBitmap(bitmap);
////                        }
//                    }
//                });
    }

    public void getUpdateContactList(String contactId, String newFirstName, String newLastName, String newPersonalPhoneNumber, String newOfficePhoneNumber, Uri newImage) {

        ContentResolver contentResolver = getContentResolver();

        // Update the first phone number
        ContentValues phoneValues1 = new ContentValues();
        phoneValues1.put(ContactsContract.CommonDataKinds.Phone.NUMBER, newPersonalPhoneNumber);

        String phoneSelection1 = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Phone.TYPE + " = ?";

        String[] phoneSelectionArgs1 = new String[]{contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)};

        contentResolver.update(ContactsContract.Data.CONTENT_URI, phoneValues1, phoneSelection1, phoneSelectionArgs1);

        // Update the second phone number
        ContentValues phoneValues2 = new ContentValues();
        phoneValues2.put(ContactsContract.CommonDataKinds.Phone.NUMBER, newOfficePhoneNumber);

        String phoneSelection2 = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Phone.TYPE + " = ?";

        String[] phoneSelectionArgs2 = new String[]{contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_WORK)};

        contentResolver.update(ContactsContract.Data.CONTENT_URI, phoneValues2, phoneSelection2, phoneSelectionArgs2);


        // update name
        ContentResolver contentResolver1 = getContentResolver();
        ContentValues contactNameValues = new ContentValues();
        contactNameValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, newFirstName);
        contactNameValues.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, newLastName);

        String selection1 = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";

        String[] selectionArgs1 = new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};
        contentResolver1.update(ContactsContract.Data.CONTENT_URI, contactNameValues, selection1, selectionArgs1);

        // update photo
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);

        if (newImage != null) {
            if (user.image != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), newImage);
                    ByteArrayOutputStream image = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, image);

                    builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                    builder.withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId),
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE});
                    builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, image.toByteArray());
                    ops.add(builder.build());
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ContentValues values = new ContentValues();
                    ContentResolver contentResolver3 = getContentResolver();

                    bitmap = null;

                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), newImage);
                    ByteArrayOutputStream image = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, image);
                    values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
                    values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, image.toByteArray());
                    contentResolver3.insert(ContactsContract.Data.CONTENT_URI, values);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            user.image = newImage.toString();
        }
        user.first = newFirstName;
        user.last = newLastName;
        user.personPhone = newPersonalPhoneNumber;
        user.officePhone = "";

        for (int i = 0; i < Constant.usersArrayList.size(); i++) {

            if (Constant.usersArrayList.get(i).contactId.equalsIgnoreCase(contactId)) {
                Constant.usersArrayList.remove(i);
                Constant.usersArrayList.add(i, user);
                break;
            }
        }

        Toast.makeText(UpdateContactActivity.this, "Contact saved", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void openImagePicker() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    private void dialog() {
        Dialog dialog = new Dialog(UpdateContactActivity.this);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(false);
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.dialog_camera_and_gallery);
        dialog.setCancelable(false);
        dialog.show();

        ImageView camera = dialog.findViewById(R.id.camera);
        ImageView gallery = dialog.findViewById(R.id.gallery);
        Button cancel1 = dialog.findViewById(R.id.cancel);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraPermission();
                dialog.dismiss();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
                dialog.dismiss();
            }
        });

        cancel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void updatedData() {
        imagepath = imagepath + "/" + imagename;
        String firstname = update_firstname.getText().toString();
        String lastname = update_lastname.getText().toString();
        String pphone = update_pphone.getText().toString();
        String ophone = update_ophone.getText().toString();

        if (TextUtils.isEmpty(firstname) || TextUtils.isEmpty(pphone)) {
            Toast.makeText(UpdateContactActivity.this, "Please Fill Data", Toast.LENGTH_SHORT).show();
        } else {
            getUpdateContactList(user.contactId, firstname, lastname, pphone, ophone, newUri);
            Toast.makeText(UpdateContactActivity.this, "Contact saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPermissionsForSave() {

        String[] permissions = new String[]{Manifest.permission.WRITE_CONTACTS};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(UpdateContactActivity.this, permissions, 101);
        } else {
            updatedData();
        }
    }

    private void cameraPermission() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST);
    }

    private void checkPermissionsForCamera() {

        String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(UpdateContactActivity.this, permissions, 100);
        } else {
            dialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            dialog();
        } else if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updatedData();
        } else {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        Dialog dialog = new Dialog(UpdateContactActivity.this);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.CENTER);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(false);
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setContentView(R.layout.dialog_permission);
        dialog.setCancelable(false);
        dialog.show();

        Button gotosettings = dialog.findViewById(R.id.gotosettings);

        gotosettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    checkPermissionsForCamera();
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {
                    checkPermissionsForSave();
                } else {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 100);
                    Toast.makeText(UpdateContactActivity.this, "Setting", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            personimage.setImageBitmap(bitmap);
            newUri = saveToInternalStorage(bitmap);
            imagepath = newUri.getPath();
        }
    }

    private Uri saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        imagename = "img" + new Random().nextInt(100000) + ".png";
        File mypath = new File(directory, imagename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Uri.fromFile(mypath);
    }

    private void init() {
        cancel = findViewById(R.id.update_cancel);
        personimage = findViewById(R.id.update_image);
        update_firstname = findViewById(R.id.update_firstname);
        update_lastname = findViewById(R.id.update_lastname);
        update_pphone = findViewById(R.id.update_pphone);
        update_ophone = findViewById(R.id.update_ophone);
        update_contact = findViewById(R.id.update_contact);
        show_person_name = findViewById(R.id.show_personName);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("user", user);
        setResult(RESULT_OK, intent);
        finish();
    }
}