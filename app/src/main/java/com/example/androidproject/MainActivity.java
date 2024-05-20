package com.example.androidproject;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    String DB_PATH_SUFFIX = "/databases/";
    SQLiteDatabase database=null;
    String DATABASE_NAME="qlSP.db";
    EditText edtUserName, edtPassword;
    TextView txtToRegisterActivity, txtForgetPassword;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainInViewProduct), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Xóa CSDL trong device
//        deleteDatabase("qlSP.db");
        //Copy CSDL từ asset vào device
        processCopy();
        edtUserName = findViewById(R.id.UserName);
        edtPassword = findViewById(R.id.PassWord);
        txtForgetPassword = findViewById(R.id.txtForgetPassword);
        edtUserName.setText("");
        edtPassword.setText("");
        btnLogin = findViewById(R.id.btnLogin);
        txtToRegisterActivity = findViewById(R.id.txtToRegisterActivity);
        database = openOrCreateDatabase("qlSP.db", MODE_PRIVATE, null);

        //Xử lý sự kiện khi người dùng yêu cầu đăng ký -> chuyển đến activity đăng ký
        txtToRegisterActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setAction("FromMain");
                startActivity(intent);
            }
        });

        txtForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
            }
        });

        //Khi người dùng đăng nhập
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = edtUserName.getText().toString().trim();
                String passWord = edtPassword.getText().toString();
                //Xác định là người dùng khi đã đăng nhập thành công
                boolean isUser = false;
                //Phân quyền người dùng
                int role = -1;
                //Khi người dùng không nhập đủ các trường thông tin
                if(userName.isEmpty()){
                    edtUserName.setError("Vui lòng nhập tên đăng nhập/Email");
                    edtUserName.requestFocus();
                } else if(passWord.isEmpty()){
                    edtPassword.setError("Vui lòng nhập mật khẩu");
                    edtPassword.requestFocus();
                }
                //Nhập đủ
                else{
                    Cursor c = database.query("Account", null, null, null, null, null, null);
                    //Duyệt danh sách Account từ CSDL
                    c.moveToFirst();
                    while (c.isAfterLast() == false){
                        if((c.getString(0).trim().equals(userName) || c.getString(3).trim().equals(userName)) && c.getString(1).trim().equals(passWord)){
                            isUser = true;
                            role = c.getInt(2);
                            Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            userName = c.getString(0).trim();
                            break;
                        }
                        else{
                            c.moveToNext();
                        }
                    }
                    c.close();
                    if(isUser == true){
                        //Role == 1 -> khách hàng -> vào HomepageActivity
                        if(role == 1){
                            Intent intent = new Intent(MainActivity.this, HomePageActivity.class);
                            intent.putExtra("userName", userName);
                            startActivity(intent);
                        }
                        else {
//                            Intent intent = new Intent(MainActivity.this, ManagementHomepageActivity.class);
//                            startActivity(intent);
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Thông tin đăng nhập không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    public void showMessage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_forget_password, null);
        builder.setView(dialogView);
        TextView txtCancelInForgetPassword = dialogView.findViewById(R.id.txtCancelInForgetPassword);
        TextView txtSubmitInForgetPassword = dialogView.findViewById(R.id.txtSubmitInForgetPassword);
        EditText edtEmailInForgetPassword = dialogView.findViewById(R.id.edtEmailInForgetPassword);
        AlertDialog dialog2 = builder.create();
        txtCancelInForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {dialog2.dismiss();}
        });
        txtSubmitInForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmailInForgetPassword.getText().toString().trim();
                if(email.isEmpty()){
                    edtEmailInForgetPassword.setError("Vui lòng nhập email");
                    edtEmailInForgetPassword.requestFocus();
                    return;
                }
                else{
                    try {
                        Cursor cursor = database.query("Account", null, "Email = ?", new String[]{email}, null, null, null);
                        if(!cursor.moveToFirst()){
                            edtEmailInForgetPassword.setError("Email không tồn tại");
                            edtEmailInForgetPassword.requestFocus();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Đã gửi mã OTP đến địa chỉ email của bạn", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();
                    }
                    catch (Exception e){
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                dialog2.dismiss();
            }
        });
        dialog2.show();

    }


    //Copy cơ sở dữ liệu từ thư mục assets vào project
    private void processCopy() {
//private app
        File dbFile = getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists())
        {
            try{CopyDataBaseFromAsset();
                Toast.makeText(this, "Copying sucess from Assets folder",
                        Toast.LENGTH_LONG).show();
            }
            catch (Exception e){
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private String getDatabasePath() {
        return getApplicationInfo().dataDir + DB_PATH_SUFFIX+ DATABASE_NAME;
    }
    public void CopyDataBaseFromAsset() {
        try {
            InputStream myInput;
            myInput = getAssets().open(DATABASE_NAME);
// Path to the just created empty db
            String outFileName = getDatabasePath();
// if the path doesn't exist first, create it
            File f = new File(getApplicationInfo().dataDir + DB_PATH_SUFFIX);
            if (!f.exists())
                f.mkdir();
// Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);
// transfer bytes from the inputfile to the outputfile
// Truyền bytes dữ liệu từ input đến output
            int size = myInput.available();
            byte[] buffer = new byte[size];
            myInput.read(buffer);
            myOutput.write(buffer);
// Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}