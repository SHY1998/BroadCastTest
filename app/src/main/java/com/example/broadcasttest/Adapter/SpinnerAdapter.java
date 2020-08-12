package com.example.broadcasttest.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.broadcasttest.R;

public class SpinnerAdapter extends ArrayAdapter<CharSequence> {
    private String[] array;
    private Context mContext;
    public SpinnerAdapter(Context context, int resource, String[] array) {
        super(context, resource,array);
        this.array = array;
        mContext = context;
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.support_simple_spinner_dropdown_item, parent,false);
        }
        TextView tv =  convertView.findViewById(android.R.id.text1);
        tv.setText(array[position]);
        //tv.setTextSize(22f);
        tv.setTextColor(Color.GREEN);
        tv.setPadding(40,0,40,0);
        return convertView;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView,@NonNull ViewGroup parent) {
        //此处text1是Spinner默认的用来显示文字的TextView
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.support_simple_spinner_dropdown_item, parent,false);
        }
        TextView tv =  convertView.findViewById(android.R.id.text1);
        tv.setText(array[position]);
        tv.setPadding(10,0,10,0);
        //tv.setTextSize(18f);
        tv.setTextColor(Color.BLACK);
        return convertView;
    }

}
