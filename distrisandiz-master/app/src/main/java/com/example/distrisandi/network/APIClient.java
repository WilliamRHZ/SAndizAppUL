package com.example.distrisandi.network;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class APIClient {
    private static Retrofit retrofit = null;
    public static final String BASE_URL = "https://sandiz.com.mx/grupo_sandiz/WebService/";
    public static final String BASE_URL_LOCAL = "http://10.0.2.2/sandiz/WebService/";

    public static APIInterface getClient(){
        if(retrofit == null){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            Response response = null;
                            boolean responseOK = false;
                            int tryCount = 0;

                            while (!responseOK && tryCount < 3) {
                                try {
                                    response = chain.proceed(request);
                                    responseOK = response.isSuccessful();
                                }catch (Exception e){
                                    Log.d("intercept", "Request is not successful - " + tryCount);
                                }finally{
                                    tryCount++;
                                }
                            }

                            // otherwise just pass the original response on
                            return response != null ? response : chain.proceed(request);
                        }
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit.create(APIInterface.class);
    }
}
