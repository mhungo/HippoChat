package vn.edu.stu.hippochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingInfoActivity extends AppCompatActivity {

    private CircleImageView userProFileImage;
    private EditText username, fullname;
    private Button buttonUpdateProfile;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;

    private static final int GalaryPick = 1;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    private Toolbar toolbarSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_info);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

        InitializeFields();

        username.setVisibility(View.INVISIBLE);
        fullname.setVisibility(View.INVISIBLE);

        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSetting();
            }
        });

        RetrieveUserInfo();

        userProFileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galaryIntent = new Intent();
                galaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galaryIntent.setType("image/*");
                startActivityForResult(galaryIntent,GalaryPick);
            }
        });


    }

    private void InitializeFields() {
        userProFileImage = findViewById(R.id.set_profile_image);
        username = findViewById(R.id.set_user_name);
        fullname = findViewById(R.id.set_user_full_name);
        buttonUpdateProfile = findViewById(R.id.update_settings_button);

        progressDialog = new ProgressDialog(this);

        toolbarSetting = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbarSetting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Thông tin cá nhân");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalaryPick && requestCode == RESULT_OK && data != null) {

            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (requestCode == RESULT_OK) {
                progressDialog.setTitle("Đăng cập nhật ảnh đại diện");
                progressDialog.setMessage("Vui lòng đợi trong giây lát...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference filepath = storageReference.child(currentUserID + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingInfoActivity.this, "Tải hoàn tất", Toast.LENGTH_SHORT).show();
                            final String downloadURL = task.getResult().getUploadSessionUri().toString();

                            reference.child("Users").child(currentUserID).child("imageURL")
                                    .setValue(downloadURL)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingInfoActivity.this, "Đã lưu vào CSDL", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                            else{
                                                Toast.makeText(SettingInfoActivity.this, "Lỗi trong quá trình lưu, thử lại sau...", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });


                        }
                        else{
                            String error = task.getException().toString();
                            Toast.makeText(SettingInfoActivity.this, "Lỗi " + error, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSetting(){

        String Username = username.getText().toString();
        String Fullname = fullname.getText().toString();

        if(TextUtils.isEmpty(Username)){
            Toast.makeText(this, "Vui lòng nhập username", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Fullname)){
            Toast.makeText(this, "Vui lòng nhập full name", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("id", currentUserID);
            hashMap.put("username",Username);
            hashMap.put("fullname", Fullname);
            reference.child("Users").child(currentUserID).updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SentToMainActivity();
                                Toast.makeText(SettingInfoActivity.this, "Cập nhật thông tin thành công...", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String error = task.getException().toString();
                                Toast.makeText(SettingInfoActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void RetrieveUserInfo(){

        reference.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("username")) && (dataSnapshot.hasChild("imageURL"))){
                            String retrieveusername = dataSnapshot.child("username").getValue().toString();
                            String retrievefullname = dataSnapshot.child("fullname").getValue().toString();
                            String retrieveimage = dataSnapshot.child("imageURL").getValue().toString();

                            username.setText(retrieveusername);
                            fullname.setText(retrievefullname);
                            Picasso.get().load(retrieveimage).into(userProFileImage);
                        }
                        else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("username"))){
                            String retrieveusername = dataSnapshot.child("username").getValue().toString();
                            String retrievefullname = dataSnapshot.child("fullname").getValue().toString();

                            username.setText(retrieveusername);
                            fullname.setText(retrievefullname);
                        }
                        else{
                            username.setVisibility(View.VISIBLE);
                            fullname.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingInfoActivity.this, "Vui lòng điền thông tin và cập nhật thông tin cá nhân", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void SentToMainActivity() {
        Intent mainIntent = new Intent(SettingInfoActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}






