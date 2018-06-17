package jp.techacademy.ken.suzuki.qa_app;

import java.io.Serializable;

public class Like implements Serializable {
    // Firebaseから取得した質問
    private String mBody;

    // Firebaseから取得したお気に入りしたユーザーの名前
    private String mName;

    // Firebaseから取得したお気に入りしたユーザーのUID
    private String mUid;

    // Firebaseから取得したお気に入りのUID
    private String mLikeUid;

    public Like(String body, String name, String uid, String likeUid) {
        mBody = body;
        mName = name;
        mUid = uid;
        mLikeUid = likeUid;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getLikeUid() {
        return mLikeUid;
    }
}