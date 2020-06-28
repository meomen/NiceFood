package com.vuducminh.nicefood.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.bumptech.glide.Glide;
import com.vuducminh.nicefood.eventbus.BestDealItemClick;
import com.vuducminh.nicefood.model.BestDealModel;
import com.vuducminh.nicefood.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


//Apdate tạo item Món ăn đề suất( Bestdeal)
//BestdealList ở Home Fragment sử dụng
public class MyBestdealAdapter extends LoopingPagerAdapter<BestDealModel> {

    //Liên kết phần tử giao diện(weight)
    //Sử dụng thư viện Butter Knife

    @BindView(R.id.img_best_deal)
    ImageView img_best_deal;
    @BindView(R.id.tv_best_deal)
    TextView tv_best_deal;

    Unbinder unbinder;

    public MyBestdealAdapter(Context context, List<BestDealModel> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
    }

    // liên kết giao diện(layout)
    @Override
    protected View inflateView(int viewType, ViewGroup container, int listPosition) {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deal_item,container,false);
    }

    @Override
    protected void bindView(View convertView, int listPosition, int viewType) {
        unbinder = ButterKnife.bind(this,convertView);

        // Đổ dữ liệu vào giao diện
        Glide.with(convertView).load(itemList.get(listPosition).getImage()).into(img_best_deal);
        tv_best_deal.setText(itemList.get(listPosition).getName());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BestDealItemClick(itemList.get(listPosition)));
            }
        });
    }
}
