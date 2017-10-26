package home.mockaraokev2.Object;

/**
 Created by admin on 5/25/2017.
 */

public class PlayListObject {

    private String name;
    private String id;
    private String image;

    public PlayListObject(String name, String id, String image) {
        this.name = name;
        this.id = id;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgage() {
        return image;
    }

    public void setImgage(String imgage) {
        this.image = imgage;
    }

}
