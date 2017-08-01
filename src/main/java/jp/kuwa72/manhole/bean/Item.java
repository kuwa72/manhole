package jp.kuwa72.manhole.bean;

public class Item {
    public String id;
    public String name;

    public Item(int aid,String aname) {
        this.id = Integer.toString(aid);
        this.name = aname;
    }

    public Item() {
        //do nothing
    }
}
