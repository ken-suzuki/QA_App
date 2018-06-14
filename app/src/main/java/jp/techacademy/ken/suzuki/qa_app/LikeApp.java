package jp.techacademy.ken.suzuki.qa_app;

import android.app.Application;
import io.realm.Realm;

public class LikeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
