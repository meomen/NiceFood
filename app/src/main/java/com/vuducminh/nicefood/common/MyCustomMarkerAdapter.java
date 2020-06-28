package com.vuducminh.nicefood.common;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.vuducminh.nicefood.R;

// Adapter này đổ dữ liệu vào khung thông tin shipper, trên bảng đồ google
public class MyCustomMarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private View itemView;

    // liên kết giao diện(layout)
    public MyCustomMarkerAdapter(LayoutInflater inflater) {
        this.itemView = inflater.inflate(R.layout.layout_marker_display,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        //Liên kết phần tử giao diện(weight)
        TextView tv_shipper_name = ((TextView)itemView.findViewById(R.id.tv_shipper_name));
        TextView tv_shipper_info = ((TextView)itemView.findViewById(R.id.tv_shipper_info));

        // Đổ dữ liệu vào giao diện
        tv_shipper_name.setText(marker.getTitle());
        tv_shipper_info.setText(marker.getSnippet());
        return itemView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

