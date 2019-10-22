package com.trangdv.orderfood.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.trangdv.orderfood.R;

public class ClickItemCartDialog extends BottomSheetDialogFragment {
    private BottomSheetBehavior behavior;
    private TextView tvCancel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.bottom_sheet_dialog, null);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());
        tvCancel = view.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                Toast.makeText(getActivity(), "ok!", Toast.LENGTH_SHORT).show();
            }
        });
        ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

}
