package umairayub.tapcounter;

import android.app.Application;

public class Base extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ObjectBox.init(this);
    }
}
