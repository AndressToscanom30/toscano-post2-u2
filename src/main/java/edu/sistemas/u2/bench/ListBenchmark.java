package edu.sistemas.u2.bench;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark comparativo de ArrayList vs LinkedList bajo tres patrones de
 * acceso.
 *
 * <p>
 * Experimentos:
 * <ul>
 * <li>Acceso aleatorio por índice: ArrayList O(1) vs LinkedList O(n)</li>
 * <li>Iteración secuencial: ambas O(n) pero con diferente localidad de
 * caché</li>
 * <li>Inserción al final: ambas O(1) amortizado pero con diferente presión de
 * GC</li>
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ListBenchmark {

    /**
     * Tamaño de las listas evaluadas.
     * Se usan tres órdenes de magnitud para observar la escalabilidad.
     */
    @Param({ "1000", "10000", "100000" })
    public int n;

    /** Lista de acceso aleatorio con respaldo en array contiguo. */
    private List<Integer> arrayList;

    /** Lista doblemente enlazada con nodos dispersos en heap. */
    private List<Integer> linkedList;

    /**
     * Inicializa ambas listas con n elementos antes de cada trial.
     *
     * @throws OutOfMemoryError si n es demasiado grande para la JVM configurada
     */
    @Setup(Level.Trial)
    public void setup() {
        arrayList = new ArrayList<>(n);
        linkedList = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            arrayList.add(i);
            linkedList.add(i);
        }
    }

    /**
     * Acceso aleatorio al elemento central de ArrayList — O(1).
     * El array subyacente permite cálculo directo de la dirección de memoria.
     *
     * @param bh Blackhole para prevenir dead code elimination
     * @return valor del elemento en posición n/2
     */
    @Benchmark
    public int arrayListRandomAccess(Blackhole bh) {
        int val = arrayList.get(n / 2);
        bh.consume(val);
        return val;
    }

    /**
     * Acceso aleatorio al elemento central de LinkedList — O(n).
     * Requiere traversal desde la cabeza o cola hasta el índice solicitado.
     *
     * @param bh Blackhole para prevenir dead code elimination
     * @return valor del elemento en posición n/2
     */
    @Benchmark
    public int linkedListRandomAccess(Blackhole bh) {
        int val = linkedList.get(n / 2);
        bh.consume(val);
        return val;
    }

    /**
     * Iteración secuencial completa de ArrayList — O(n), cache-friendly.
     * Los elementos están contiguos en memoria, maximizando el uso de caché.
     *
     * @param bh Blackhole para prevenir dead code elimination
     * @return suma de todos los elementos
     */
    @Benchmark
    public long arrayListIteration(Blackhole bh) {
        long sum = 0;
        for (int x : arrayList)
            sum += x;
        bh.consume(sum);
        return sum;
    }

    /**
     * Iteración secuencial completa de LinkedList — O(n), cache-hostile.
     * Cada nodo está disperso en el heap, generando cache misses frecuentes.
     *
     * @param bh Blackhole para prevenir dead code elimination
     * @return suma de todos los elementos
     */
    @Benchmark
    public long linkedListIteration(Blackhole bh) {
        long sum = 0;
        for (int x : linkedList)
            sum += x;
        bh.consume(sum);
        return sum;
    }
}