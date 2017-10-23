package com.example.jatin.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<ThreeStrings> {
    private int layoutResource;

    public CustomAdapter(Context context, int layoutResource, List<ThreeStrings> threeStringsList) {
        super(context, layoutResource, threeStringsList);
        this.layoutResource = layoutResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(layoutResource, null);
        }
        ThreeStrings threeStrings = getItem(position);
        if (threeStrings != null) {
            TextView subject =  view.findViewById(R.id.subject);
            TextView author =view.findViewById(R.id.author);
            TextView content =view.findViewById(R.id.content);
            if (subject != null) {
                subject.setText(threeStrings.getSubject());
            }
            if (author != null) {
                author.setText(threeStrings.getAuthor());
            }
            if (content != null) {
                content.setText(threeStrings.getContent());
            }
        }
        return view;
    }
}
