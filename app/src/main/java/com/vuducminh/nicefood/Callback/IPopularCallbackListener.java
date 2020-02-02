package com.vuducminh.nicefood.Callback;

import com.vuducminh.nicefood.Model.PopluarCategoryModel;

import java.util.List;

public interface IPopularCallbackListener {
    void onPopularLoadSuccess(List<PopluarCategoryModel> popluarCategoryModels);
    void onPopularLoadFailed(String message);
}
