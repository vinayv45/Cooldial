package com.droideve.apps.nearbystores.adapter.pager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.classes.Banner;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import com.droideve.apps.nearbystores.utils.NSLog;

import me.huseyinozer.TooltipIndicator;

public class BannerAdapter extends PagerAdapter {
    private Context mContext;
    private List<Banner> banners;
    private int width = 0, height = 0;
    private OnItemClickListener onItemClickListener;
    private ViewPager viewPager;

    // constructor
    public BannerAdapter(Context context, List<Banner> banners, ViewPager viewpager) {
        this.mContext = context;
        this.banners = banners;
        this.viewPager = viewpager;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getCount() {
        return this.banners.size();
    }

    public Banner getItem(int pos) {
        return banners.get(pos);
    }

    public void setbanners(List<Banner> banners) {
        this.banners = banners;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Banner o = banners.get(position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.v3_slider_item_image, container, false);


        ImageView banner = v.findViewById(R.id.image);
        MaterialRippleLayout lyt_parent = v.findViewById(R.id.lyt_parent);
        TooltipIndicator tooltip_indicator = v.findViewById(R.id.tooltip_indicator);
        Button btnOffer = v.findViewById(R.id.btnOffer);
        //Tools.displayImageOriginal(act, banner, o.getListImages().get(0).getUrl200_200());

        if (height > 0 && width > 0) {
            //set set the dp dimention
            int dp1 = Utils.dip2pix(mContext, 1);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width * dp1, height * dp1);
            params.setMargins((5 * dp1), (5 * dp1), (5 * dp1), (5 * dp1));
            lyt_parent.setLayoutParams(params);
        }

        if (o.getListImages().size() > 0 && o.getListImages().get(0) != null) {
            Glide.with(mContext)
                    .load(o.getListImages().get(0).getUrl500_500())
                    .placeholder(R.drawable.def_logo)
                    .into(banner);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.def_logo)
                    .placeholder(R.drawable.def_logo)
                    .into(banner);
        }


        ((TextView) v.findViewById(R.id.title)).setText(o.getTitle());
        //((TextView) v.findViewById(R.id.description)).setText(o.getDescription());
        //addBottomDots((LinearLayout) v.findViewById(R.id.layout_dots), getCount(), position);

        tooltip_indicator.setupViewPager(viewPager);


        lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, o);
                }
            }
        });

        container.addView(v);

        return v;
    }

    private void addBottomDots(LinearLayout layout_dots, int size, int current) {
        ImageView[] dots = new ImageView[size];

        layout_dots.removeAllViews();
        if (dots != null && dots.length > 0)
            for (int i = 0; i < dots.length; i++) {
                dots[i] = new ImageView(mContext);
                int width_height = 15;
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(width_height, width_height));
                params.setMargins(10, 10, 10, 10);
                dots[i].setLayoutParams(params);
                dots[i].setImageResource(R.drawable.shape_circle_outline);
                layout_dots.addView(dots[i]);
            }

        if (dots.length > 0) {
            dots[current].setImageResource(R.drawable.shape_circle);
        }
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Banner obj);
    }

}
