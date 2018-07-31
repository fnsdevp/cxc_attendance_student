package com.fnspl.hiplaedu_student.retrofit;

import android.content.Context;

import com.fnspl.hiplaedu_student.model.LoginResponce;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Uzibaba on 1/8/2017.
 */

public class RestHandler {

   public Retrofit retrofit;
    RetrofitListener retroListener;
    String method_name;
    Context mContext;

    public RestHandler(Context con, RetrofitListener retroListener ){

        this.retroListener=retroListener;
        mContext=con;

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(CONST.base_url)
                .client(new OkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public interface RestInterface {

        @FormUrlEncoded
        @POST("loginStudent.php")
        Call<LoginResponce> getOTP(@Field("username") String username,
                                   @Field("device_id") String device_id,
                                   @Field("device_type") String device_type,
                                   @Field("device_token") String device_token
        );


        /*@FormUrlEncoded
        @POST("user/remove_push_badge")
        Call<BatchRemove> removeTotalBadgeCount(@Field("user_id") String user_id,
                                                @Field("badge_type") String badge_type);*/

    }

    public void makeHttpRequest(Call call,String method)
    {
        this.method_name =method;
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                // do something with the response
                retroListener.onSuccess(call,response, method_name);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                retroListener.onFailure(call,t);
            }
        });
    }

}
