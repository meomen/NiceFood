package com.vuducminh.nicefood.callback;

import com.vuducminh.nicefood.model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadtimeFailed(String message);
}
