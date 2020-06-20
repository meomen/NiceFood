package com.vuducminh.nicefood.common;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.vuducminh.nicefood.R;

public class MyCustomMarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private View itemView;

    public MyCustomMarkerAdapter(LayoutInflater inflater) {
        this.itemView = inflater.inflate(R.layout.layout_marker_display,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {

        TextView tv_shipper_name = ((TextView)itemView.findViewById(R.id.tv_shipper_name));
        TextView tv_shipper_info = ((TextView)itemView.findViewById(R.id.tv_shipper_info));

        tv_shipper_name.setText(marker.getTitle());
        tv_shipper_info.setText(marker.getSnippet());
        return itemView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}

