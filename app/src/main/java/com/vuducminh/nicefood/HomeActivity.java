package com.vuducminh.nicefood;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.andremion.counterfab.CounterFab;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefood.Common.Common;
import com.vuducminh.nicefood.Common.CommonAgr;
import com.vuducminh.nicefood.Database.CartDataSource;
import com.vuducminh.nicefood.Database.CartDatabase;
import com.vuducminh.nicefood.Database.LocalCartDataSource;
import com.vuducminh.nicefood.EventBus.BestDealItemClick;
import com.vuducminh.nicefood.EventBus.CategoryClick;
import com.vuducminh.nicefood.EventBus.CountCartEvent;
import com.vuducminh.nicefood.EventBus.FoodItemClick;
import com.vuducminh.nicefood.EventBus.HideFABCart;
import com.vuducminh.nicefood.EventBus.PopluarCategoryClick;
import com.vuducminh.nicefood.Model.CategoryModel;
import com.vuducminh.nicefood.Model.FoodModel;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private CartDataSource cartDataSource;

    private android.app.AlertDialog dialog;

    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume() {
        super.onResume();
        coutCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);
            }
        });
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,
                R.id.nav_tools, R.id.nav_cart, R.id.nav_food_list)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView tv_user = (TextView) headerView.findViewById(R.id.tv_user);
        Common.setSpanString("Hey, ",Common.currentUser.getName(),tv_user);

        coutCartItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(true);
        drawer.closeDrawers();
        switch (item.getItemId()) {
            case R.id.nav_home: {
                navController.navigate(R.id.nav_home);
                break;
            }
            case R.id.nav_menu: {
                navController.navigate(R.id.nav_menu);
                break;
            }
            case R.id.nav_cart: {
                navController.navigate(R.id.nav_cart);
                break;
            }
            case R.id.nav_sign_out: {
                signoOut();
                break;
            }

        }
        return true;
    }

    private void signoOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Signout")
                .setMessage("Do you really want to sign out?")
                .setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.currentUser = null;

                        FirebaseAuth.getInstance().signOut();

                        Intent intent = new Intent(HomeActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // EventBus

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if(event.isSuccess()) {
            navController.navigate(R.id.nav_food_list);
        }
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onFoodSelected(FoodItemClick event) {
        if(event.isSuccess()) {
           navController.navigate(R.id.nav_food_detail);
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if(event.isHidden()) {
            fab.hide();
        }
        else {
            fab.show();
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if(event.getBestDealModel() != null) {

            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.CATEGORY_REF)
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                FirebaseDatabase.getInstance()
                                        .getReference(CommonAgr.CATEGORY_REF)
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child(CommonAgr.FOOD_REF)
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot:dataSnapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }
                                                else {
                                                    Toast.makeText(HomeActivity.this,"Item doesn't exists",Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this,"Item doesn't exists",Toast.LENGTH_SHORT).show();
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopluarCategoryClick event) {
        if(event.getPopluarCategoryModel() != null) {

            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference(CommonAgr.CATEGORY_REF)
                    .child(event.getPopluarCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                FirebaseDatabase.getInstance()
                                        .getReference(CommonAgr.CATEGORY_REF)
                                        .child(event.getPopluarCategoryModel().getMenu_id())
                                        .child(CommonAgr.FOOD_REF)
                                        .orderByChild("id")
                                        .equalTo(event.getPopluarCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot:dataSnapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());

                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }
                                                else {
                                                    Toast.makeText(HomeActivity.this,"Item doesn't exists",Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this,"Item doesn't exists",Toast.LENGTH_SHORT).show();
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this,""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    private void countCartAgain(CountCartEvent event) {
        if(event.isSuccess()) {
            coutCartItem();
        }
    }


    // Bắt event counter cart
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCartCounter(CountCartEvent event) {
        if(event.isSuccess()) {
           coutCartItem();
        }
    }

    private void coutCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("query returned empty")) {
                            Toast.makeText(HomeActivity.this,"[COUNT CART]"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        else{
                            fab.setCount(0);
                        }
                    }
                });
    }

}
