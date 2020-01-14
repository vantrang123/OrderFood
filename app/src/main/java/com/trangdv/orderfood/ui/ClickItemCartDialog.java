package com.trangdv.orderfood.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailActivity;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailFragment;

public class ClickItemCartDialog extends BottomSheetDialogFragment implements View.OnClickListener{
    private BottomSheetBehavior behavior;
    private TextView tvCancel;
    private TextView tvDelete;
    private TextView tvDetail;
    private Order order;
    private int position;

    public ClickItemCartDialog(int position, Order order) {
        this.order = order;
        this.position = position;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_option_item_cart, null);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());


        findViewById(view);
        ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        return dialog;
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
                onDetail();
                closeBottomSheet();
            default:
                break;

        }
    }

    private void onDetail() {
        Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("productId", order.getProductId());
        bundle.putInt("quantity", Integer.valueOf(order.getQuanlity()));
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void onDelete() {
        Fragment fragment = ((MainActivity)getActivity()).getFragmentCurrent();
        if (fragment instanceof CartFragment) {
            ((CartFragment) fragment).removeItem(position, order.getProductId());
            ((CartFragment) fragment).showUndoDelete(order.getProductName(), position, order);
        }
    }

    private void closeBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
