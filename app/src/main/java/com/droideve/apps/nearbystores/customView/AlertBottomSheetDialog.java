package com.droideve.apps.nearbystores.customView;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.droideve.apps.nearbystores.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AlertBottomSheetDialog {

    public static AlertBottomSheetDialog newInstance(Context ctx){
        AlertBottomSheetDialog mBottomSheetDialog = new AlertBottomSheetDialog(ctx);
        return mBottomSheetDialog;
    }

    private Listeners mlisteners;

    private BottomSheetDialog mBottomSheetDialog;

    public TextView titleView(){
        return mBottomSheetDialog.findViewById(R.id.title);
    }

    public TextView bodyView(){
        return mBottomSheetDialog.findViewById(R.id.body);
    }

    public AlertBottomSheetDialog(Context ctx){

        mBottomSheetDialog = new BottomSheetDialog(ctx);
        mBottomSheetDialog.setContentView(R.layout.bottom_sheet_alert_layout);
        mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        mBottomSheetDialog.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mlisteners != null){
                    mlisteners.onConfirm();
                }

                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog.findViewById(R.id.bt_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mlisteners != null){
                    mlisteners.onDismiss();
                }

                mBottomSheetDialog.dismiss();
            }
        });


    }

   public void show(){
        if(mBottomSheetDialog != null)
            mBottomSheetDialog.show();
    }

    public AlertBottomSheetDialog setlisteners(Listeners l){
        this.mlisteners = l;
        return this;
    }

    public interface Listeners{
        void onConfirm();
        void onDismiss();
    }
}
