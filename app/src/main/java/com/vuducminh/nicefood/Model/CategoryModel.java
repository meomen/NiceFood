package com.vuducminh.nicefood.Model;

import java.util.List;

public class CategoryModel {
    private String menu_id,name,image;
    List<FoodModel> listfood;

    public CategoryModel() {
    }

    public CategoryModel(String menu_id, String name, String image, List<FoodModel> listfood) {
        this.menu_id = menu_id;
        this.name = name;
        this.image = image;
        this.listfood = listfood;
    }

    public String getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(String menu_id) {
        this.menu_id = menu_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<FoodModel> getListfood() {
        return listfood;
    }

    public void setListfood(List<FoodModel> listfood) {
        this.listfood = listfood;
    }
}
