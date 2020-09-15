package vn.edu.stu.hippochat;

import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    Button CreateAccountButton;
    EditText UserEmail, UserPassword, UserName;
    TextView DaCoTaiKhoan;

    FirebaseAuth mAuth;
    DatabaseReference reference;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        InitializeFields();

        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt_username = UserName.getText().toString();
                String txt_email = UserEmail.getText().toString();
                String txt_password = UserPassword.getText().toString();

                if (TextUtils.isEmpty(txt_email)) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(RegisterActivity.this, "Bạn chưa nhập mật khẩu", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Đang tạo tài khoản mới");
                    loadingBar.setMessage("Vui lòng đợi trong giât lát!");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    CreateNewAccount(txt_username, txt_email, txt_password);

                }
            }
        });

        DaCoTaiKhoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SentToStartActivity();
            }
        });

    }

    private void CreateNewAccount(final String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            String userid = user.getUid();

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("imageURL", "Chưa cập nhật");
                            hashMap.put("username", username);
                            hashMap.put("fullname", "Chưa cập nhập");
                            hashMap.put("status", "offline");
                            hashMap.put("search", username.toLowerCase());

                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Đăng kí thành công", Toast.LENGTH_SHORT).show();
                                        SentToSettingActivity();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RegisterActivity.this, "Đăng kí không thành công, thử lại sau...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void InitializeFields() {
        CreateAccountButton = findViewById(R.id.register_button);
        UserName = findViewById(R.id.register_username);
        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        DaCoTaiKhoan = findViewById(R.id.register_da_co_tai_khoan);

    }

    private void SentToStartActivity() {
        Intent intent = new Intent(RegisterActivity.this, StartActivity.class);
        startActivity(intent);
    }

    private void SentToSettingActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }


}



