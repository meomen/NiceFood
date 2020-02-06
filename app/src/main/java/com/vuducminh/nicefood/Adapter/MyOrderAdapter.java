package com.vuducminh.nicefood.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vuducminh.nicefood.Common.Common;
import com.vuducminh.nicefood.Model.Order;
import com.vuducminh.nicefood.R;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>{

    private Context context;
    private List<Order> orderList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = LayoutInflater.from(context).inflate(R.layout.layout_order_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Order order = orderList.get(position);
        Glide.with(context).load(order.getCartItemList().get(0).getFoodImage()).into(holder.img_order);
        calendar.setTimeInMillis(order.getCreateDate());
        Date date = new Date(order.getCreateDate());
        holder.tv_order_date.setText(new StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
        .append("")
        .append(simpleDateFormat.format(date)));
        holder.tv_order_number.setText(new StringBuilder("Order number: ").append(order.getOrderNumber()));
        holder.tv_order_comment.setText(new StringBuilder("Comment: ").append(order.getCommet()));
        holder.tv_order_status.setText(new StringBuilder("Status: ").append(Common.convertStatusToText(order.getOrderStatus())));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        private Unbinder unbinder;

        @BindView(R.id.img_order)
        ImageView img_order;
        @BindView(R.id.tv_order_date)
        TextView tv_order_date;
        @BindView(R.id.tv_order_number)
        TextView tv_order_number;
        @BindView(R.id.tv_order_comment)
        TextView tv_order_comment;
        @BindView(R.id.tv_order_status)
        TextView tv_order_status;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
