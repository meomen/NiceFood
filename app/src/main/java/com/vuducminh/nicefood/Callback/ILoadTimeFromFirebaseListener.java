package com.vuducminh.nicefood.Callback;

import com.vuducminh.nicefood.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order, long estimateTimeInMs);
    void onLoadtimeFailed(String message);
}
