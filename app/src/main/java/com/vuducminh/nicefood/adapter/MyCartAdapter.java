package com.vuducminh.nicefood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vuducminh.nicefood.common.Common;
import com.vuducminh.nicefood.database.CartItem;
import com.vuducminh.nicefood.eventbus.UpdateItemInCart;
import com.vuducminh.nicefood.R;
import com.vuducminh.nicefood.model.AddonModel;
import com.vuducminh.nicefood.model.SizeModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    Gson gson;

    public MyCartAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        this.gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.layout_cart_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CartItem cartItem = cartItemList.get(position);
        Glide.with(context).load(cartItem.getFoodImage()).into(holder.img_cart);

        holder.tv_food_name.setText(new StringBuilder(cartItem.getFoodName()));

        holder.tv_food_price.setText(new StringBuilder("").
                append(cartItem.getFoodPrice() + cartItem.getFoodExtraPrice()));

        if(cartItemList.get(position).getFoodSize() != null) {
            if(cartItemList.get(position).getFoodSize().equals("Default")) {
                holder.tv_food_size.setText(new StringBuilder("Size: ").append("Default"));
            }
            else {
                SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getFoodSize(),new TypeToken<SizeModel>(){}.getType());
                holder.tv_food_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
            }
        }

        if(cartItemList.get(position).getFoodAddon() != null) {
            if(cartItemList.get(position).getFoodAddon().equals("Default"))
                holder.tv_food_addon.setText(new StringBuilder("Addon: ").append("Default"));
            else {
                List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getFoodAddon(),
                        new TypeToken<List<AddonModel>>(){}.getType());
                holder.tv_food_addon.setText(new StringBuilder("Addon: ").append(Common.getListAddon(addonModels)));
            }
        }

        holder.numberButton.setNumber(String.valueOf(cartItem.getFoodQuantity()));
        //Event
        holder.numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                cartItem.setFoodQuantity(newValue);
                EventBus.getDefault().postSticky(new UpdateItemInCart(cartItem));
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public CartItem getItemAtPosition(int position) {
        return cartItemList.get(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private Unbinder unbinder;

        @BindView(R.id.img_cart)
        ImageView img_cart;
        @BindView(R.id.tv_food_name)
        TextView tv_food_name;
        @BindView(R.id.tv_food_price)
        TextView tv_food_price;
        @BindView(R.id.tv_food_size)
        TextView tv_food_size;
        @BindView(R.id.tv_food_addon)
        TextView tv_food_addon;
        @BindView(R.id.number_button)
        ElegantNumberButton numberButton;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
