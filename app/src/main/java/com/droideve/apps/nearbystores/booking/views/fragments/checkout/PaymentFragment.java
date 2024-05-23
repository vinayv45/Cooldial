package com.droideve.apps.nearbystores.booking.views.fragments.checkout;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;

import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.customView.AlertBottomSheetDialog;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.droideve.apps.nearbystores.utils.NSLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.adapter.lists.PayGWAdapter;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.controllers.PaymentController;
import com.droideve.apps.nearbystores.booking.controllers.adapters.PayGWParser;
import com.droideve.apps.nearbystores.booking.modals.Fee;
import com.droideve.apps.nearbystores.booking.modals.PaymentGateway;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.utils.OfferUtils;
import com.droideve.apps.nearbystores.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.droideve.apps.nearbystores.utils.NSLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;
import static com.droideve.apps.nearbystores.booking.views.activities.BookingCheckoutActivity.bookingAmount;


public class PaymentFragment extends Fragment implements PayGWAdapter.ClickListener {


    @BindView(R.id.list_payment)
    RecyclerView listPayment;

    @BindView(R.id.payment_detail_layout)
    LinearLayout paymentDetailLayout;

    @BindView(R.id.layout_fees)
    LinearLayout layoutFees;


    @BindView(R.id.layout_subtotal)
    LinearLayout layoutSubtotal;

    @BindView(R.id.subtotal_val)
    TextView subtotalVal;

    @BindView(R.id.layout_total)
    LinearLayout layoutTotal;

    @BindView(R.id.total_value)
    TextView totalValue;


    private PayGWAdapter mAdapter;
    private Context mContext;

    public static int paymentChoosed = -1;


    public PaymentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_payment, container, false);
        mContext = root.getContext();
        ButterKnife.bind(this, root);


        setupAdapter();

        return root;
    }


    public List<PaymentGateway> getData() {

        List<PaymentGateway> results = new ArrayList<>();

        RealmList<PaymentGateway> listCats = PaymentController.list();

        for (PaymentGateway cat : listCats) { //mPG.getId() != 10012
            if (cat.getId() != 10012)
                results.add(cat);
        }


        return results;
    }

    @Override
    public void onStart() {
        super.onStart();

        getPaymentGatewayFromAPI();
    }


    private void setupAdapter() {
        mAdapter = new PayGWAdapter(getActivity(), getData());
        mAdapter.setClickListener(this);
        listPayment.setHasFixedSize(true);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listPayment.setLayoutManager(mLayoutManager);
        listPayment.setAdapter(mAdapter);


        ((SimpleItemAnimator) listPayment.getItemAnimator()).setSupportsChangeAnimations(false);

    }

    @Override
    public void itemClicked(View view, int position) {

        paymentDetailLayout.setVisibility(View.VISIBLE);

        PaymentGateway mPG = mAdapter.getItemDetail(position);
        paymentChoosed = mPG.getId();


        //sum of all the item added in the card ( card = sum(amount * qte)
        double calculateTotal = bookingAmount;
        double totalFees = 0; //init total fees to 1


        subtotalVal.setText(OfferUtils.parseCurrencyFormat(
                (float) calculateTotal,
                OfferUtils.defaultCurrency()));

        //check fees
        if (mPG.getFees() != null && mPG.getFees().size() > 0) {
            for (Fee mFee : mPG.getFees()) {

                //fixing bug related to duplicated payment detail
                layoutFees.removeAllViews();

                //Build Layout item Fee
                LinearLayout itemLayoutFee = new LinearLayout(mContext);
                itemLayoutFee.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams itemLayoutFeeLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemLayoutFeeLP.setMargins(Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5), Utils.dpToPx(5));
                itemLayoutFee.setLayoutParams(itemLayoutFeeLP);

                //Build Fee Name
                TextView feeName = new TextView(view.getContext());
                LinearLayout.LayoutParams feeNameLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                feeNameLP.weight = 1;
                feeName.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                feeName.setLayoutParams(feeNameLP);
                feeName.setTypeface(feeName.getTypeface(), Typeface.BOLD);
                itemLayoutFee.addView(feeName);

                //Build Fee Value
                TextView feeValue = new TextView(view.getContext());
                LinearLayout.LayoutParams feeValueLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                feeValue.setLayoutParams(feeValueLP);
                feeValue.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                feeValue.setTypeface(feeName.getTypeface(), Typeface.BOLD);
                itemLayoutFee.addView(feeValue);


                //calculate total fees
                double feesValue = ((float) (mFee.getValue() / 100) * calculateTotal);
                totalFees += feesValue;

                feeName.setText(mFee.getName() + "(" + String.format("%s%%", (int) mFee.getValue()) + ")");

                feeValue.setText(
                        OfferUtils.parseCurrencyFormat(
                                (float) feesValue,
                                OfferUtils.defaultCurrency()));

                layoutFees.addView(itemLayoutFee);

            }


        }


        //add fees
        if (totalFees > 0)
            calculateTotal = calculateTotal + totalFees;

        totalValue.setText(OfferUtils.parseCurrencyFormat(
                (float) calculateTotal,
                OfferUtils.defaultCurrency()));


        //popup for transfer bank
        if(paymentChoosed == 10013){

            AlertBottomSheetDialog alert = AlertBottomSheetDialog.newInstance(getActivity())
                    .setlisteners(new AlertBottomSheetDialog.Listeners() {
                @Override
                public void onConfirm() {

                }
                @Override
                public void onDismiss() {

                }
            });
            alert.titleView().setText(getString(R.string.payment));
            alert.bodyView().setText(getString(R.string.selected_transferBank));
            alert.show();

        }
    }


    private void getPaymentGatewayFromAPI() {

        ApiRequest.newPostInstance(Constances.API.API_PAYMENT_GATEWAY, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                final PayGWParser mPayGWParser = new PayGWParser(parser);
                if (mPayGWParser.getSuccess() == 1 && mPayGWParser.getPaymentGetway().size() > 0) {
                    mAdapter.addAll(mPayGWParser.getPaymentGetway());
                    mAdapter.notifyDataSetChanged();
                    PaymentController.insertPaymentGatewayList(
                            mPayGWParser.getPaymentGetway()
                    );
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {
                MessageDialog.showMessage(getActivity(), errors);
            }
        }, Map.of(
                "user_id", String.valueOf(SessionsController.getSession().getUser().getId())
        ));


    }

}
