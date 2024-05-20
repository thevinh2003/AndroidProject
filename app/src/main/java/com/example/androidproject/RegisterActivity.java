package com.example.androidproject;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText UserNameInRegister, PasswordInRegister, RePasswordInRegister, EmailInRegister;
    String email = null, userName = null, passWord = null, rePW = null;
    Button btnRegister;
    TextView txtToMainActivity;
    SQLiteDatabase database = null;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Ánh xạ ID
        UserNameInRegister = findViewById(R.id.UserNameInRegister);
        PasswordInRegister = findViewById(R.id.PassWordInRegister);
        RePasswordInRegister = findViewById(R.id.RePassWordInRegister);
        txtToMainActivity = findViewById(R.id.txtToMainActivity);
        btnRegister = findViewById(R.id.btnRegister);
        EmailInRegister = findViewById(R.id.EmailInRegister);

        //Xử lý sự kiện khi người dùng xác nhận đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = EmailInRegister.getText().toString();
                userName = UserNameInRegister.getText().toString();
                passWord = PasswordInRegister.getText().toString();
                rePW = RePasswordInRegister.getText().toString();
                //Khi người dùng không nhập đủ thông tin
                if (email.isEmpty()) {
                    EmailInRegister.setError("Vui lòng nhập email");
                    EmailInRegister.requestFocus();
                    return;
                } else if (!isValidEmail(email)){
                    EmailInRegister.setError("Email không đúng định dạng");
                    EmailInRegister.requestFocus();
                    return;
                } else if(userName.isEmpty()){
                    UserNameInRegister.setError("Vui lòng nhập tên đăng nhập");
                    UserNameInRegister.requestFocus();
                    return;
                } else if(passWord.isEmpty()){
                    PasswordInRegister.setError("Vui lòng nhập mật khẩu");
                    PasswordInRegister.requestFocus();
                    return;
                } else if (rePW.isEmpty()){
                    RePasswordInRegister.setError("Vui lòng xác nhận mật khẩu");
                    RePasswordInRegister.requestFocus();
                    return;
                }
                //Nhập đủ
                else{
                    Boolean check = true;
                    //userNameList chứa các userName có sẵn trong database
                    ArrayList<String> userNameList, emailList;
                    userNameList = new ArrayList<>();
                    emailList = new ArrayList<>();
                    database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);
                    Cursor c = database.query("Account", null, null, null, null, null, null);
                    c.moveToFirst();
                    while (c.isAfterLast() == false){
                        userNameList.add(c.getString(0));
                        emailList.add(c.getString(3));
                        c.moveToNext();
                    }
                    c.close();
                    //Kiểm tra trùng lặp userName
                    if(emailList.contains(email)) {
                        EmailInRegister.setError("Email đã tồn tại");
                        EmailInRegister.requestFocus();
                        check = false;
                        return;
                    } else if(userNameList.contains(userName)){
                        UserNameInRegister.setError("Tên đăng nhập đã tồn tại");
                        UserNameInRegister.requestFocus();
                        check = false;
                        return;
                    }
                    //Kiểm tra sau khi userName xác định không trùng lặp
                    if(check == true){
                        //Nhập lại mật khẩu sai
                        if(!passWord.equals(rePW)){
                            RePasswordInRegister.setError("Mật khẩu phải khớp");
                            RePasswordInRegister.requestFocus();
                        }
                        //Nếu đúng, tạo user và giỏ hàng mới cho user đó
                        else{
                            //Tạo tài khoản mới
                            ContentValues values = new ContentValues();
                            values.put("Email", email);
                            values.put("UserName", userName);
                            values.put("PassWord", passWord);
                            values.put("Role", 1);
                            database.insert("Account", null, values);

                            //Tạo giỏ hàng mới cho tài khoản tương ứng
                            ContentValues value2 = new ContentValues();
                            value2.put("UserName", userName);
                            database.insert("Cart", null, value2);
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        });

        //Chuyển về LoginActivity
        txtToMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    public static boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}