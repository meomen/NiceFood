package com.vuducminh.nicefood.callback;

import com.vuducminh.nicefood.database.CartItem;
import com.vuducminh.nicefood.model.CategoryModel;
import com.vuducminh.nicefood.model.FoodModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
