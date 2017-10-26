package home.mockaraokev2.network.models;

import java.util.List;

/**
 Created by buimi on 7/5/2017.
 */

public class PlaylistResult {
    private String nextPageToken;
    private List<PlaylistItem> items;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<PlaylistItem> getItems() {
        return items;
    }

    public void setItems(List<PlaylistItem> items) {
        this.items = items;
    }
}
