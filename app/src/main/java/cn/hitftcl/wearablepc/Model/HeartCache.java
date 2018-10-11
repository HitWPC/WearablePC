package cn.hitftcl.wearablepc.Model;

import java.util.LinkedList;

/**
 * Created by Administrator on 2018/10/11.
 */

public class HeartCache {

    private final static int SIZE = 7;
    private static LinkedList<HeartTable> heartDataCache = new LinkedList<>();
    static long version = 0;


    public static LinkedList<HeartTable> getHeartDataCache() {
        return heartDataCache;
    }

    public static long getVersion() {
        return version;
    }

    public static void add(HeartTable heartTable){
        if(heartDataCache.size()>7)
            heartDataCache.poll();
        heartDataCache.add(heartTable);
        version++;
    }
}
