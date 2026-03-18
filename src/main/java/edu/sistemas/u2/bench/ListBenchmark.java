package edu.sistemas.u2.bench;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ListBenchmark {

    @Param({ "1000", "10000", "100000" })
    public int n;

    private List<Integer> arrayList;
    private List<Integer> linkedList;

    @Setup(Level.Trial)
    public void setup() {
        arrayList = new ArrayList<>(n);
        linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
    }

    @Benchmark
    public int arrayListRandomAccess(Blackhole bh) {
        int val = arrayList.get(n / 2);
        bh.consume(val);
        return val;
    }

    @Benchmark
    public int linkedListRandomAccess(Blackhole bh) {
        int val = linkedList.get(n / 2);
        bh.consume(val);
        return val;
    }

    @Benchmark
    public long arrayListIteration(Blackhole bh) {
        long sum = 0;
        for (int x : arrayList)
            sum += x;
        bh.consume(sum);
        return sum;
    }

    @Benchmark
    public long linkedListIteration(Blackhole bh) {
        long sum = 0;
        for (int x : linkedList)
            sum += x;
        bh.consume(sum);
        return sum;
    }
}