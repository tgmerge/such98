package me.tgmerge.such98.Util;

import android.util.LruCache;

/**
 * Created by tgmerge on 2/17.
 * Cache, other than which provided by universal-image-loader
 */
public class CacheUtil {

    private static final int DEFAULT_STR_CACHE_SIZE = 500; // size is measured in the number of entries
    public static final LruCache<Integer, String> id_avaUrlCache = new LruCache<>(DEFAULT_STR_CACHE_SIZE);

}
