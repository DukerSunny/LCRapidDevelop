package com.xiaochao.lcrapiddevelop.Data;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xiaochao.lcrapiddevelop.MyApplication.MyApplication;
import com.xiaochao.lcrapiddevelop.R;
import com.xiaochao.lcrapiddevelop.Volley.VolleyInterface;
import com.xiaochao.lcrapiddevelop.Volley.VolleyReQuest;
import com.xiaochao.lcrapiddevelop.entity.DataDto;
import com.xiaochao.lcrapiddevelop.entity.IsError;
import com.xiaochao.lcrapiddevelop.entity.MySection;
import com.xiaochao.lcrapiddevelop.entity.UniversityListDto;
import com.xiaochao.lcrapiddeveloplibrary.Cache.ACache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/19.
 */
public class JsonData {
    public static final int DATA_LOAD_NULL=1000;
    public static final int DATA_LOAD_OK=100;
    public static final int DATA_REFRESH_NULL=1001;
    public static final int DATA_REFRESH_OK=200;
    public static final int DATA_ERROR=1004;

    private static Gson gson;
    public static IsError josnToObj(JSONObject response){
        gson = new Gson();
        try {
            return gson.fromJson(response.toString(),IsError.class);
        } catch (JsonSyntaxException e) {
            return new IsError(0,0,1001,"程序发生未知错误","");
        }
    }
    public static void initdate(Context context, final int PageIndex, final int PageSize, final Boolean isJz, final DataInterface dataInterface){
        if(MyApplication.getAcache().getAsString("data_test_PageIndex_"+PageIndex+"_PageSize_"+PageSize)==null){
            Map<String,String> map=new HashMap<String,String>();
            map.put("ProvinceIds","");
            map.put("Classify","");
            map.put("CollegeLevel","");
            map.put("IsBen","");
            map.put("PageIndex",PageIndex+"");
            map.put("PageSize",PageSize+"");
            JSONObject json=new JSONObject(map);
            VolleyReQuest.ReQuestPost_null(context, Constant.DATA_URL, "school_list_post", json, new VolleyInterface(VolleyInterface.mLisener, VolleyInterface.mErrorLisener) {
                @Override
                public void onMySuccess(JSONObject response) {
                    MyApplication.getAcache().put("data_test_PageIndex_"+PageIndex+"_PageSize_"+PageSize,response, 7*ACache.TIME_DAY);//缓存数据7天
                    dataInterface.onMySuccess(response);
                }

                @Override
                public void onMyError(VolleyError error) {
                    dataInterface.onMyError();
                }
            });
        }else{
            if(isJz){
                //这里必须加上一个线程延时  因为RecyclerView在加载新数据如果View还没有计算完 会报错
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dataInterface.onMySuccess(MyApplication.getAcache().getAsJSONObject("data_test_PageIndex_"+PageIndex+"_PageSize_"+PageSize));
                    }
                }, 200);
            }else{
                dataInterface.onMySuccess(MyApplication.getAcache().getAsJSONObject("data_test_PageIndex_"+PageIndex+"_PageSize_"+PageSize));
            }
        }
    }
    public static DataDto<UniversityListDto> httpDate(JSONObject response, Boolean isJz) {
        IsError error = JsonData.josnToObj(response);
        if (error.getCode() == 1) {
            try {
                JSONArray array1 = response.getJSONArray("Results");
                Gson gson = new Gson();
                if (isJz) {
                    List<UniversityListDto> expertLists = gson.fromJson(array1.toString(),
                            new TypeToken<List<UniversityListDto>>() {
                            }.getType());
                    if (expertLists.size() == 0) {
                        return new DataDto<UniversityListDto>(DATA_LOAD_NULL,null);
                    } else {
                        //新增自动加载的的数据
                        return new DataDto<UniversityListDto>(DATA_LOAD_OK,expertLists);
                    }
                } else {
                    List<UniversityListDto> expertLists  = gson.fromJson(array1.toString(),
                            new TypeToken<List<UniversityListDto>>() {
                            }.getType());
                    if(expertLists.size()==0) {
                        //没有找到数据显示
                        return new DataDto<UniversityListDto>(DATA_REFRESH_NULL,null);
                    }else{
                        //进入显示的初始数据或者下拉刷新显示的数据
                        return new DataDto<UniversityListDto>(DATA_REFRESH_OK,expertLists);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new DataDto<UniversityListDto>(DATA_ERROR,null);
            }
        }
        return new DataDto<UniversityListDto>(DATA_ERROR,null);

    }
    public static List<MySection> getSampleData(List<UniversityListDto> expertLists,int PageIndex) {
        List<MySection> list = new ArrayList<>();
        if(PageIndex%2==0){
            list.add(new MySection(true, "分组"+PageIndex, false));
        }else{
            list.add(new MySection(true, "分组"+PageIndex, true));
        }
        for (UniversityListDto UniversityListDto:expertLists) {
            list.add(new MySection(UniversityListDto));
        }
        return list;
    }
}
