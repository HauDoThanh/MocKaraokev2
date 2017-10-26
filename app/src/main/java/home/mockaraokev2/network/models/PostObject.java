package home.mockaraokev2.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 Created by admin on 9/28/2017.
 */

public class PostObject {
    @SerializedName("result")
    @Expose
    private String result;

    public String getToken() {
        return result;
    }

    public void setToken(String token) {
        this.result = token;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
