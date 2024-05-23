package com.droideve.apps.nearbystores.adapter.lists;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Coupon;
import com.droideve.apps.nearbystores.classes.Coupon;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class CouponsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Coupon> items = new ArrayList<>();
    private Context ctx;
    private ClickListener mClickListener;

    public CouponsAdapter(Context context, List<Coupon> items) {
        this.items = items;
        ctx = context;
    }

    public List<Coupon> getItems() {
        return items;
    }

    public void setItems(List<Coupon> items) {
        this.items = items;
    }

    public void setClickListener(final ClickListener mItemClickListener) {
        this.mClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon, parent, false);
        vh = new CouponViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof CouponViewHolder) {
            CouponViewHolder view = (CouponViewHolder) holder;
            final Coupon object = items.get(position);

            ((CouponViewHolder) holder).label.setText(object.getLabel());
            ((CouponViewHolder) holder).coupon_description.setText(
                    String.format(ctx.getString(R.string.coupon_code), object.getCode())
            );

            if(object.getStatus() == 0){
                ((CouponViewHolder) holder).status.setText(ctx.getString(R.string.coupon_unverified));
                ((CouponViewHolder) holder).status.setTextColor(ResourcesCompat.getColor(ctx.getResources(),R.color.orange_600,null));
            }else  if(object.getStatus() == 1){
                ((CouponViewHolder) holder).status.setText(ctx.getString(R.string.coupon_verified));
                ((CouponViewHolder) holder).status.setTextColor(ResourcesCompat.getColor(ctx.getResources(),R.color.green,null));
            }else  if(object.getStatus() == 2){
                ((CouponViewHolder) holder).status.setText(ctx.getString(R.string.coupon_used));
                ((CouponViewHolder) holder).status.setTextColor(ResourcesCompat.getColor(ctx.getResources(),R.color.blue,null));
            }else{
                ((CouponViewHolder) holder).status.setText(ctx.getString(R.string.coupon_canceled));
                ((CouponViewHolder) holder).status.setTextColor(ResourcesCompat.getColor(ctx.getResources(),R.color.red,null));
            }

            if (object.getImage() != null) {
                Glide.with(ctx)
                        .load(object.getImage().getUrl200_200())
                        .centerCrop().placeholder(R.drawable.def_logo)
                        .into(view.image);
            } else {
                Glide.with(ctx).load(R.drawable.def_logo)
                        .centerCrop().into(view.image);
            }

        }
    }

    public void addItem(Coupon item) {
        if (item != null) {
            if (items.add(item)) {
                notifyDataSetChanged();
            }
        }

    }

    public void updateItem(final int position, final Coupon item) {
        if (item != null && position >= 0) {
            items.set(position, item);
            notifyDataSetChanged();
        }
    }

    public void removeItem(final int noti_id, final int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    public void addAll(final List<Coupon> productList) {
        items.addAll(productList);
        notifyDataSetChanged();
    }

    public void removeAll() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public interface ClickListener {
        void onItemClick(View view, int pos);

        void onMoreButtonClick(View view, int position);

    }

    public class CouponViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView container;
        LinearLayout layout;
        ImageView image;
        TextView label;
        TextView coupon_description;
        ImageView more;
        TextView status;


        public CouponViewHolder(View v) {
            super(v);

            container = v.findViewById(R.id.container);
            layout = v.findViewById(R.id.layout);
            image = v.findViewById(R.id.image);
            label = v.findViewById(R.id.label);
            coupon_description = v.findViewById(R.id.coupon_description);
            more = v.findViewById(R.id.menu);
            status = v.findViewById(R.id.status);

            //set click listeners
            container.setOnClickListener(this);
            more.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.container) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(view, getLayoutPosition());
                }
            } else if (view.getId() == R.id.menu) {
                if (mClickListener != null) {
                    mClickListener.onMoreButtonClick(view, getLayoutPosition());
                }
            }

        }
    }


}