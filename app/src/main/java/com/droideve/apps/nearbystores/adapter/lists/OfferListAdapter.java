package com.droideve.apps.nearbystores.adapter.lists;

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
import com.droideve.apps.nearbystores.classes.Offer;
import com.droideve.apps.nearbystores.utils.OfferUtils;
import com.droideve.apps.nearbystores.utils.Utils;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.text.DecimalFormat;
import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;

import io.realm.RealmList;


public class OfferListAdapter extends RecyclerView.Adapter<OfferListAdapter.mViewHolder> {


    private LayoutInflater infalter;
    private List<Offer> data;
    private Context context;
    private ClickListener clickListener;
    private boolean isHorizontalList = false;
    private float width = 0, height = 0;

    public OfferListAdapter(Context context, List<Offer> data, boolean isHorizontalList) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.isHorizontalList = isHorizontalList;
    }

    public OfferListAdapter(Context context, List<Offer> data, boolean isHorizontalList, float width, float height) {
        this.data = data;
        this.infalter = LayoutInflater.from(context);
        this.context = context;
        this.isHorizontalList = isHorizontalList;
        this.width = width;
        this.height = height;
    }

    @Override
    public OfferListAdapter.mViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rootView = null;
        if (isHorizontalList) rootView = infalter.inflate(R.layout.v3_item_offer, parent, false);
        else rootView = infalter.inflate(R.layout.fragment_custom_item_offer, parent, false);


        mViewHolder holder = new mViewHolder(rootView);

        return holder;
    }


    @Override
    public void onBindViewHolder(OfferListAdapter.mViewHolder holder, int position) {


        if (height > 0 && width > 0) {
            //set set the dp dimention
            int dp1 = Utils.dip2pix(context, 1);
            CardView.LayoutParams params = new CardView.LayoutParams((int) width, (int) height);
            params.setMargins((5 * dp1), (5 * dp1), (5 * dp1), (5 * dp1));
            //holder.itemView.setLayoutParams(params);
        }


        if (data.get(position).getValue_type() != null && !data.get(position).getValue_type().equals("")) {
            if (data.get(position).getValue_type().equalsIgnoreCase("Percent") && (data.get(position).getOffer_value() > 0 || data.get(position).getOffer_value() < 0)) {
                DecimalFormat decimalFormat = new DecimalFormat("#0");
                holder.offer.setText(decimalFormat.format(data.get(position).getOffer_value()) + "%");
            } else if (data.get(position).getValue_type().equalsIgnoreCase("Price") && data.get(position).getOffer_value() != 0) {
                holder.offer.setText(OfferUtils.parseCurrencyFormat(
                        data.get(position).getOffer_value(),
                        data.get(position).getCurrency()));
            } else {
                holder.offer.setText(context.getString(R.string.promo));
            }

            holder.offer.setVisibility(View.VISIBLE);

        }else{
            holder.offer.setVisibility(View.GONE);
        }

        if (data.get(position).getValue_type().equalsIgnoreCase("unspecifie")) {
            holder.offer.setText(context.getString(R.string.promo));

        }


        holder.name.setText(data.get(position).getName());
        holder.address.setText(data.get(position).getStore_name());
        holder.description.setText(data.get(position).getDescription());

        Drawable arrowIcon = context.getResources().getDrawable(R.drawable.ic_location);

        DrawableCompat.setTint(
                DrawableCompat.wrap(arrowIcon),
                ContextCompat.getColor(context, R.color.colorPrimary)
        );



        if (!isHorizontalList) {
            holder.address.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        } else {
            if (!AppController.isRTL())
                holder.address.setCompoundDrawablesWithIntrinsicBounds(arrowIcon, null, null, null);
            else
                holder.address.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowIcon, null);

        }

        holder.address.setCompoundDrawablePadding(14);

        if (data.get(position).getImages() != null && data.get(position).getImages().getUrl500_500() != null) {
            Glide.with(context).load(data.get(position).getImages().getUrl500_500())
                    .placeholder(ImageLoaderAnimation.glideLoader(context))
                    .dontTransform()
                    .into(holder.image);
        } else {

            Glide.with(context).load(R.drawable.def_logo)
                    .into(holder.image);
        }


        if (data.get(position).getFeatured() == 0) {
            holder.featured.setVisibility(View.GONE);
        } else {
            holder.featured.setVisibility(View.VISIBLE);
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

    }


    public void addAllItems(RealmList<Offer> list) {

        data.addAll(list);
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

    public Offer getItem(int position) {

        try {
            return data.get(position);
        } catch (Exception e) {
            return null;
        }

    }


    public void addItem(Offer item) {

        int index = (data.size());
        data.add(item);
        notifyDataSetChanged();
        //notifyItemInserted(index);
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
        //public TextView distance;
        public TextView offer;
        public ImageView featured;
        public TextView description;
        public TextView distance;
        public ImageView likeButton;



        public mViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            //distance = (TextView) itemView.findViewById(R.id.distance);
            offer = itemView.findViewById(R.id.offer);
            featured = itemView.findViewById(R.id.featured);
            description = itemView.findViewById(R.id.description);
            likeButton = itemView.findViewById(R.id.likeButton);
            distance = itemView.findViewById(R.id.distance);

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
