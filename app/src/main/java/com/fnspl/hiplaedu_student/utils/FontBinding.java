package com.fnspl.hiplaedu_student.utils;

import android.databinding.BindingAdapter;
import android.widget.TextView;

/**
 * Created by FNSPL on 3/6/2018.
 */

public class FontBinding {

    @BindingAdapter({"bind:font"})
    public static void setFont(TextView textView, String fontName) {
        textView.setTypeface(CustomFontFamily.getInstance().getFont(fontName));
    }

}
