package com.vuducminh.nicefood;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.vuducminh.nicefood.common.Common;
import com.vuducminh.nicefood.common.CommonAgr;
import com.vuducminh.nicefood.model.UserModel;
import com.vuducminh.nicefood.remote.ICloudFunction;
import com.vuducminh.nicefood.remote.RetrofitICloudClient;


import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private List<AuthUI.IdpConfig> providers;

    private DatabaseReference userRef;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

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

        Places.initialize(this,getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);


        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());
        userRef = FirebaseDatabase.getInstance().getReference(CommonAgr.USER_REFERENCES);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
//        cloudFunctions = RetrofitICloudClient.getInstance(Common.currentRestaurant.getPaymentUrl()).create(ICloudFunction.class);
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                Dexter.withActivity(MainActivity.this)
                        .withPermissions(Arrays.asList(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        )
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if(report.areAllPermissionsGranted()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if(user  != null)
                                        checkUserFromFireBase(user);
                                    else
                                        phoneLogin();
                                }
                                else
                                    Toast.makeText(MainActivity.this,"You must accept all permissions",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                            }
                        })
                        .check();

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

                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            Toast.makeText(MainActivity.this, "Welcome back, "+userModel.getName() , Toast.LENGTH_SHORT).show();
                            goToHomeActivity(userModel);

//                            compositeDisposable.add(iCloudFunction.getToken()
//                            .subscribeOn(Schedulers.io())
//                            .observeOn(AndroidSchedulers.mainThread())
//                            .subscribe(new Consumer<BraintreeToken>() {
//                                @Override
//                                public void accept(BraintreeToken braintreeToken) throws Exception {
//                                    dialog.dismiss();
//                                    UserModel userModel = dataSnapshot.getValue(UserModel.class);
//                                    goToHomeActivity(userModel,braintreeToken.getToken());
//                                }
//                            }, new Consumer<Throwable>() {
//                                @Override
//                                public void accept(Throwable throwable) throws Exception {
//                                    dialog.dismiss();
//                                    Toast.makeText(MainActivity.this,""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
//                                }
//                            }));
                        } else {
                            showRegisterDialog(user);
//                            dialog.dismiss();
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
        TextInputLayout phone_input_layout = (TextInputLayout)itemView.findViewById(R.id.phone_input_layout);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        TextView tv_address_detail = (TextView)itemView.findViewById(R.id.tv_address_detail);

        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        places_fragment = (AutocompleteSupportFragment)getSupportFragmentManager()
        .findFragmentById(R.id.places_autocomplete_fragment);

        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                tv_address_detail.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this,""+status.getStatusMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        if(user.getPhoneNumber() == null || TextUtils.isEmpty(user.getPhoneNumber())) {
            phone_input_layout.setHint("Email");
            edt_phone.setText(user.getEmail());
            edt_name.setText(user.getDisplayName());
        }
        else {
            phone_input_layout.setHint("Phone");
            edt_phone.setText(user.getPhoneNumber());
        }

        // Dổ dữ liệu và bắt sự kiện


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
                if(placeSelected != null) {
                    if (TextUtils.isEmpty(tv_address_detail.getText().toString())) {
                        Toast.makeText(MainActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserModel userModel = new UserModel();
                    userModel.setUid(user.getUid());
                    userModel.setName(edt_name.getText().toString());
                    userModel.setAddress(tv_address_detail.getText().toString());
                    userModel.setPhone(edt_phone.getText().toString());
                    userModel.setLat(placeSelected.getLatLng().latitude);
                    userModel.setLng(placeSelected.getLatLng().longitude);

                    userRef.child(user.getUid()).setValue(userModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
//                                    compositeDisposable.add(iCloudFunction.getToken()
//                                            .subscribeOn(Schedulers.io())
//                                            .observeOn(AndroidSchedulers.mainThread())
//                                            .subscribe(new Consumer<BraintreeToken>() {
//                                                @Override
//                                                public void accept(BraintreeToken braintreeToken) throws Exception {
//                                                    dialogInterface.dismiss();
//                                                    Toast.makeText(MainActivity.this,"Congratulation ! Register success",Toast.LENGTH_SHORT).show();
//                                                    goToHomeActivity(userModel,braintreeToken.getToken());
//                                                }
//                                            }, new Consumer<Throwable>() {
//                                                @Override
//                                                public void accept(Throwable throwable) throws Exception {
//                                                    dialog.dismiss();
//                                                    Toast.makeText(MainActivity.this,""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
//                                                }
//                                            }));//                                    compositeDisposable.add(iCloudFunction.getToken()
//                                            .subscribeOn(Schedulers.io())
//                                            .observeOn(AndroidSchedulers.mainThread())
//                                            .subscribe(new Consumer<BraintreeToken>() {
//                                                @Override
//                                                public void accept(BraintreeToken braintreeToken) throws Exception {
//                                                    dialogInterface.dismiss();
//                                                    Toast.makeText(MainActivity.this,"Congratulation ! Register success",Toast.LENGTH_SHORT).show();
//                                                    goToHomeActivity(userModel,braintreeToken.getToken());
//                                                }
//                                            }, new Consumer<Throwable>() {
//                                                @Override
//                                                public void accept(Throwable throwable) throws Exception {
//                                                    dialog.dismiss();
//                                                    Toast.makeText(MainActivity.this,""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
//                                                }
//                                            }));
                                        dialogInterface.dismiss();
                                        Toast.makeText(MainActivity.this,"Congratulation ! Register success",Toast.LENGTH_SHORT).show();
                                        goToHomeActivity(userModel);
                                    }
                                }
                            });
                }
                else {
                    Toast.makeText(MainActivity.this,"Please select address",Toast.LENGTH_SHORT).show();
                    return;
                }
            }

        });


        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialog1 -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel) {

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {

                    Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();

                })
                .addOnCompleteListener(task -> {
                    Common.currentUser = userModel;
                    Common.updateToken(MainActivity.this,task.getResult().getToken());
                    startActivity(new Intent(MainActivity.this,HomeActivity.class));
                    finish();
                });


    }

//    private void goToHomeActivity(UserModel userModel,String token) {
//        Common.currentUser = userModel;
//        Common.currentToken = token;
//        startActivity(new Intent(MainActivity.this,HomeActivity.class));
//
//        finish();
//
//    }

    private void phoneLogin() {

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.logo)
                .setTheme(R.style.LoginTheme)
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
