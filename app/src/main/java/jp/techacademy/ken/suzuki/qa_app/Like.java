package jp.techacademy.ken.suzuki.qa_app;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Like extends RealmObject implements Serializable {
    private String like; // お気に入り

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;

    public String getTitle() {
        return like;
    }

    public void setTitle(String title) {
        this.like = like;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}