package home.mockaraokev2.network.retrofit;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 Created by admin on 7/8/2017.
 */

class CachingControlInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        request = new Request.Builder()
                .cacheControl(new CacheControl.Builder()
                        .maxAge(1, TimeUnit.DAYS)
                        .minFresh(4, TimeUnit.HOURS)
                        .maxStale(8, TimeUnit.HOURS)
                        .build())
                .url(request.url())
                .build();


        return chain.proceed(request);
    }
}
