package com.library.util;

import java.util.Comparator;
import java.util.List;

/**
 * 排序工具类
 * 实现多种排序算法，用于图书列表的多字段排序
 * 包括：冒泡排序、快速排序、归并排序
 */
public class SortUtil {

    /**
     * 使用快速排序对列表进行排序（原地排序）
     * 时间复杂度 O(n log n)，空间复杂度 O(log n)
     */
    public static <T> void quickSort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) return;
        quickSort(list, comparator, 0, list.size() - 1);
    }

    private static <T> void quickSort(List<T> list, Comparator<T> cmp, int low, int high) {
        if (low < high) {
            int pi = partition(list, cmp, low, high);
            quickSort(list, cmp, low, pi - 1);
            quickSort(list, cmp, pi + 1, high);
        }
    }

    private static <T> int partition(List<T> list, Comparator<T> cmp, int low, int high) {
        T pivot = list.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (cmp.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high);
        return i + 1;
    }

    /**
     * 使用冒泡排序对列表进行排序
     * 时间复杂度 O(n²)，适合小数据量或教学演示
     */
    public static <T> void bubbleSort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) return;
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (comparator.compare(list.get(j), list.get(j + 1)) > 0) {
                    swap(list, j, j + 1);
                    swapped = true;
                }
            }
            if (!swapped) break; // 已有序，提前退出
        }
    }

    /**
     * 使用归并排序对列表进行排序
     * 时间复杂度 O(n log n)，稳定排序
     */
    public static <T> void mergeSort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() <= 1) return;
        mergeSort(list, comparator, 0, list.size() - 1);
    }

    @SuppressWarnings("unchecked")
    private static <T> void mergeSort(List<T> list, Comparator<T> cmp, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(list, cmp, left, mid);
            mergeSort(list, cmp, mid + 1, right);
            merge(list, cmp, left, mid, right);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void merge(List<T> list, Comparator<T> cmp, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        Object[] L = new Object[n1];
        Object[] R = new Object[n2];
        for (int i = 0; i < n1; i++) L[i] = list.get(left + i);
        for (int j = 0; j < n2; j++) R[j] = list.get(mid + 1 + j);

        int i = 0, j = 0, k = left;
        while (i < n1 && j < n2) {
            if (cmp.compare((T) L[i], (T) R[j]) <= 0) {
                list.set(k++, (T) L[i++]);
            } else {
                list.set(k++, (T) R[j++]);
            }
        }
        while (i < n1) list.set(k++, (T) L[i++]);
        while (j < n2) list.set(k++, (T) R[j++]);
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    // ========== 预定义Comparator ==========

    /** 按书名字典序 */
    public static <T> Comparator<T> byString(java.util.function.Function<T, String> getter) {
        return Comparator.comparing(getter, String.CASE_INSENSITIVE_ORDER);
    }

    /** 按整数升序 */
    public static <T> Comparator<T> byInt(java.util.function.Function<T, Integer> getter) {
        return Comparator.comparingInt(getter::apply);
    }

    /** 按整数降序 */
    public static <T> Comparator<T> byIntDesc(java.util.function.Function<T, Integer> getter) {
        return (a, b) -> Integer.compare(getter.apply(b), getter.apply(a));
    }
}
