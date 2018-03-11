package home.mockaraokev2.network.retrofit;

import home.mockaraokev2.network.models.Mp3Object;
import home.mockaraokev2.network.models.PlaylistResult;
import home.mockaraokev2.network.models.PostObject;
import home.mockaraokev2.network.models.VideoResult;
import retrofit2.Call;
import retrofit2.Callback;
/**
 Created by admin on 6/9/2017.
 */

public class Command {

    private static Command instance = null;
    private Command(){
    }

    public static Command getInstance(){
        if (instance == null){
            instance = new Command();
        }
        return instance;
    }

    public void execute(String order, String pageToken, String type, String q, String maxResults,
                        Callback<VideoResult> callback) {
        LinkBuilder linkBuilder = LinkBuilderGenerator.createLinkBuilder(LinkBuilder.class);
        Call<VideoResult> call = linkBuilder.getLinkBuilder(order, pageToken, type, q, maxResults);
        call.enqueue(callback);
    }

    public void execute(String playlistId, String pageToken, String maxResults,
                        Callback<PlaylistResult> callback) {
        LinkBuilder linkBuilder = LinkBuilderGenerator.createLinkBuilder(LinkBuilder.class);
        Call<PlaylistResult> call = linkBuilder.getLinkBuilder(playlistId, pageToken, maxResults);
        call.enqueue(callback);
    }

    //post
    public void execute(String user, String pass, Callback<PostObject> callback){
        LinkBuilder2 linkBuilder = LinkBuilderGenerator.createLinkBuilderMocKara(LinkBuilder2.class);
        Call<PostObject> call = linkBuilder.getToken(user, pass);
        call.enqueue(callback);
    }

    public void executeMp3(String token, String filename, Callback<Mp3Object> callback){
        LinkBuilder linkBuilder = LinkBuilderGenerator.createLinkBuilderMocKara(LinkBuilder.class);
        Call<Mp3Object> call = linkBuilder.getMp3(token, filename);
        call.enqueue(callback);
    }
}