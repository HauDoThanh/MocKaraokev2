package home.mockaraokev2.network.models;

/**
  Created by admin on 6/9/2017.
 */

public class VideoItem {
    private Id id;
    private Snippet snippet;

    public Id getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }

    public class Id {
        String videoId;
        String playlistId;

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public String getPlaylistId() {
            return playlistId;
        }

        public void setPlaylistId(String playlistId) {
            this.playlistId = playlistId;
        }
    }

    public class Snippet {
        String title;
        Thumbnails thumbnails;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Thumbnails getThumbnails() {
            return thumbnails;
        }

        public void setThumbnails(Thumbnails thumbnails) {
            this.thumbnails = thumbnails;
        }

    }

    public class Thumbnails {
        // HashMap<String,Object> default;
        medium medium;

        public VideoItem.medium getMedium() {
            return medium;
        }

        public void setMedium(VideoItem.medium medium) {
            this.medium = medium;
        }
    }
    public class medium{
        String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
