package com.vuducminh.nicefood.EventBus;

public class CountCartEvent {
    private boolean success;

    public CountCartEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
