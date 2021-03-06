package com.minorproject.admin.sas;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NewsMessageAdapter extends ArrayAdapter<NewsMessageInfoCollector> {
    public NewsMessageAdapter(Context context, int resource, List<NewsMessageInfoCollector> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_news_message, parent, false);
        }

        ImageView photoImageView =  convertView.findViewById(R.id.photoImageView);
        TextView newsTextView =  convertView.findViewById(R.id.newsTextView);
        TextView senderNameTextView = convertView.findViewById(R.id.senderNameTextView);
        TextView dateDayTextView = convertView.findViewById(R.id.dateDayTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);
        TextView titleTextView = convertView.findViewById(R.id.titleTextView);

        NewsMessageInfoCollector newsMessage = getItem(position);





        boolean isPhoto = (newsMessage.getContent_url() != null);
        if (isPhoto) {
            newsTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(newsMessage.getContent_url())
                    .into(photoImageView);

            newsTextView.setText(newsMessage.getContent_txt());

        } else {
            newsTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            newsTextView.setText(newsMessage.getContent_txt());
        }

        senderNameTextView.setText("Sent By : " + newsMessage.getBy());
        dateDayTextView.setText(newsMessage.getdate() + " : " + newsMessage.getday());
        timeTextView.setText(newsMessage.getTime());
        titleTextView.setText("Title:"+newsMessage.getTitle());



        return convertView;
    }






}
