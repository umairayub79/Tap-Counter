package umairayub.tapcounter;

import android.content.Context;

import io.objectbox.BoxStore;

public class ObjectBox {

    public static BoxStore boxStore;

    public static void init (Context context){
        boxStore = MyObjectBox.builder()
                .androidContext(context.getApplicationContext())
                .build();
    }


    public static BoxStore get(){
        return boxStore;
    }
}
