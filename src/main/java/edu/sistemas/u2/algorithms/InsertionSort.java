package edu.sistemas.u2.algorithms;

public final class InsertionSort {
    private InsertionSort() {
    }

    public static int[] sort(int[] arr) {

        int[] a = arr.clone();

        for (int i = 1; i < a.length; i++) {

            int key = a[i];
            int j = i - 1;

            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }

            a[j + 1] = key;
        }

        return a;
    }
}
