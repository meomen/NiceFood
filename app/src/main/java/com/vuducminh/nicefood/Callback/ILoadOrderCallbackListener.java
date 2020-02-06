package com.vuducminh.nicefood.Callback;

import com.vuducminh.nicefood.Model.Order;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderLoadSuccess(List<Order> corderModels);
    void onLoadOrderLoadFailed(String message);
}
