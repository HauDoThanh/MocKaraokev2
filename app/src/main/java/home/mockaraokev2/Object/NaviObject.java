package home.mockaraokev2.Object;

/**
 Created by Admin on 5/23/2017.
 */

public class NaviObject {
    private int src;
    private String title;
    private String count;

    public NaviObject(int src, String title, String count) {
        this.src = src;
        this.title = title;
        this.count = count;
    }

    public int getSrc() {
        return src;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
