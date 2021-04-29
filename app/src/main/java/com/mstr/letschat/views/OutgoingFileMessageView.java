package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.mstr.letschat.R;

/**
 * Created by Administrator on 2021/4/19 0019.
 */

public class OutgoingFileMessageView extends FileMessageView {
    private ProgressBar progressBar;
    public OutgoingFileMessageView(Context context) {
        this(context, null);
    }
    public OutgoingFileMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void init(Context context) {
        super.init(context);

        progressBar = (ProgressBar)findViewById(R.id.sending_progress);
        progressBar.setVisibility(View.GONE);
    }
    protected int getLayoutResource() {
        return R.layout.outgoing_file_view;
    }

    @Override
    public void showProgress(boolean sent) {
        if (sent) {
//            progressBar.setVisibility(View.INVISIBLE);
        } else {
//            progressBar.setVisibility(View.VISIBLE);
        }
    }
}
