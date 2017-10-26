package home.mockaraokev2.network.models;

/**
 Created by buimi on 7/5/2017.
 */

public class PlaylistItem {
    private String id;
    private Snippet snippet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Snippet getSnippet() {
        return snippet;
    }

    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }

    public class Snippet {
        String title;
        Thumbnails thumbnails;
        ResourceId resourceId;

        public ResourceId getResourceId() {
            return resourceId;
        }

        public void setResourceId(ResourceId resourceId) {
            this.resourceId = resourceId;
        }

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
        VideoItem.medium medium;

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

    public class ResourceId {
        String videoId;

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }
    }
}
