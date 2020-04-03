package com.trangdv.orderfood.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.ui.orderdetail.OrderDetailActivity;
import com.trangdv.orderfood.ui.orderdetail.OrderStatusFragment;

public class ConfirmUpdateOrderStatusDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private TextView tvNo;
    private TextView tvYes;
    private BottomSheetBehavior behavior;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_confirm_update_order_status, null);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());

        findViewById(view);
        tvYes.setOnClickListener(this);
        tvNo.setOnClickListener(this);
        ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        return dialog;
    }

    private void findViewById(View view) {
        tvYes = view.findViewById(R.id.tv_yes);
        tvNo = view.findViewById(R.id.tv_no);
    }

    private void closeBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onClick(View v) {
        OrderStatusFragment orderStatusFragment = (OrderStatusFragment) ((OrderDetailActivity)getActivity()).getFragmentCurrent();
        switch (v.getId()) {
            case R.id.tv_yes:
                closeBottomSheet();
                orderStatusFragment.updateOrderStatus();
                break;
            case R.id.tv_no:
                closeBottomSheet();
                orderStatusFragment.initStatus();
                break;
            default:
                break;
        }
    }
}
