package com.example.androidproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText UserNameInRegister, PasswordInRegister, RePasswordInRegister, EmailInRegister, PhoneNumberInRegister;
    String email = null, userName = null, passWord = null, rePW = null, phoneNumber = null;
    Button btnRegister;
    TextView txtToMainActivity;
    FirebaseAuth db = FirebaseAuth.getInstance();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    AccountService accountService = new AccountService();
    private String verificationId;
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
        PhoneNumberInRegister = findViewById(R.id.PhoneNumberInRegister);

        //Xử lý sự kiện khi người dùng xác nhận đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = EmailInRegister.getText().toString();
                userName = UserNameInRegister.getText().toString();
                passWord = PasswordInRegister.getText().toString();
                String passWordHash = PasswordUtils.encryptPassword(passWord);
                rePW = RePasswordInRegister.getText().toString();
                phoneNumber = PhoneNumberInRegister.getText().toString();
                String formatedPhoneNumber = "+84" + phoneNumber.substring(1);
                //Khi người dùng không nhập đủ thông tin
                if (email.isEmpty()) {
                    EmailInRegister.setError("Vui lòng nhập email");
                    EmailInRegister.requestFocus();
                    return;
                } else if (PhoneNumberInRegister.getText().toString().isEmpty()) {
                    PhoneNumberInRegister.setError("Vui lòng nhập số điện thoại");
                    PhoneNumberInRegister.requestFocus();
                    return;
                } else if (!isValidEmail(email)) {
                    EmailInRegister.setError("Email không đúng định dạng");
                    EmailInRegister.requestFocus();
                    return;
                } else if (userName.isEmpty()) {
                    UserNameInRegister.setError("Vui lòng nhập tên đăng nhập");
                    UserNameInRegister.requestFocus();
                    return;
                } else if (passWord.isEmpty()) {
                    PasswordInRegister.setError("Vui lòng nhập mật khẩu");
                    PasswordInRegister.requestFocus();
                    return;
                } else if (rePW.isEmpty()) {
                    RePasswordInRegister.setError("Vui lòng xác nhận mật khẩu");
                    RePasswordInRegister.requestFocus();
                    return;
                }
                //Nhập đủ
                else {
                    CompletableFuture<Boolean> checkUserNameFuture = accountService.checkUserNameExists(userName);
                    CompletableFuture<Boolean> checkEmailFuture = accountService.checkEmailExists(email);
                    CompletableFuture<Boolean> checkPhoneNumberFuture = accountService.checkPhoneNumberExists(phoneNumber);

                    CompletableFuture.allOf(checkUserNameFuture, checkEmailFuture, checkPhoneNumberFuture).thenAcceptAsync(voids -> {
                        boolean userNameExists = checkUserNameFuture.join();
                        boolean emailExists = checkEmailFuture.join();
                        boolean phoneNumberExists = checkPhoneNumberFuture.join();

                        // Handle results accordingly
                        runOnUiThread(() -> {
                            if (userNameExists) {
                                Toast.makeText(RegisterActivity.this, "UserName đã tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "UserName chưa tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                            }

                            if (emailExists) {
                                Toast.makeText(RegisterActivity.this, "Email đã tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Email chưa tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                            }

                            if (phoneNumberExists) {
                                Toast.makeText(RegisterActivity.this, "Số điện thoại đã tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Số điện thoại chưa tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                            }
                        });

                        //Kiểm tra sau khi userName xác định không trùng lặp
                        //Nhập lại mật khẩu sai
                        if (!passWord.equals(rePW)) {
                            RePasswordInRegister.setError("Mật khẩu phải khớp");
                            RePasswordInRegister.requestFocus();
                            return;
                        }
                        //Nếu đúng, tạo user và giỏ hàng mới cho user đó
                        else {
                            CompletableFuture<Void> sendOTPAndCreateAccountFuture = new CompletableFuture<>();

                            // Gửi OTP và Tạo tài khoản mới
                            String accountID = firebaseDatabase.getReference("Account").push().getKey();
                            Account account = new Account(accountID, userName, passWordHash, email, phoneNumber, 1, false);
                            firebaseDatabase.getReference("Account").child(accountID).setValue(account)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // Tạo thành công tài khoản
                                            sendOTPAndCreateAccountFuture.complete(null);
                                        } else {
                                            // Xử lý khi tạo tài khoản thất bại
                                            sendOTPAndCreateAccountFuture.completeExceptionally(task.getException());
                                        }
                                    });

                            sendOTPAndCreateAccountFuture.thenAcceptAsync(voidResult -> {
                                // Tạo thành công tài khoản, tiếp tục với việc gửi OTP và tạo giỏ hàng
                                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(db)
                                        .setPhoneNumber(formatedPhoneNumber)
                                        .setTimeout(60L, TimeUnit.SECONDS)
                                        .setActivity(RegisterActivity.this)
                                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                            @Override
                                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                                Log.e("OTP", e.getMessage());
                                                Toast.makeText(RegisterActivity.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            @Override
                                            public void onCodeSent(@NonNull String verificationId,
                                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                                RegisterActivity.this.verificationId = verificationId;
                                                android.content.Intent intent = new android.content.Intent(RegisterActivity.this, OTPVerify.class);
                                                intent.putExtra("verificationId", verificationId);
                                                intent.putExtra("type", "register");
                                                intent.putExtra("phone", phoneNumber);
                                                startActivity(intent);
                                            }

                                            @Override
                                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                                // Xử lý khi OTP được xác minh tự động (nếu có)
                                            }
                                        }).build();
                                PhoneAuthProvider.verifyPhoneNumber(options);

//                                // Tạo giỏ hàng mới cho tài khoản tương ứng
//                                CartService cartService = new CartService();
//                                cartService.addCart(accountID).thenAccept(cart -> {
//                                    finish();
//                                }).exceptionally(ex -> {
//                                    // Xử lý trường hợp ngoại lệ (nếu có)
//                                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
//                                    return null;
//                                });

                            }).exceptionally(ex -> {
                                // Xử lý khi có lỗi xảy ra trong quá trình gửi OTP và tạo tài khoản
                                runOnUiThread(() -> {
                                    Toast.makeText(RegisterActivity.this, "Đã xảy ra lỗi: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                                return null;
                            });

//                            // Gửi OTP và Tạo tài khoản mới
//                            String accountID = firebaseDatabase.getReference("Account").push().getKey();
//                            Account account = new Account(accountID, userName, passWordHash, email, phoneNumber, 1, false);
//                            firebaseDatabase.getReference("Account").child(accountID).setValue(account);
//
//                            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(db)
//                                    .setPhoneNumber(formatedPhoneNumber)
//                                    .setTimeout(60L, TimeUnit.SECONDS)
//                                    .setActivity(RegisterActivity.this)
//                                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//                                        @Override
//                                        public void onVerificationFailed(@NonNull FirebaseException e) {
//                                            Log.e("OTP", e.getMessage());
//                                            Toast.makeText(RegisterActivity.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
//                                        }
//
//                                        @Override
//                                        public void onCodeSent(@NonNull String verificationId,
//                                                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
//                                            RegisterActivity.this.verificationId = verificationId;
//                                            android.content.Intent intent = new android.content.Intent(RegisterActivity.this, OTPVerify.class);
//                                            intent.putExtra("verificationId", verificationId);
//                                            intent.putExtra("phone", phoneNumber);
//                                            startActivity(intent);
//                                        }
//
//                                        @Override
//                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//
//                                        }
//                                    }).build();
//                            PhoneAuthProvider.verifyPhoneNumber(options);
//
//                            //Tạo giỏ hàng mới cho tài khoản tương ứng
//                            String cartId = firebaseDatabase.getReference("Cart").push().getKey();
//                            Cart cart = new Cart(cartId, accountID);
//                            firebaseDatabase.getReference("Cart").child(cartId).setValue(cart);
                        }
                    }).exceptionally(ex -> {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "Đã xảy ra lỗi: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                        return null;
                    });
                }
            }
        });
    }

    public static boolean isValidEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}