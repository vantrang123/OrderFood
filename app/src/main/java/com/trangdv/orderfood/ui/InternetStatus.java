package com.trangdv.orderfood.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trangdv.orderfood.R;

public class InternetStatus extends BottomSheetDialogFragment {
    private BottomSheetBehavior behavior;
    private TextView tvStatus;

    public InternetStatus() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_notify_connect_internet, null);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());

        findViewById(view);
        return dialog;
    }

    private void findViewById(View view) {
        tvStatus = view.findViewById(R.id.tv_status_internet);
    }

    public void setText(String text) {
        tvStatus.setText(text);
    }

    private void closeBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }
}
