package com.company.alex.privategallery.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.bumptech.glide.Glide;
import com.company.alex.privategallery.utils.ImagesData;
import com.company.alex.privategallery.R;
import com.company.alex.privategallery.utils.Parameters;
import com.company.alex.privategallery.utils.Utils;
import com.vlk.multimager.views.AutoImageView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Alex on 04/05/2017.
 */

public class ImagesAdapter extends DragSelectRecyclerViewAdapter<ImagesAdapter.ImageHolder> {

    ArrayList<ImagesData> list;
    Activity activity;
    int columnCount;
    private ArrayList<Long> selectedIDs;
    private int screenWidth;
    private View.OnClickListener onClickListener;
    Parameters parameters;

    public interface ClickListener {

        void onClick(int index);
        void onLongClick(int index);
    }

    private final ClickListener mCallback;

    public ImagesAdapter(Context activity, ArrayList<ImagesData> list, int columnCount, Parameters parameters) {
        this.activity = (Activity) activity;
        this.mCallback = (ClickListener) activity;
        this.list = list;
        this.columnCount = columnCount;
        this.parameters = parameters;
        this.selectedIDs = new ArrayList();
        WindowManager wm = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        this.screenWidth = size.x;
    }
    public String getItem(int index) {
        return list.get(index).toString();
    }
    public int getItemCount() {
        return this.list.size();
    }

    public long getItemId(int position) {
        return (this.list.get(position))._id;
    }

    public ImagesData getData(int position) {
        return this.list.get(position);
    }

    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_layout, parent, false);
        return new ImageHolder(view);

/*        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.grid_item_layout, parent, false);
        ImagesAdapter.ImageHolder dataObjectHolder = new ImagesAdapter.ImageHolder(view);
        return new ImageHolder(view);*/
    }

    public void onBindViewHolder(ImageHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        final Drawable d;
        final Context c = viewHolder.itemView.getContext();
        if (isIndexSelected(position)) {
            d = new ColorDrawable(ContextCompat.getColor(c, R.color.grid_foreground_selected));
        } else {
            d = null;
        }
        (viewHolder.imageView).setForeground(d);
       // viewHolder.imageView.setBackgroundColor(COLORS[position]);
/////////////////////////////////


        ImagesAdapter.ImageHolder holder = viewHolder;
        ImagesData entity = list.get(position);
        float height;
        if(entity.isPortraitImage) {
            height = this.activity.getResources().getDimension(R.dimen.image_height_portrait);
        } else {
            height = this.activity.getResources().getDimension(R.dimen.image_height_landscape);
        }

        if(holder.imageView != null) {
            Glide.with(this.activity).load(entity.uri).crossFade().into(holder.imageView);
        }

        if(this.selectedIDs.contains(entity._id)) {
            if(this.parameters.getLightColor() != 0) {
                holder.frameLayout.setForeground(new ColorDrawable(this.parameters.getLightColor()));
            }

            holder.selectedImageView.setVisibility(View.INVISIBLE);
        } else {
            holder.frameLayout.setForeground((Drawable)null);
        }

      //  holder.setTag(R.id.image_id, entity._id);

        ////////////////////
    }


    @Override
    protected boolean isIndexSelectable(int index) {
        return true;
    }


    public void setItems(ArrayList<ImagesData> imagesList) {
        this.list.clear();
        this.list.addAll(imagesList);
    }

    public ArrayList<Long> getSelectedIDs() {
        return this.selectedIDs;
    }

    public void setOnHolderClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public class ImageHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public RelativeLayout parentLayout;
        public FrameLayout frameLayout;
        public AutoImageView imageView;
        public ImageView selectedImageView;


        public ImageHolder(View v) {
            super(v);

            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);

            this.imageView = (AutoImageView)v.findViewById(R.id.main_image_view);
            this.parentLayout = (RelativeLayout)v.findViewById(R.id.main_parentLayout);
            this.frameLayout = (FrameLayout)v.findViewById(R.id.main_frameLayout);
            if(ImagesAdapter.this.parameters.getToolbarColor() != 0) {
                Utils.setViewBackgroundColor(ImagesAdapter.this.activity, this.selectedImageView, ImagesAdapter.this.parameters.getToolbarColor());
            }
        }

        @Override
        public void onClick(View v) { //Poner para ampliar

            if (mCallback != null) mCallback.onClick(getAdapterPosition()); }

        @Override
        public boolean onLongClick(View v) {
            if (mCallback != null) mCallback.onLongClick(getAdapterPosition());
            return true;
        }

/*        public void setId(int position) {
            this.parentLayout.setId(position);
        }

        public void setTag(int resource_id, long id) {
            this.parentLayout.setTag(resource_id, Long.valueOf(id));*/
        //}
    }
}


