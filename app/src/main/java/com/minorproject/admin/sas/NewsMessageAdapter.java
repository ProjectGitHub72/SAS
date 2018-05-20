package com.minorproject.admin.sas;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
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

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView newsTextView = (TextView) convertView.findViewById(R.id.newsTextView);
        TextView senderNameTextView = (TextView) convertView.findViewById(R.id.senderNameTextView);
        TextView dateDayTextView = convertView.findViewById(R.id.dateDayTextView);
        TextView timeTextView = convertView.findViewById(R.id.timeTextView);

        NewsMessageInfoCollector newsMessage = getItem(position);

        Date mDate = new Date(newsMessage.getdate());
        String formattedDate = formatDate(mDate);
        String formattedTime = formatTime(mDate);
        String dayOfWeek = newsMessage.getday();



        boolean isPhoto = (newsMessage.getPhotoUrl() != null);
        if (isPhoto) {
            newsTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(newsMessage.getPhotoUrl())
                    .into(photoImageView);
        } else {
            newsTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            newsTextView.setText(newsMessage.getNews());
        }

        senderNameTextView.setText("Sent By : " + newsMessage.getSender());
        dateDayTextView.setText(formattedDate + " : " + dayOfWeek);
        timeTextView.setText(formattedTime);



        return convertView;
    }


    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }



}
