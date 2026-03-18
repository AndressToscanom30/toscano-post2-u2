package edu.sistemas.u2.algorithms;

public final class MergeSort {
    private MergeSort() {
    }

    public static int[] sort(int[] arr) {

        int[] a = arr.clone();
        mergeSort(a, 0, a.length - 1);

        return a;
    }

    private static void mergeSort(int[] a, int lo, int hi) {

        if (lo >= hi)
            return;

        int mid = lo + (hi - lo) / 2;

        mergeSort(a, lo, mid);
        mergeSort(a, mid + 1, hi);

        merge(a, lo, mid, hi);
    }

    private static void merge(int[] a, int lo, int mid, int hi) {

        int[] tmp = new int[hi - lo + 1];

        int i = lo;
        int j = mid + 1;
        int k = 0;

        while (i <= mid && j <= hi) {

            if (a[i] <= a[j]) {
                tmp[k++] = a[i++];
            } else {
                tmp[k++] = a[j++];
            }
        }

        while (i <= mid)
            tmp[k++] = a[i++];
        while (j <= hi)
            tmp[k++] = a[j++];

        System.arraycopy(tmp, 0, a, lo, tmp.length);
    }
}
