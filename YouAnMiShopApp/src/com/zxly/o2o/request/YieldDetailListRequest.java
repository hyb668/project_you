package com.zxly.o2o.request;



import android.util.Log;

import com.easemob.easeui.AppException;
import com.zxly.o2o.account.Account;
import com.zxly.o2o.model.YieldDetail;
import com.zxly.o2o.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dsnx on 2015/12/21.
 */
public class YieldDetailListRequest extends BaseRequest {

    private   Map<Object, List<Object>> yieldDetailList = new LinkedHashMap<Object, List<Object>>();
    public YieldDetailListRequest(int pageIndex, int type){
        addParams("pageIndex",pageIndex);
        addParams("pageSize",10);
        addParams("shopId", Account.user.getShopId());
        addParams("type",type);
    }
    public  Map<Object, List<Object>> getYieldDetailList()
    {
        return  yieldDetailList;
    }

    @Override
    protected void fire(String data) throws AppException {
        try {
            JSONArray jsonArray=new JSONArray(data);
            int length=jsonArray.length();
            for(int i=0;i<length;i++)
            {
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                YieldDetail yieldDetail=new YieldDetail();
                if(jsonObject.has("commissionStatus"))
                {
                    yieldDetail.setCommissionStatus(jsonObject.getInt("commissionStatus"));
                }
                float money=Float.parseFloat(jsonObject.getString("money")) ;
                yieldDetail.setId(jsonObject.optInt("id"));
                yieldDetail.setMoney(money);
                yieldDetail.setPayType(jsonObject.getInt("payType"));
                long time=jsonObject.optLong("time");
                yieldDetail.setTime(time);
                yieldDetail.setType(jsonObject.getInt("type"));
                yieldDetail.setSerialNumber(jsonObject.getString("number"));
                yieldDetail.setStatus(jsonObject.optInt("status"));
                String keyTime= TimeUtil.formatOrderTimeHHMM(time);
                if(!yieldDetailList.containsKey(keyTime))
                {
                    List<Object> list=new ArrayList<Object>();
                    list.add(yieldDetail);
                    yieldDetailList.put(keyTime,list);
                }else
                {
                    yieldDetailList.get(keyTime).add(yieldDetail);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String method() {
        return "/pay/billDetailList";
    }
}
