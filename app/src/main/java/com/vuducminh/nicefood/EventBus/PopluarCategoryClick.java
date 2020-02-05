package com.vuducminh.nicefood.EventBus;

import com.vuducminh.nicefood.Model.PopluarCategoryModel;

public class PopluarCategoryClick {

    private PopluarCategoryModel popluarCategoryModel;

    public PopluarCategoryClick(PopluarCategoryModel popluarCategoryModel) {
        this.popluarCategoryModel = popluarCategoryModel;
    }

    public PopluarCategoryModel getPopluarCategoryModel() {
        return popluarCategoryModel;
    }

    public void setPopluarCategoryModel(PopluarCategoryModel popluarCategoryModel) {
        this.popluarCategoryModel = popluarCategoryModel;
    }
}
