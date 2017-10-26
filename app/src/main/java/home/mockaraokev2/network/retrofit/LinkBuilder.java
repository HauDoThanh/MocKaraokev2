package home.mockaraokev2.network.retrofit;

import home.mockaraokev2.network.models.Mp3Object;
import home.mockaraokev2.network.models.PlaylistResult;
import home.mockaraokev2.network.models.PostObject;
import home.mockaraokev2.network.models.VideoResult;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 Created by admin on 6/9/2017.
 */

interface LinkBuilder {

    @GET("search?part=snippet&channelId=UC6-iiAvpLENHaflu1FGEctA&key=AIzaSyA2XcoY4o4L_zGdFoYVMFcUMYvJikbgCfw")
    Call<VideoResult> getLinkBuilder(
            @Query("order") String order,
            @Query("pageToken") String pageToken,
            @Query("type") String type,
            @Query("q") String q,
            @Query("maxResults") String maxResults
    );

    @GET("playlistItems?part=snippet&channelId=UC6-iiAvpLENHaflu1FGEctA&key=AIzaSyA2XcoY4o4L_zGdFoYVMFcUMYvJikbgCfw")
    Call<PlaylistResult> getLinkBuilder(
            @Query("playlistId") String playlistId,
            @Query("pageToken") String pageToken,
            @Query("maxResults") String maxResults
    );

    @POST("/")
    @FormUrlEncoded
    Call<PostObject> getToken(
            @Field("user") String user,
            @Field("pass") String pass
    );

    @POST("/checkToken.php")
    @FormUrlEncoded
    Call<Mp3Object> getMp3(
            @Field("token") String token,
            @Field("file_name") String file_name
    );
}
