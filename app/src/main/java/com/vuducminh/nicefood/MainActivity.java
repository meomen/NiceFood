package com.vuducminh.nicefood;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefood.Common.Common;
import com.vuducminh.nicefood.Common.CommonAgr;
import com.vuducminh.nicefood.Model.UserModel;
import com.vuducminh.nicefood.Remote.ICloudFunction;
import com.vuducminh.nicefood.Remote.RetrofitICloudClient;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ICloudFunction iCloudFunction;
    private List<AuthUI.IdpConfig> providers;

    private DatabaseReference userRef;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    // Khởi tạo weight
    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
        userRef = FirebaseDatabase.getInstance().getReference(CommonAgr.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        iCloudFunction = RetrofitICloudClient.getInstance().create(ICloudFunction.class);
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    checkUserFromFireBase(user);
                } else {
                    phoneLogin();
                }
            }
        };
    }

    private void checkUserFromFireBase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(MainActivity.this, "You alredy register", Toast.LENGTH_SHORT).show();

                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);
                        } else {
                            showRegisterDialog(user);
                        }

                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Register");
        builder.setMessage("Please fill information");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_address = (EditText) itemView.findViewById(R.id.edt_address);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        // Dổ dữ liệu và bắt sự kiện
        edt_phone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        builder.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(edt_address.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserModel userModel = new UserModel();
                userModel.setUid(user.getUid());
                userModel.setName(edt_name.getText().toString());
                userModel.setAddress(edt_address.getText().toString());
                userModel.setPhone(edt_phone.getText().toString());

                userRef.child(user.getUid()).setValue(userModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {

                                    dialogInterface.dismiss();
                                    Toast.makeText(MainActivity.this,"Congratulation ! Register success",Toast.LENGTH_SHORT).show();
                                    goToHomeActivity(userModel);
                                }
                            }
                        });
            }
        });


        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel) {
        Common.currentUser = userModel;
    }

    private void phoneLogin() {

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
                , CommonAgr.APP_REQUEST_CODE);
    }



    private void signInWIthCustomToken(String customToken) {
        dialog.dismiss();
        firebaseAuth.signInWithCustomToken(customToken)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonAgr.APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(MainActivity.this,"Failed to sign in!",Toast.LENGTH_SHORT).show();
            }
        }
    }

}
