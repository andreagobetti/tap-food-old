package com.lynkteam.tapmanager.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by robertov on 02/09/15.
 */
public class FileSystemUtil {
    private static String WAREHOUSE_ELEMENT_BITMAP_PATH_PREFIX = "wheb";

    public static void saveWarehouseElementBitmap (Context context, Bitmap b, String picName) throws Exception{
        String path = WAREHOUSE_ELEMENT_BITMAP_PATH_PREFIX + "_" + picName + ".png";
        FileOutputStream fos = context.openFileOutput(path, Context.MODE_PRIVATE);
        b.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
    }

    static public Bitmap loadWarehouseElementBitmap(Context context, String picName) throws Exception{
        Bitmap b = null;
        String path = WAREHOUSE_ELEMENT_BITMAP_PATH_PREFIX + "_" + picName + ".png";
        FileInputStream fis = context.openFileInput(path);
        b = BitmapFactory.decodeStream(fis);
        fis.close();
        return b;
    }
}
