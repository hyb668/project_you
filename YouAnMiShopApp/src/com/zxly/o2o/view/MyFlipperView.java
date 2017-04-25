package com.zxly.o2o.view;

import com.zxly.o2o.shop.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ViewFlipper;

/**
 * Created by Administrator on 2015/4/21.
 */
public class MyFlipperView extends ViewFlipper {
    private Context context;
    //    private OnAgainListener onAgainListener;

    public MyFlipperView(Context context) {
        super(context);
        this.context = context;

        startAnim();

    }

    public View getRetryBtn(){
        return findViewById(R.id.loading_fail_layout);
    }

    public void setDisplayedChild(int whichChild, boolean isShowAnim) {
        if (isShowAnim)
            startAnim();
        super.setDisplayedChild(whichChild);
        cleanAnim();
    }

    public MyFlipperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        startAnim();
    }


    public void cleanAnim() {
        this.setInAnimation(context, R.anim.aphoa_nomal);
        this.setOutAnimation(context, R.anim.aphoa_nomal);
    }

    public void startAnim() {
        this.setInAnimation(context, R.anim.alpha_in_anim);
        this.setOutAnimation(context, R.anim.alpha_out_anim);
    }


}
