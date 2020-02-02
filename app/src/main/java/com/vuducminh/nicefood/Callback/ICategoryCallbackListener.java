package com.vuducminh.nicefood.Callback;

import com.vuducminh.nicefood.Model.BestDealModel;
import com.vuducminh.nicefood.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> CategoryModels);
    void onCategoryLoadFailed(String message);
}
