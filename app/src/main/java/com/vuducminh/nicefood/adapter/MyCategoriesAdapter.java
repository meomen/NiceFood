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
import com.vuducminh.nicefood.callback.IRecyclerClickListener;
import com.vuducminh.nicefood.common.Common;
import com.vuducminh.nicefood.common.CommonAgr;
import com.vuducminh.nicefood.eventbus.CategoryClick;
import com.vuducminh.nicefood.model.CategoryModel;
import com.vuducminh.nicefood.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/*Apdate tạo item Cart (Giỏ hàng)
CategolyFragment ở CategoryFragment sử dụng
 */
public class MyCategoriesAdapter extends RecyclerView.Adapter<MyCategoriesAdapter.MyViewHodler> {

    Context context;
    List<CategoryModel> categoryModelList;

    public MyCategoriesAdapter(Context context, List<CategoryModel> categoryModelList) {
        this.context = context;
        this.categoryModelList = categoryModelList;
    }

    // liên kết giao diện(layout)
    @NonNull
    @Override
    public MyViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_category_item,parent,false);
        return new MyViewHodler(view);
    }

    // Đổ dữ liệu vào giao diện
    @Override
    public void onBindViewHolder(@NonNull MyViewHodler holder, int position) {
        Glide.with(context).load(categoryModelList.get(position).getImage()).into(holder.img_category);
        holder.tv_category.setText(new StringBuffer(categoryModelList.get(position).getName()));

        //Bắt sự kiện
        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                Common.categorySelected = categoryModelList.get(pos);
                EventBus.getDefault().postSticky(new CategoryClick(true,categoryModelList.get(pos)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public List<CategoryModel> getListCategory() {
        return categoryModelList;
    }

    public class MyViewHodler extends RecyclerView.ViewHolder implements View.OnClickListener {

        Unbinder unbinder;

        @BindView(R.id.img_category)
        ImageView img_category;
        @BindView(R.id.tv_category)
        TextView tv_category;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHodler(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
        }
    }


    //Kích thước item trong RecyclerView  tùy số lượng phần tử trong List
    @Override
    public int getItemViewType(int position) {
        if(categoryModelList.size() == 1) {        //    List có 1 phần tử
            return CommonAgr.DEFAULT_COLUMN_COUNT;  //   thì item rộng 1 nửa màn hình
        }
        else {
            if(categoryModelList.size() % 2 == 0) {     // Nếu số lượng phần trử chắn
                return CommonAgr.DEFAULT_COLUMN_COUNT;   //thì item rộng 1 nửa màn hình
            }
            else {  //Lẻ
                if(position > 1 && position == categoryModelList.size()-1)
                    return CommonAgr.FULL_WIDTH_COLUMN;    // Phần tử cuối cùng, rộng toàn màn hình
                else
                    return CommonAgr.DEFAULT_COLUMN_COUNT;
            }
        }
    }
}
