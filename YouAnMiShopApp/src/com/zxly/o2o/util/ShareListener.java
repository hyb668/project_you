package com.zxly.o2o.util;

import com.tencent.tauth.UiError;

/**
 * Created by dsnx on 2015/7/16.
 */
public interface ShareListener {

    void onComplete(Object var1);

    void onFail(int errorCode);

}
