
package com.yintong.pay.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.yintong.android.app.IPayService;
import com.yintong.android.app.IRemoteServiceCallback;
import com.yintong.secure.service.PayService;
import com.zxly.o2o.util.ParameCallBack;

import org.json.JSONException;
import org.json.JSONObject;

public class MobileSecurePayer {

    Integer lock = 0;
    IPayService payService = null;
    boolean mbPaying = false;
    static final String TAG = "MobileSecurePayer";

    Activity mActivity = null;

    // 和安全支付服务建立连接
    private ServiceConnection mSecurePayConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                //
                // wake up the binder to continue.
                // 获得通信通道
                synchronized (lock) {
                    payService = IPayService.Stub.asInterface(service);
                    lock.notify();
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            payService = null;
        }
    };

    /**
     * 向银通支付发送支付请求
     * 
     * @param strOrderInfo 订单信息
     * @param callback 回调handler
     * @param activity 目标activity
     * @param isTest 是否是测试环境，true为测试环境，但不推荐使用。
     * @return
     */
    public boolean payPreAuth(String strOrderInfo, final ParameCallBack callback,
            final Activity activity, boolean isTest) {

        return pay(strOrderInfo, callback, activity, "2", false, isTest);

    }

    

    /**
     * 向银通支付发送支付请求
     * 
     * @param strOrderInfo 订单信息
     * @param callback 回调handler
     * @param activity 目标activity
     * @param isTest 是否是测试环境，true为测试环境，但不推荐使用。
     * @return
     */

    public boolean paySign(String strOrderInfo, final ParameCallBack callback,
            final Activity activity, boolean isTest) {

        return pay(strOrderInfo, callback,  activity, "0", true, isTest);

    }
    
    
    /**
     * 向银通支付发送支付请求
     * 
     * @param strOrderInfo 订单信息
     * @param callback 回调handler
     * @param activity 目标activity
     * @param isTest 是否是测试环境，true为测试环境，但不推荐使用。
     * @return
     */

    public boolean pay(String strOrderInfo, final ParameCallBack callback,
             final Activity activity, boolean isTest) {

        return pay(strOrderInfo, callback, activity, "0", false, isTest);

    }

    /**
     * 向银通支付发送支付请求
     * 
     * @param strOrderInfo 订单信息
     * @param callback 回调handler
     * @param activity 目标activity
     * @param isTest 测试环境
     * @param signCard 单独签约
     * @return
     */
    public boolean pay(String strOrderInfo, final ParameCallBack callback,
           final Activity activity, String pay_product, boolean signCard,
            boolean isTest) {
        if (mbPaying)
            return false;
        mbPaying = true;

        try {
            if (isTest) {
                strOrderInfo = new JSONObject(strOrderInfo).put("test_mode", "1").toString();
            }
            if (signCard) {
                strOrderInfo = new JSONObject(strOrderInfo).put("sign_mode", "1").toString();
            }
            strOrderInfo = new JSONObject(strOrderInfo).put("pay_product", pay_product).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //
        mActivity = activity;

        // bind the service.
        // 绑定服务
        if (payService == null) {
            // 绑定安全支付服务需要获取上下文环境，
            // 如果绑定不成功使用mActivity.getApplicationContext().bindService
            // 解绑时同理
            mActivity.getApplicationContext().bindService(
                    new Intent(activity, PayService.class),
                    mSecurePayConnection, Context.BIND_AUTO_CREATE);
        }
        // else ok.

        final String payinfo = strOrderInfo;

        // 实例一个线程来进行支付
        new Thread(new Runnable() {
            public void run() {
                try {
                    // wait for the service bind operation to completely
                    // finished.
                    // Note: this is important,otherwise the next
                    // payService.Pay()
                    // will fail.
                    // 等待安全支付服务绑定操作结束
                    // 注意：这里很重要，否则payService.pay()方法会失败
                    synchronized (lock) {
                        if (payService == null)
                            lock.wait();
                    }

                    // register a Callback for the service.
                    // 为安全支付服务注册一个回调
                    payService.registerCallback(mCallback);

                    // call the MobileSecurePay service.
                    // 调用安全支付服务的pay方法
                    String strRet = payService.pay(payinfo);
                    BaseHelper.log(TAG, "服务端支付结果：" + strRet);

                    // set the flag to indicate that we have finished.
                    // unregister the Callback, and unbind the service.
                    // 将mbPaying置为false，表示支付结束
                    // 移除回调的注册，解绑安全支付服务
                    mbPaying = false;
                    payService.unregisterCallback(mCallback);
                    mActivity.getApplicationContext().unbindService(
                            mSecurePayConnection);

                    // send the result back to caller.
                    // 发送交易结果
                   callback.onCall(strRet);
                } catch (Exception e) {
                    callback.onCall(e.toString());
                }
            }
        }).start();

        return true;
    }

    /**
     * This implementation is used to receive callbacks from the remote service.
     * 实现安全支付的回调
     */
    private IRemoteServiceCallback mCallback = new IRemoteServiceCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about new
         * values. Note that IPC calls are dispatched through a thread pool
         * running in each process, so the code executing here will NOT be
         * running in our main thread like most other things -- so, to update
         * the UI, we need to use a Handler to hop over there. 通过IPC机制启动安全支付服务
         */
        public void startActivity(String packageName, String className,
                int iCallingPid, Bundle bundle) throws RemoteException {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);

            if (bundle == null)
                bundle = new Bundle();
            // else ok.

            try {
                bundle.putInt("CallingPid", iCallingPid);
                intent.putExtras(bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }

            intent.setClassName(packageName, className);
            mActivity.startActivity(intent);
        }

        /**
         * when the msp loading dialog gone, call back this method.
         */
        @Override
        public boolean isHideLoadingScreen() throws RemoteException {
            return false;
        }

        /**
         * when the current trade is finished or cancelled, call back this
         * method.
         */
        @Override
        public void payEnd(boolean arg0, String arg1) throws RemoteException {

        }

    };

}
