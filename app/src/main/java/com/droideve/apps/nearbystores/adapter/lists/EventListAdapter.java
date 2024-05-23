package com.droideve.apps.nearbystores.adapter.lists;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.classes.Event;
import com.droideve.apps.nearbystores.utils.DateUtils;
import com.droideve.apps.nearbystores.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;

import io.realm.RealmList;


public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Event> data;
    private Context context;
    private ClickListener clickListener;
    private boolean isHorizontalList = false;
    private float width = 0, height = 0;


    public EventListAdapter(Context context, List<Event> data, boolean isHorizontalList) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.isHorizontalList = isHorizontalList;
    }

    public EventListAdapter(Context context, List<Event> data, boolean isHorizontalList, float width, float height) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.isHorizontalList = isHorizontalList;
        this.width = width;
        this.height = height;
    }

    @Override
    public EventListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = null;
        if (isHorizontalList) rootView = infalter.inflate(R.layout.v3_item_event, parent, false);
        else rootView = infalter.inflate(R.layout.fragment_custom_item_event, parent, false);

        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }


    @SuppressLint("StringFormatInvalid")
    @Override
    public void onBindViewHolder(EventListAdapter.mViewHolder holder, int position) {


        if (height > 0 && width > 0) {
            //set set the dp dimention
            int dp1 = Utils.dip2pix(context, 1);
            CardView.LayoutParams params = new CardView.LayoutParams((int) width, (int) height);
            params.setMargins((5 * dp1), (5 * dp1), (5 * dp1), (5 * dp1));
            holder.itemView.setLayoutParams(params);
        }


        if (this.data.get(position).getListImages() != null && this.data.get(position).getListImages()
                .get(0).getUrl200_200() != null) {
            
            Glide.with(context)
                    .load(this.data.get(position).getListImages()
                            .get(0).getUrl200_200())
                    .dontTransform()
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .into(holder.image);

        } else {
            Glide.with(context).load(R.drawable.def_logo).into(holder.image);
        }

        if (this.data.get(position).getListImages() == null)
            if (data.get(position).getType() == 1 && data.get(position).getType() == 2) {
                holder.image.setImageResource(R.drawable.def_logo);
            } else if (data.get(position).getType() == 3) {
                holder.image.setImageResource(R.drawable.def_logo);
            }

        if (this.data.get(position).getListImages().size() > 0) {
            Glide.with(context)
                    .load(this.data.get(position).getListImages().get(0).getUrl500_500())
                    .into(holder.image);
        } else {
            Glide.with(context).load(R.drawable.def_logo)
                    .into(holder.image);
        }

        if (DateUtils.isLessThan24(this.data.get(position).getDateB(), null)) {
            holder.upcoming.setVisibility(View.VISIBLE);
        } else {
            holder.upcoming.setVisibility(View.GONE);
        }


        holder.name.setText(data.get(position).getName());

        Drawable arrowIcon = context.getResources().getDrawable(R.drawable.ic_location);

        DrawableCompat.setTint(
                DrawableCompat.wrap(arrowIcon),
                ContextCompat.getColor(context, R.color.colorPrimary)
        );


        holder.address.setCompoundDrawablePadding(10);

        if (AppController.isRTL()) {
            holder.address.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowIcon, null);
        } else {
            holder.address.setCompoundDrawablesWithIntrinsicBounds(arrowIcon, null, null, null);
        }

        holder.address.setText(data.get(position).getAddress());

        if (data.get(position).getDistance() > 0 && this.data.get(position).getLat() != 0 && this.data.get(position).getLng() != 0) {

            SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
            String distance_unit = sh.getString("distance_unit", "km");

            if (distance_unit.equals("km")) {
                if (Utils.isNearMAXDistanceKM(this.data.get(position).getDistance())) {
                    holder.distance.setText(
                            Utils.prepareDistanceKm(this.data.get(position).getDistance())
                                    + " " +
                                    Utils.getDistanceByKm(this.data.get(position).getDistance())
                    );

                    holder.distance.setVisibility(View.VISIBLE);
                } else if (!Utils.isNearMAXDistanceKM(this.data.get(position).getDistance())) {
                    holder.distance.setText(String.format(context.getString(R.string.distance_100), distance_unit));
                    holder.distance.setVisibility(View.VISIBLE);
                } else {
                    holder.distance.setVisibility(View.GONE);
                }

            } else {
                if (Utils.isNearMAXDistanceKM(this.data.get(position).getDistance())) {
                    holder.distance.setText(
                            Utils.prepareDistanceMiles(this.data.get(position).getDistance())
                                    + " " +
                                    Utils.getDistanceMiles(this.data.get(position).getDistance())
                    );
                    holder.distance.setVisibility(View.VISIBLE);
                } else if (!Utils.isNearMAXDistanceMiles(this.data.get(position).getDistance())) {
                    holder.distance.setText(String.format(context.getString(R.string.distance_100), distance_unit));
                    holder.distance.setVisibility(View.VISIBLE);
                } else {
                    holder.distance.setVisibility(View.GONE);
                }
            }


            holder.distance.setText(holder.distance.getText().toString().toLowerCase());

        } else {
            holder.distance.setVisibility(View.GONE);
        }


        //


        if (data.get(position).getFeatured() == 0) {
            holder.featured.setVisibility(View.GONE);
        } else {
            holder.featured.setVisibility(View.VISIBLE);
        }

        if (data.get(position).getDateB() != null && !data.get(position).getDateB().equals("")) {
            holder.day_calendar.setText(DateUtils.getDateByTimeZone(data.get(position).getDateB(), "dd"));
            holder.month_calendar.setText(DateUtils.getDateByTimeZone(data.get(position).getDateB(), "MMM"));
        }

        if (holder.join_button != null && holder.joined_button != null) {
            if (data.get(position).getSaved() == 0) {
                holder.join_button.setVisibility(View.VISIBLE);
                holder.joined_button.setVisibility(View.GONE);
            } else {
                holder.join_button.setVisibility(View.GONE);
                holder.joined_button.setVisibility(View.VISIBLE);
            }
        }


        if (data.get(position).getSaved()==1) {
            holder.likeButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_favourite,null));
        } else {
            holder.likeButton.setImageDrawable(ResourcesCompat.getDrawable(context.getResources(),R.drawable.ic_favourite_outline,null));
        }

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(clickListener != null){
                    clickListener.likeClicked(holder.likeButton, position);
                }
            }
        });
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

    public Event getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }

    public void addAllItems(RealmList<Event> list) {

        data.addAll(list);
        notifyDataSetChanged();

    }


    public void addItem(Event item) {

        int index = (data.size());
        data.add(item);
        notifyDataSetChanged();
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
        void likeClicked(View view, int position);
    }

    public class mViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public ImageView image;
        public TextView name;
        public TextView address;
        public TextView distance;
        //public ImageView location;
        public ImageView featured;
        public TextView upcoming;
        public TextView day_calendar, month_calendar;
        public FloatingActionButton joined_button, join_button;
        public ImageView likeButton;



        public mViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            //location = (ImageView) itemView.findViewById(R.id.location);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            distance = itemView.findViewById(R.id.distance);
            featured = itemView.findViewById(R.id.featured);
            upcoming = itemView.findViewById(R.id.upcoming);
            day_calendar = itemView.findViewById(R.id.day_calendar);
            month_calendar = itemView.findViewById(R.id.month_calendar);
            joined_button = itemView.findViewById(R.id.joined_button);
            join_button = itemView.findViewById(R.id.join_button);
            likeButton = itemView.findViewById(R.id.likeButton);


            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {


            if (clickListener != null) {
                clickListener.itemClicked(v, getPosition());
            }

            //delete(getPosition());


        }
    }


}
