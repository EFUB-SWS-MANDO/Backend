package com.example.sprout.global.common.util;

import org.springframework.cglib.core.internal.Function;

import java.util.List;

public class CursorPageUtils {

    private CursorPageUtils() {}

    public static <T> boolean hasNextPage(List<T> list, int limit) {
        return list.size() > limit;
    }

    public static <T> List<T> trimToPageSize(List<T> list, int limit, boolean hasNext) {
        return hasNext ? list.subList(0, limit) : list;
    }

    public static <T> Long resolveNextIdAfter(List<T> pageList, boolean hasNext, Function<T, Long> idExtractor) {
        if (!hasNext || pageList.isEmpty()) return null;
        return idExtractor.apply(pageList.get(pageList.size() - 1));
    }
}
