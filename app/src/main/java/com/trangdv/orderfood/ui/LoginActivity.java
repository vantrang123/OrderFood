package com.trangdv.orderfood.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.main.MainActivity;
import com.trangdv.orderfood.utils.DialogUtils;
import com.trangdv.orderfood.utils.SharedPrefs;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class LoginActivity extends AppCompatActivity {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public static final int REQUEST_CODE = 2019;
    public static final String KEY_PHONENUMBER = "key phonenumber address";
    public static final String KEY_PASSWORD = "key password";
    public static final String SAVE_USER = "save user";
    private static final String TAG = "LoginActivity";

    private TextView dispatch_signup;
    private EditText edt_phonenumber;
    private EditText edt_password;
    private FloatingActionButton fab;

    private String phonenumber;
    private String password;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference table_user = database.getReference("User");

    DialogUtils dialogUtils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inits();
    }

    private void inits() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);

        dispatch_signup = findViewById(R.id.dispatch_signup);
        setClickDispatchSignup();
        fab = findViewById(R.id.fab_login);
        setOnClickFab();
        edt_phonenumber = findViewById(R.id.phonenumber_edt_login);
        edt_password = findViewById(R.id.password_edt_login);

        dialogUtils = new DialogUtils();

    }

    private void setClickDispatchSignup() {
        dispatch_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchSignup();
            }
        });
    }

    private void dispatchSignup() {
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
//        startActivityForResult(intent, REQUEST_CODE);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            phonenumber = data.getExtras().getString(KEY_PHONENUMBER, "");
            password = data.getExtras().getString(KEY_PASSWORD, "");

            setTextintoEdt();

        }
    }

    private void setOnClickFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTextfromEdt();

                if (phonenumber.equals("")==false && password.equals("")==false) {
                    dialogUtils.showProgress(LoginActivity.this);
                    authLogin();

                }

            }
        });
    }

    private void getTextfromEdt() {
        phonenumber = edt_phonenumber.getText().toString();
        password = edt_password.getText().toString();
    }

    private void authLogin() {
//        table_user.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //check if user not exist in firebaseDatabase
//                if (dataSnapshot.child(phonenumber).exists()) {
//                    User user = dataSnapshot.child(phonenumber).getValue(User.class);
//           }         user.setPhone(phonenumber);
////
////                    if (user.getPassword().equals(password)) {
////                        SharedPrefs.getInstance().put(SplashActivity.CHECK_ALREADLY_LOGIN, 1);
////
////                        //save user in share pref
////                        SharedPrefs.getInstance().put(SAVE_USER, user);
////                        intoHome(user);
////                    } else {
////                        Toast.makeText(LoginActivity.this, "Wrong Password !", Toast.LENGTH_SHORT).show();
////                    }
////                } else {
////                    Toast.makeText(LoginActivity.this, "User not exist in Database !", Toast.LENGTH_SHORT).show();
////                }
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError databaseError) {
////
////
//        });

            compositeDisposable.add(
                    anNgonAPI.getUser(Common.API_KEY, "383170518961862")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(userModel -> {
                                        if (userModel.isSuccess()) {
                                            // save curreentUser
                                            Common.currentUser = userModel.getResult().get(0);

                                            SharedPrefs.getInstance().put(SplashActivity.CHECK_ALREADLY_LOGIN, 2);

                                            //save user in share pref
                                            SharedPrefs.getInstance().put(SAVE_USER, Common.currentUser);

                                            Toast.makeText(this, "[GET USER API SUCCESS]" + Common.currentUser.getUserPhone(), Toast.LENGTH_SHORT).show();

                                            gotoMainActivity();

                                        } else {
                                            Toast.makeText(this, "[GET USER API NOT DATABASE]", Toast.LENGTH_SHORT).show();
                                        }
                                        dialogUtils.dismissProgress();

                                    },
                                    throwable -> {
                                        Toast.makeText(this, "[GET USER API]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        dialogUtils.dismissProgress();
                                    }
                            ));
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);

        startActivity(intent);
        finish();
    }

    private void setTextintoEdt() {
        edt_phonenumber.setText(phonenumber);
        edt_password.setText(password);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    //
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
