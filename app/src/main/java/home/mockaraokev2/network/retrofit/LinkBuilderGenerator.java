package home.mockaraokev2.network.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 Created by admin on 6/9/2017.
 */

class LinkBuilderGenerator {
    private static final String URL = "https://www.googleapis.com/youtube/v3/";
    private static final String URL2 = "http://moctvkaraoke.com/";

    public static <T> T createLinkBuilder(Class<T> t) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .addInterceptor(new CachingControlInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(t);
    }

    public static <T> T createLinkBuilderMocKara(Class<T> t) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL2)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(t);
    }

}
