package com.uotttawa.lschu105.gcccyclingapp.Utils;

import com.uotttawa.lschu105.gcccyclingapp.Event;

import java.util.ArrayList;

public class QuickSort {
    static void swap(ArrayList<Event> array, int i, int j) {
        Event temp = array.get(i);
        array.set(i, array.get(j));
        array.set(j, temp);
    }

    static int partition(ArrayList<Event> array, int low, int high) {
        Event pivot = array.get(high);
        int i = low - 1;

        for (int j = low; j <= high - 1; j++) {
            if (array.get(j).compareTo(pivot) < 0) {
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return (i + 1);
    }

    static void quickSort(ArrayList<Event> array, int low, int high) {
        if (low < high) {
            int pi = partition(array, low, high);
            quickSort(array, low, pi - 1);
            quickSort(array, pi + 1, high);
        }
    }

    public static void sortEvents(ArrayList<Event> array, int ascending) {
        if (ascending == 1) {
            quickSort(array, 0, array.size() - 1);
        } else {
            // To sort in descending order, reverse the array after sorting in ascending order
            quickSort(array, 0, array.size() - 1);
            int i = 0;
            int j = array.size() - 1;
            while (i < j) {
                swap(array, i, j);
                i++;
                j--;
            }
        }
    }

    public static void printArray(ArrayList<Event> array) {
        for (Event e: array) {
            System.out.println(e);
        }
    }
}
