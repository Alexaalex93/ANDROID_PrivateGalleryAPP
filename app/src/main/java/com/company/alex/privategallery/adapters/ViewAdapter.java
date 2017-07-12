package com.company.alex.privategallery.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.company.alex.privategallery.R;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.util.ArrayList;

/**
 * Created by Alex on 24/04/2017.
 */

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList <Bitmap> mList;


    ArrayList<Image> list;
    Activity activity;
    int columnCount;
    private ArrayList<Long> selectedIDs;
    private int screenWidth;
    private View.OnClickListener onClickListener;
    Params params;


    public ViewAdapter(Context context, ArrayList list) {
        mContext = context;
        mList = list;
    }
    public ViewAdapter(Context context, Bitmap image) {
        mContext = context;
        mList = new ArrayList<>();
        mList.add(image);
    }

    private Context getmContext() {
        return mContext;
    }

    @Override
    public ViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflate the custom layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_layout, parent, false);

        // Return a new holder instance
        return new ViewHolder(view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewAdapter.ViewHolder viewHolder, int position) {

        // Set item views based on your views and data model
        ImageView image = viewHolder.imageView;
        image.setImageBitmap(mList.get(position));
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public ViewHolder(View v) {
            super(v);
            imageView = (ImageView) v.findViewById(R.id.main_image_view);
        }
    }

}
