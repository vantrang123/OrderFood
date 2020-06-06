package com.trangdv.orderfood.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.model.eventbus.FoodDetailEvent;
import com.trangdv.orderfood.remote.IFCMService;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailActivity;
import com.trangdv.orderfood.ui.main.CartFragment;
import com.trangdv.orderfood.ui.main.MainActivity;
import com.trangdv.orderfood.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ClickItemCartDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private BottomSheetBehavior behavior;
    private TextView tvCancel;
    private TextView tvDelete;
    private TextView tvDetail;
    private Food food;
    private int position;
    private int foodId;
    DialogUtils dialogUtils;
    IFCMService mService;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ClickItemCartDialog(int position, int foodId) {
        this.position = position;
        this.foodId = foodId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_option_item_cart, null);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());

        findViewById(view);
        init();

        ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        return dialog;
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();
    }

    private void findViewById(View v) {
        tvDelete = v.findViewById(R.id.tv_delete);
        tvDelete.setOnClickListener(this);
        tvCancel = v.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);
        tvDetail = v.findViewById(R.id.tv_detail);
        tvDetail.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_delete:
                onDelete();
                closeBottomSheet();
                break;
            case R.id.tv_cancel:
                closeBottomSheet();
                break;
            case R.id.tv_detail:
                gotoFoodDetail();
                closeBottomSheet();
            default:
                break;

        }
    }

    private void gotoFoodDetail() {
        dialogUtils.showProgress(getContext());

        compositeDisposable.add(
                anNgonAPI.getFoodById(Common.API_KEY, foodId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(foodModel -> {
                            dialogUtils.dismissProgress();
                            EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodModel.getResult().get(0)));
                            startActivity(new Intent(getContext(), FoodDetailActivity.class));
                            getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                        }, throwable -> {
                            Toast.makeText(getActivity(), "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                        })
        );

    }

    private void onDelete() {
        Fragment fragment = ((MainActivity)getActivity()).getFragmentCurrent();
        if (fragment instanceof CartFragment) {
            ((CartFragment) fragment).removeItem(position);
//            ((CartFragment) fragment).showUndoDelete(food.getProductName(), position, food);
        }
    }

    private void closeBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
