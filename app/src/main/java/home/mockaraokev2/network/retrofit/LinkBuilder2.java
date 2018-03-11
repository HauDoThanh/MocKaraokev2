package home.mockaraokev2.network.retrofit;

import home.mockaraokev2.network.models.Mp3Object;
import home.mockaraokev2.network.models.PostObject;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 12/4/2017.
 */

interface LinkBuilder2 {

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
