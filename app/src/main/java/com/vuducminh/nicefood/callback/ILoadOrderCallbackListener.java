package com.vuducminh.nicefood.callback;

import com.vuducminh.nicefood.model.Order;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderLoadSuccess(List<Order> corderModels);
    void onLoadOrderLoadFailed(String message);
}
