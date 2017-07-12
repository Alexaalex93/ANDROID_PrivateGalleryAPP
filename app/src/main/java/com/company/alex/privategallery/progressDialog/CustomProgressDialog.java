package com.company.alex.privategallery.progressDialog;

/**
 * Created by Alex on 15/05/2017.
 */

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.vlk.multimager.R.attr;
import com.vlk.multimager.R.id;
import com.vlk.multimager.R.layout;

public class CustomProgressDialog extends Dialog {
    TextView messageTextView;
    ProgressBar progressBar;
    private String message;

    public CustomProgressDialog(Context context) {
        super(context);
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        this.setContentView(layout.custom_progress_dialog);
        this.messageTextView = (TextView)this.findViewById(id.messageTextView);
        this.progressBar = (ProgressBar)this.findViewById(id.waitProgressBar);
        this.progressBar.getIndeterminateDrawable().setColorFilter(this.fetchAccentColor(), Mode.SRC_IN);
        this.getWindow().setLayout(-1, -2);
    }

    private int fetchAccentColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = this.getContext().obtainStyledAttributes(typedValue.data, new int[]{attr.colorAccent});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public void show() {
        super.show();
        this.messageTextView.setText(this.message);
    }

    public void dismiss() {
        super.dismiss();
    }
}
