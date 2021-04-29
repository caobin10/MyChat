package com.mstr.letschat.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;

/**
 * Created by Administrator on 2021/4/19 0019.
 */

public abstract class FileMessageView extends MessageView {
    protected TextView fileText;

    public FileMessageView(Context context) {
        this(context, null);
    }

    public FileMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFileText(String text) {
        fileText.setText(text);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        fileText = (TextView)findViewById(R.id.tv_file);

        setOrientation(LinearLayout.VERTICAL);
    }
}
