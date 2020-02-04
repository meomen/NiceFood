package com.vuducminh.nicefood.Common;

import com.vuducminh.nicefood.Model.AddonModel;
import com.vuducminh.nicefood.Model.CategoryModel;
import com.vuducminh.nicefood.Model.FoodModel;
import com.vuducminh.nicefood.Model.SizeModel;
import com.vuducminh.nicefood.Model.UserModel;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

public class Common {

    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;

    public static String formatPrice(double price) {
        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return finalPrice.replace(".", ",");
        } else return "0,00";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result = 0.0;

        // không có size và addon
        if (userSelectedSize == null && userSelectedAddon == null) {
            return 0.0;
        }
        // Không có size
        else if (userSelectedSize == null) {
            for(AddonModel addonModel : userSelectedAddon) {
                result += addonModel.getPrice();
            }
            return result;
        }
        //Không có addon
        else if(userSelectedAddon == null) {
            return userSelectedSize.getPrice()*1.0;
        }

        // Có đủ cả 2
        else {
            result = userSelectedSize.getPrice()*1.0;
            for(AddonModel addonModel : userSelectedAddon) {
                result += addonModel.getPrice();
            }
            return result;
        }
    }
}
