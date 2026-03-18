package edu.sistemas.u2.bench;

import edu.sistemas.u2.algorithms.InsertionSort;
import edu.sistemas.u2.algorithms.MergeSort;
import edu.sistemas.u2.algorithms.HeapSort;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)

@Fork(2)

@State(Scope.Benchmark)
public class SortingBenchmark {

    @Param({ "500", "1000", "2000", "4000", "8000", "16000" })
    public int n;

    private int[] data;

    @Setup(Level.Invocation)
    public void setup() {

        data = new Random(42)
                .ints(n, 0, Integer.MAX_VALUE)
                .toArray();
    }

    @Benchmark
    public void insertionSort(Blackhole bh) {
        bh.consume(InsertionSort.sort(data));
    }

    @Benchmark
    public void mergeSort(Blackhole bh) {
        bh.consume(MergeSort.sort(data));
    }

    @Benchmark
    public void heapSort(Blackhole bh) {
        bh.consume(HeapSort.sort(data));
    }
}