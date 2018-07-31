package com.fnspl.hiplaedu_student.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fnspl.hiplaedu_student.R;

/**
 * Created by FNSPL on 1/14/2018.
 */

public class Dialogs {

    static boolean isDialogShowing = false;

    public static void dialogFetchImage(Context context,
                                        final OnOptionSelect _callback) {

        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_fetch_image);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        final TextView fetchImage_tvOpenCamera = (TextView) dialog.findViewById(R.id.fetchImage_tvOpenCamera);
        final TextView fetchImage_tvOpenGallery = (TextView) dialog.findViewById(R.id.fetchImage_tvOpenGallery);
        final TextView fetchImage_tvCancel = (TextView) dialog.findViewById(R.id.fetchImage_tvCancel);

        fetchImage_tvOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                _callback.openNavigine();
                isDialogShowing=false;
            }
        });
        fetchImage_tvOpenGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                _callback.openNormalApp();
                isDialogShowing=false;
            }
        });
        fetchImage_tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShowing=false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }

    public interface OnOptionSelect {
        void openNavigine();

        void openNormalApp();
    }

}
