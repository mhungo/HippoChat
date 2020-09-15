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

public class StartActivity extends AppCompatActivity {

    private EditText email, password;
    private Button btnLogin;
    private TextView forGotPass, createNewAccount;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        InitializeFields();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = email.getText().toString();
                String Pass = password.getText().toString();

                if(TextUtils.isEmpty(Email) || TextUtils.isEmpty(Pass)){
                    Toast.makeText(StartActivity.this, "Vui lòng nhập Email và Password", Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog.setTitle("Đang đăng nhập...");
                    progressDialog.setMessage("Vui lòng đợi trong giây lát");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    loginUserAccount(Email,Pass);
                }
            }
        });

        createNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SentToRegisterActivity();
            }
        });


    }

    private void loginUserAccount(String Email, String Password) {
        mAuth.signInWithEmailAndPassword(Email,Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(StartActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            SentoMainActivity();
                        }
                        else{
                            Toast.makeText(StartActivity.this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });

    }

    private void SentoMainActivity() {
        Intent intent = new Intent(StartActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void InitializeFields() {
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.login_button);
        forGotPass = findViewById(R.id.forget_password_link);
        createNewAccount = findViewById(R.id.need_new_account_link);
    }
    private void SentToRegisterActivity(){
        Intent intent = new Intent(StartActivity.this,RegisterActivity.class);
        startActivity(intent);
        finish();
    }
    private void SentToForGotPasswordActivity(){

    }
}