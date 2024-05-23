package com.droideve.apps.nearbystores.adapter.lists;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.classes.Category;
import com.droideve.apps.nearbystores.classes.Images;
import com.droideve.apps.nearbystores.utils.Utils;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;
import java.util.Map;

import io.realm.RealmList;


public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Category> data;
    private Context context;
    private ClickListener clickListener;
    private boolean rectCategoryView = false;
    private Map<String, Object> optionalParams;
    private int selectedPos = RecyclerView.NO_POSITION;
    // Define an array like the following in your adapter
    private float width = 0, height = 0;
    private boolean selectedAfterAction;


    public CategoriesListAdapter(Context context, List<Category> data, boolean rectCategoryView, Map<String, Object> optionalParams, float width, float height, boolean selectedAfterAction) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.rectCategoryView = rectCategoryView;
        this.optionalParams = optionalParams;
        this.width = width;
        this.height = height;
        this.selectedAfterAction = selectedAfterAction;
    }

    @Override
    public CategoriesListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = null;
        if (rectCategoryView) {
            rootView = infalter.inflate(R.layout.v3_item_category, parent, false);
        } else {
            rootView = infalter.inflate(R.layout.v2_item_category_rect, parent, false);
        }

        mViewHolder holder = new mViewHolder(rootView);


        return holder;
    }


    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(final mViewHolder holder, final int position) {
        //resize image frame
        if (height > 0 && width > 0) {
            //set set the dp dimension
            int dp1 = Utils.dip2pix(context, 1);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) width, (int) height);
            params.setMargins((5 * dp1), (5 * dp1), (5 * dp1), (5 * dp1));
        }

        if (optionalParams != null && optionalParams.containsKey("displayCatTitle") && !((Boolean) optionalParams.get("displayCatTitle"))) {
            holder.name.setVisibility(View.GONE);
        } else if (rectCategoryView) {
            holder.name.setVisibility(View.VISIBLE);
        } else {
            holder.name.setVisibility(View.VISIBLE);
        }

        holder.name.setText(data.get(position).getNameCat());

        Images mainImg = null;

        if (rectCategoryView && data.get(position).getLogo() != null && !data.get(position).getLogo().equals("")) {
            if (optionalParams != null && optionalParams.containsKey("displayCatTitle") && !((Boolean) optionalParams.get("displayCatTitle"))) {
                mainImg = data.get(position).getImages();
            } else {
                mainImg = data.get(position).getLogo();
            }
        } else if (data.get(position).getImages() != null) {
            mainImg = data.get(position).getImages();
        }

        if (mainImg != null)
            Glide.with(context)
                    .asBitmap()
                    .load(mainImg.getUrl500_500())
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            if (AppController.isRTL()) {
                                resource = Utils.flip(resource);
                            }
                            holder.image.setImageBitmap(resource);
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                            holder.image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.def_logo, null));

                        }
                    });
        else
            holder.image.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(), R.drawable.def_logo, null));

        if (optionalParams != null && optionalParams.containsKey("displayStoreNumber") && !((Boolean) optionalParams.get("displayStoreNumber"))) {
            holder.stores.setVisibility(View.GONE);
        } else {
            holder.stores.setVisibility(View.VISIBLE);
            holder.stores.setText(String.format(
                    context.getString(R.string.nbr_stores_message),
                    data.get(position).getNbr_stores()
            ));
        }

        if (selectedPos == position){
            holder.linear.setBackgroundColor(ContextCompat.getColor(context,R.color.colorAccent));
            holder.name.setTextColor(ContextCompat.getColor(context,R.color.white));
            holder.stores.setTextColor(ContextCompat.getColor(context,R.color.white));
        }else {
            holder.linear.setBackgroundColor(ContextCompat.getColor(context,R.color.card_bg));
            holder.name.setTextColor(ContextCompat.getColor(context,R.color.black_text_color));
            holder.stores.setTextColor(ContextCompat.getColor(context,R.color.grey_text_color));
        }
    }


    public void setSelectedPos(final int pos) {
        if (rectCategoryView) {
            selectedPos = pos;
            notifyItemChanged(selectedPos);
        }

    }


    public Category getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }


    public void clear() {

        data = new ArrayList<Category>();
        notifyDataSetChanged();

    }

    public void addItem(Category item) {

        int index = (data.size());
        data.add(item);
        notifyItemInserted(index);
    }

    public void addAllItems(RealmList<Category> listCats) {

        data.addAll(listCats);
        notifyDataSetChanged();

    }

    public void removeAll() {
        int size = this.data.size();

        if (size > 0) {
            for (int i = 0; i < size; i++) {
                this.data.remove(0);
            }

            if (size > 0)
                this.notifyItemRangeRemoved(0, size);

        }


    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clicklistener) {

        this.clickListener = clicklistener;

    }


    public interface ClickListener {
        void itemClicked(View view, int position);
    }

    public class mViewHolder extends RecyclerView.ViewHolder {


        public TextView name;
        public ImageView image;
        public TextView stores;
        public View mainLayout;
        public View linear;
        /*public View colorImgFilter;
        public View frameImage;*/


        /*public LinearLayout transparency_rec_filter;
        public TextView transparency_rec_filter_text;*/


        public mViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.cat_name);
            image = itemView.findViewById(R.id.image);
            stores = itemView.findViewById(R.id.stores);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            linear = itemView.findViewById(R.id.linear);
            /*colorImgFilter = itemView.findViewById(R.id.colorImgFilter);
            frameImage = itemView.findViewById(R.id.frame_image);*/

            /*transparency_rec_filter = itemView.findViewById(R.id.transparency_rec_filter);
            transparency_rec_filter_text = itemView.findViewById(R.id.transparency_rec_filter_text);*/

            mainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.itemClicked(view, getLayoutPosition());

                        if (rectCategoryView) {
                            notifyItemChanged(selectedPos);
                            selectedPos = getLayoutPosition();
                            notifyItemChanged(selectedPos);
                        }
                    }
                }
            });
        }


    }


}