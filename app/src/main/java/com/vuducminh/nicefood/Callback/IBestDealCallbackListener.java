package com.vuducminh.nicefood.Callback;

import com.vuducminh.nicefood.Model.BestDealModel;
import com.vuducminh.nicefood.Model.PopluarCategoryModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> BestDealModels);
    void onBestDealLoadFailed(String message);
}
