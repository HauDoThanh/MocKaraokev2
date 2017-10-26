package home.mockaraokev2.network.models;

import java.util.List;

/**
 Created by admin on 6/9/2017.
 */

public class VideoResult {
    private String nextPageToken;
    private List<VideoItem> items;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<VideoItem> getItems() {
        return items;
    }

    public void setItems(List<VideoItem> items) {
        this.items = items;
    }
}
