package com.vuducminh.nicefood.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;
import com.vuducminh.nicefood.model.AddonModel;
import com.vuducminh.nicefood.model.CategoryModel;
import com.vuducminh.nicefood.model.FoodModel;
import com.vuducminh.nicefood.model.RestaurantModel;
import com.vuducminh.nicefood.model.ShippingOrderModel;
import com.vuducminh.nicefood.model.SizeModel;
import com.vuducminh.nicefood.model.TokenModel;
import com.vuducminh.nicefood.model.UserModel;
import com.vuducminh.nicefood.R;
import com.vuducminh.nicefood.services.MyFCMServices;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

import io.paperdb.Paper;

public class Common {

    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
    public static String currentToken = "";
    public static ShippingOrderModel currentShippingOrder;
    public static RestaurantModel currentRestaurant;

    // Chỉnh format Số tiền
    public static String formatPrice(double price) {
        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return finalPrice.replace(".", ",");
        } else return "0,00";
    }

    // Tính thêm tiền khi Food có thêm Addon và Size
    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result = 0.0;

        // không có size và addon
        if (userSelectedSize == null && userSelectedAddon == null) {
            return 0.0;
        }
        // Không có size
        else if (userSelectedSize == null) {
            for (AddonModel addonModel : userSelectedAddon) {
                result += addonModel.getPrice();
            }
            return result;
        }
        //Không có addon
        else if (userSelectedAddon == null) {
            return userSelectedSize.getPrice() * 1.0;
        }

        // Có đủ cả 2
        else {
            result = userSelectedSize.getPrice() * 1.0;
            for (AddonModel addonModel : userSelectedAddon) {
                result += addonModel.getPrice();
            }
            return result;
        }
    }

    // chỉnh font SpanString
    public static void setSpanString(String welcome, String name, TextView tv_user) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);            // In Đậm
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);     // Chữ Nghiên
        builder.append(spannableString);
        tv_user.setText(builder, TextView.BufferType.SPANNABLE);
    }

    // Tạo số ID cho order
    public static String creteOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())        // Lấy thời gian hiện tại, dạng mili giây
                .append(Math.abs(new Random().nextInt()))  // Thêm số ngẫu nhiên để tránh nhầm lẫn với order cùng thời gian tạo
                .toString();

    }

    // Thứ trong ngày
    public static String getDateOfWeek(int i) {
        switch (i) {
            case 1: {
                return "Monday";
            }
            case 2: {
                return "Tuesday";
            }
            case 3: {
                return "Wednesday";
            }
            case 4: {
                return "Thursday";
            }
            case 5: {
                return "Friday";
            }
            case 6: {
                return "Saturday";
            }
            case 7: {
                return "Sunday";
            }
            default: {
                return "Unknown";
            }
        }
    }

    // <key-value> Trạng thái order
    public static String convertStatusToText(int orderStatus) {
        switch (orderStatus) {
            case 0: {
                return "Placed";
            }
            case 1: {
                return "Shipping";
            }
            case 2: {
                return "Shipped";
            }
            case -1: {
                return "Cancelled";
            }
            default:
                return "Unknown";
        }
    }

    // Tạo thông báo
    public static void showNotification(Context context, int id, String title, String content, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "minh_vu_nice_food_java";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {             // Nếu API Android >= 26
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Nice Food Java", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Nice Food Java");           // Mô tả
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        // tạo giao diện thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_restaurant_menu_black_24dp));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);     //Nạp nội dụng thông báo
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);  //hiện thông báo
    }

    // Cập nhận Token
    public static void updateToken(Context context, String newToken) {
       if(Common.currentUser != null) {
           FirebaseDatabase.getInstance()
                   .getReference(CommonAgr.TOKEN_REF)
                   .child(Common.currentUser.getUid())
                   .setValue(new TokenModel(Common.currentUser.getPhone(), newToken))
                   .addOnFailureListener(e -> {
                       Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                   });
       }
    }

    //
    public static String createTopicOrder() {
        return new StringBuilder("/topics/")
                .append(Common.currentRestaurant.getUid())
                .append("_")
                .append("new_order")
                .toString();
    }
    public static List<LatLng> decodePoly(String encode) {
        List poly = new ArrayList();
        int index = 0,len = encode.length();
        int lat = 0, lng = 0;
        while(index < len) {
            int b,shift=0,result=0;
            do{
                b = encode.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lat +=dlat;
            shift = 0;
            result = 0;
            do{
                b = encode.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while(b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1):(result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double)lat/1E5)),
                    (((double)lng/1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);


        if(begin.latitude < end.latitude && begin.longitude < end.longitude) {
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        }
        else if(begin.latitude >= end.latitude && begin.longitude < end.longitude) {
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat)))+90);
        }
        else if(begin.latitude >= end.latitude && begin.longitude >= end.longitude) {
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        }
        else if(begin.latitude < end.latitude && begin.longitude >= end.longitude) {
            return (float)((90 - Math.toDegrees(Math.atan(lng/lat)))+270);
        }
        return -1;
    }

    public static String getListAddon(List<AddonModel> addonModels) {
        StringBuilder result = new StringBuilder();
        for(AddonModel addonModel: addonModels) {
            result.append(addonModel.getName()).append(",");
        }
        if(result.length() == 0) {
            return "Default";
        }
        return result.substring(0,result.length()-1);
    }

    public static FoodModel findFoodInListById(CategoryModel categoryModel, String foodId) {
        if(categoryModel.getFoods() != null && categoryModel.getFoods().size() > 0) {
            for (FoodModel foodModel: categoryModel.getFoods()) {
                if(foodModel.getId().equals(foodId)) {
                    return foodModel;
                }
            }
            return null;
        }
        else {
            return null;
        }
    }

    public static void showNotificationBigStyle(Context context, int id, String title, String content, Bitmap bitmap, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        String NOTIFICATION_CHANNEL_ID = "minh_vu_nice_food_java";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Nice Food Java", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Nice Food Java");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static String createTopicNews() {
        return new StringBuilder("/topics/")
                .append(Common.currentRestaurant.getUid())
                .append("_")
                .append("news")
                .toString();
    }

    public static String generateChatRoomId(String a, String b) {
        if(a.compareTo(b) > 0)
            return new StringBuilder(a).append(b).toString();
        else if(a.compareTo(b) < 0)
            return new StringBuilder(b).append(a).toString();
        else
            return new StringBuilder("ChatYourSelf_Error_")
            .append(new Random().nextInt())
            .toString();
    }

    public static String getFileName(ContentResolver contentResolver, Uri fileUri) {
        String result = null;
        if(fileUri.getScheme().equals("content")) {
            Cursor cursor = contentResolver.query(fileUri,null,null,null,null);
            try {
                if(cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

            }
            finally {
                cursor.close();
            }
        }
        if(result == null) {
           result = fileUri.getPath();
           int cut = result.lastIndexOf('/');
           if(cut != -1)
               result = result.substring(cut+1);
        }
        return result;
    }

    public static boolean isEmail(String emailStr) {
        Matcher matcher = CommonAgr.VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }
}
