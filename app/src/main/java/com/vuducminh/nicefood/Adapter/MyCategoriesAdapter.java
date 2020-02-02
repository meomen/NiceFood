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
import com.vuducminh.nicefood.Common.CommonAgr;
import com.vuducminh.nicefood.Model.CategoryModel;
import com.vuducminh.nicefood.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyCategoriesAdapter extends RecyclerView.Adapter<MyCategoriesAdapter.MyViewHodler> {

    Context context;
    List<CategoryModel> categoryModelList;

    public MyCategoriesAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public MyViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false);
        return new MyViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHodler holder, int position) {
        Glide.with(context).load(categoryModelList.get(position).getImage()).into(holder.img_category);
        holder.tv_category.setText(new StringBuffer(categoryModelList.get(position).getName()));
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class MyViewHodler extends RecyclerView.ViewHolder {

        Unbinder unbinder;

        @BindView(R.id.img_category)
        ImageView img_category;
        @BindView(R.id.tv_category)
        TextView tv_category;

        public MyViewHodler(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(categoryModelList.size() == 1) {
            return CommonAgr.DEFAULT_COLUMN_COUNT;
        }
        else {
            if(categoryModelList.size() % 2 == 0) {
                return CommonAgr.DEFAULT_COLUMN_COUNT;
            }
            else {
                if(position > 1 && position == categoryModelList.size()-1)
                    return CommonAgr.FULL_WIDTH_COLUMN;
                else
                    return CommonAgr.DEFAULT_COLUMN_COUNT;
            }
        }
    }
}
