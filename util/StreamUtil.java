package com.lynkteam.tapmanager.util;

/**
 * Created by robertov on 05/08/15.
 */
public class StreamUtil {
    public static String streamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
