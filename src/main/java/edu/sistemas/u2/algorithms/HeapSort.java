package edu.sistemas.u2.algorithms;

public class HeapSort {
    private HeapSort() {
    }

    public static int[] sort(int[] arr) {

        int[] a = arr.clone();
        int n = a.length;

        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(a, n, i);
        }

        for (int i = n - 1; i > 0; i--) {

            int temp = a[0];
            a[0] = a[i];
            a[i] = temp;

            siftDown(a, i, 0);
        }

        return a;
    }

    private static void siftDown(int[] a, int n, int root) {

        while (true) {

            int largest = root;

            int left = 2 * root + 1;
            int right = 2 * root + 2;

            if (left < n && a[left] > a[largest]) {
                largest = left;
            }

            if (right < n && a[right] > a[largest]) {
                largest = right;
            }

            if (largest == root)
                break;

            int temp = a[root];
            a[root] = a[largest];
            a[largest] = temp;

            root = largest;
        }
    }
}
