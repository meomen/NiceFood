package com.vuducminh.nicefood.ui.view_orders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidwidgets.formatedittext.widgets.FormatEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuducminh.nicefood.TrackingOrderActivity;
import com.vuducminh.nicefood.adapter.MyOrderAdapter;
import com.vuducminh.nicefood.callback.ILoadOrderCallbackListener;
import com.vuducminh.nicefood.common.Common;
import com.vuducminh.nicefood.common.CommonAgr;
import com.vuducminh.nicefood.common.MySwiperHelper;
import com.vuducminh.nicefood.database.CartDAO;
import com.vuducminh.nicefood.database.CartDataSource;
import com.vuducminh.nicefood.database.CartDatabase;
import com.vuducminh.nicefood.database.CartItem;
import com.vuducminh.nicefood.database.LocalCartDataSource;
import com.vuducminh.nicefood.eventbus.CountCartEvent;
import com.vuducminh.nicefood.eventbus.MenuItemBack;
import com.vuducminh.nicefood.eventbus.MenuItemEvent;
import com.vuducminh.nicefood.model.OrderModel;
import com.vuducminh.nicefood.R;
import com.vuducminh.nicefood.model.RefundRequestModel;
import com.vuducminh.nicefood.model.ShippingOrderModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {


    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    private Unbinder unbinder;

    private ViewOrdersViewModel viewOrdersViewModel;

    private AlertDialog dialog;
    private ILoadOrderCallbackListener listener;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewOrdersViewModel =
                ViewModelProviders.of(this).get(ViewOrdersViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_order, container, false);
        unbinder = ButterKnife.bind(this,root);
        loadOrdersFromFirebase();
        initViews(root);

        viewOrdersViewModel.getMutableLiveDataOrderList().observe(this,orderList -> {
            Collections.reverse(orderList);
            MyOrderAdapter adapter = new MyOrderAdapter(getContext(),orderList);
            recycler_orders.setAdapter(adapter);
        });
        return root;
    }

    private void loadOrdersFromFirebase() {
        List<OrderModel> orderModelList = new ArrayList<>();
        FirebaseDatabase.getInstance()
                .getReference(CommonAgr.RESTAURANT_REF)
                .child(Common.currentRestaurant.getUid())
                .child(CommonAgr.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.currentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot orderSnapShot:dataSnapshot.getChildren()) {
                            OrderModel orderModel = orderSnapShot.getValue(OrderModel.class);
                            orderModel.setOrderNumber(orderSnapShot.getKey());
                            orderModelList.add(orderModel);
                        }
                        listener.onLoadOrderLoadSuccess(orderModelList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onLoadOrderLoadFailed(databaseError.getMessage());
                    }
                });
    }

    private void initViews(View root) {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        listener = this;
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(layoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_orders, 250) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(), "Cancel Order", 30, 0, Color.parseColor("#FF3C30"),
                        position -> {
                            OrderModel orderModel = ((MyOrderAdapter)recycler_orders.getAdapter()).getItemAtPosition(position);

                            if(orderModel.getOrderStatus() == 0) {
                                if(orderModel.isCod()) {
                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                    builder.setTitle("Cancel Order")
                                            .setMessage("Do you really want to cancel this order?")
                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                            .setPositiveButton("YES", (dialog, which) -> {
                                                Map<String,Object> update_data = new HashMap<>();
                                                update_data.put("orderStatus",-1);
                                                FirebaseDatabase.getInstance()
                                                        .getReference(CommonAgr.RESTAURANT_REF)
                                                        .child(Common.currentRestaurant.getUid())
                                                        .child(CommonAgr.ORDER_REF)
                                                        .child(orderModel.getOrderNumber())
                                                        .updateChildren(update_data)
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnSuccessListener(aVoid -> {
                                                            orderModel.setOrderStatus(-1);
                                                            ((MyOrderAdapter)recycler_orders.getAdapter()).setItemAtPosition(position,orderModel);
                                                            recycler_orders.getAdapter().notifyItemChanged(position);
                                                            Toast.makeText(getContext(),"Cancel order successfully",Toast.LENGTH_SHORT).show();
                                                        });
                                            });

                                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                                else {
                                    View layout_refund_request = LayoutInflater.from(getContext())
                                            .inflate(R.layout.layout_refund_request,null);

                                    EditText edt_name = (EditText)layout_refund_request.findViewById(R.id.edt_card_name);
                                    FormatEditText edt_card_number = (FormatEditText) layout_refund_request.findViewById(R.id.edt_card_number);
                                    FormatEditText edt_card_exp = (FormatEditText) layout_refund_request.findViewById(R.id.edt_exp);

                                    //Format input
                                    edt_card_number.setFormat("---- ---- ---- ----");
                                    edt_card_exp.setFormat("--/--");


                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                                    builder.setTitle("Cancel Order")
                                            .setMessage("Do you really want to cancel this order?")
                                            .setView(layout_refund_request)
                                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                            .setPositiveButton("YES", (dialog, which) -> {

                                                RefundRequestModel refundRequestModel = new RefundRequestModel();
                                                refundRequestModel.setName(Common.currentUser.getName());
                                                refundRequestModel.setPhone(Common.currentUser.getPhone());
                                                refundRequestModel.setCardName(edt_name.getText().toString());
                                                refundRequestModel.setCardNumber(edt_card_number.getText().toString());
                                                refundRequestModel.setCardExp(edt_card_exp.getText().toString());
                                                refundRequestModel.setAmount(orderModel.getFinalPayment());


                                                FirebaseDatabase.getInstance()
                                                        .getReference(CommonAgr.RESTAURANT_REF)
                                                        .child(Common.currentRestaurant.getUid())
                                                        .child(CommonAgr.REQUEST_REFUND_MODEL)
                                                        .child(orderModel.getOrderNumber())
                                                        .setValue(refundRequestModel)
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                            }
                                                        })
                                                        .addOnSuccessListener(aVoid -> {
                                                            Map<String,Object> update_data = new HashMap<>();
                                                            update_data.put("orderStatus",-1);
                                                            FirebaseDatabase.getInstance()
                                                                    .getReference(CommonAgr.RESTAURANT_REF)
                                                                    .child(Common.currentRestaurant.getUid())
                                                                    .child(CommonAgr.ORDER_REF)
                                                                    .child(orderModel.getOrderNumber())
                                                                    .updateChildren(update_data)
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    })
                                                                    .addOnSuccessListener(a -> {
                                                                        orderModel.setOrderStatus(-1);
                                                                        ((MyOrderAdapter)recycler_orders.getAdapter()).setItemAtPosition(position,orderModel);
                                                                        recycler_orders.getAdapter().notifyItemChanged(position);
                                                                        Toast.makeText(getContext(),"Cancel order successfully",Toast.LENGTH_SHORT).show();
                                                                    });
                                                        });
                                            });

                                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                                    dialog.show();
                                }

                            }
                            else {
                                Toast.makeText(getContext(),new StringBuilder("You order was changed to ")
                                .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                .append(", so you can't cancel it!"),Toast.LENGTH_SHORT).show();
                            }
                        }));

                buf.add(new MyButton(getContext(), "Tracking Order", 30, 0, Color.parseColor("#001970"),
                        position -> {
                            OrderModel orderModel = ((MyOrderAdapter)recycler_orders.getAdapter()).getItemAtPosition(position);

                            FirebaseDatabase.getInstance()
                                    .getReference(CommonAgr.RESTAURANT_REF)
                                    .child(Common.currentRestaurant.getUid())
                                    .child(CommonAgr.SHIPPER_ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()) {
                                                Common.currentShippingOrder = dataSnapshot.getValue(ShippingOrderModel.class);
                                                Common.currentShippingOrder.setKey(dataSnapshot.getKey());


                                                if(Common.currentShippingOrder.getCurrentLat() != -1 &&
                                                Common.currentShippingOrder.getCurrentLng() != -1) {
                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));
                                                }

                                                else {
                                                    Toast.makeText(getContext(),"Shipper mot start ship your order, just wait",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else {
                                                Toast.makeText(getContext(),"Your order just placed, must be wait it shipping",Toast.LENGTH_SHORT).show();

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getContext(),""+databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }));

                buf.add(new MyButton(getContext(), "Repeat Order", 30, 0, Color.parseColor("#5d4037"),
                        position -> {
                            OrderModel orderModel = ((MyOrderAdapter)recycler_orders.getAdapter()).getItemAtPosition(position);

                            dialog.show();
                            cartDataSource.cleanCart(Common.currentUser.getUid(),
                                    Common.currentRestaurant.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {

                                            CartItem[] cartItems = orderModel
                                                    .getCartItemList().toArray(new CartItem[orderModel.getCartItemList().size()]);

                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(() -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), "Add all item in order to cart success", Toast.LENGTH_SHORT).show();
                                                        EventBus.getDefault().postSticky(new CountCartEvent(true));
                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext()," "+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    })
                                            );
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(),"[Error]"+ e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));
            }
        };

    }


    @Override
    public void onLoadOrderLoadSuccess(List<OrderModel> orderModelModels) {
        dialog.dismiss();

        viewOrdersViewModel.setMutableLiveDataOrderList(orderModelModels);
    }

    @Override
    public void onLoadOrderLoadFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}