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
import com.vuducminh.nicefood.Model.PopluarCategoryModel;
import com.vuducminh.nicefood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyPopularCategoriesAdapter extends RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViewHolder>{

    Context context;
    List<PopluarCategoryModel> popluarCategoryModelList;

    public MyPopularCategoriesAdapter(Context context, List<PopluarCategoryModel> popluarCategoryModelList) {
        this.context = context;
        this.popluarCategoryModelList = popluarCategoryModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.lauout_popular_categories_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(popluarCategoryModelList.get(position).getImage())
                .into(holder.category_image);
        holder.tv_category_name.setText(popluarCategoryModelList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return popluarCategoryModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        Unbinder unbinder;

        @BindView(R.id.tv_category_name)
        TextView tv_category_name;
        @BindView(R.id.category_image)
        ImageView category_image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}