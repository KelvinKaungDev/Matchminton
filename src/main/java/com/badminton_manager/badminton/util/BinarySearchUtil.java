package com.badminton_manager.badminton.util;

import java.util.List;
import java.util.function.ToIntFunction;

public final class BinarySearchUtil {

    private BinarySearchUtil() {}

    /**
     * Index of the first element for which comparator.applyAsInt(element) >= 0.
     * The list must already be sorted ascending w.r.t. comparator.
     */
    public static <T> int lowerBound(List<T> sortedList, ToIntFunction<T> comparator) {
        int lo = 0, hi = sortedList.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (comparator.applyAsInt(sortedList.get(mid)) < 0) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /**
     * Index of the first element for which comparator.applyAsInt(element) > 0.
     * The list must already be sorted ascending w.r.t. comparator.
     */
    public static <T> int upperBound(List<T> sortedList, ToIntFunction<T> comparator) {
        int lo = 0, hi = sortedList.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (comparator.applyAsInt(sortedList.get(mid)) <= 0) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }
}
