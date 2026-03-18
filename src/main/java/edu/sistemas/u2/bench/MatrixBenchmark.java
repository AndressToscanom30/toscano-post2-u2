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

import java.util.concurrent.TimeUnit;

/**
 * Benchmark de localidad de caché en acceso a matrices 2D.
 *
 * <p>
 * Compara dos patrones de acceso sobre una matriz cuadrada de enteros:
 * <ul>
 * <li>Row-major: acceso por filas, stride de 4 bytes (cache-friendly)</li>
 * <li>Column-major: acceso por columnas, stride de n*4 bytes
 * (cache-hostile)</li>
 * </ul>
 * Ambas versiones tienen complejidad Θ(n²) pero el comportamiento
 * de caché es radicalmente diferente para n grande.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(2)
@State(Scope.Benchmark)
public class MatrixBenchmark {

    /**
     * Dimensión de la matriz cuadrada n×n.
     * Se evalúan cuatro tamaños para observar la degradación de caché.
     */
    @Param({ "256", "512", "1024", "2048" })
    public int n;

    /** Matriz cuadrada de enteros inicializada antes de cada trial. */
    private int[][] matrix;

    /**
     * Inicializa la matriz con valores secuenciales antes de cada trial.
     * Se usa Level.Trial para no incluir el costo de inicialización
     * en las mediciones.
     */
    @Setup(Level.Trial)
    public void setup() {
        matrix = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                matrix[i][j] = i * n + j;
    }

    /**
     * Suma todos los elementos accediendo por filas (row-major).
     * Stride = 1 entero = 4 bytes — localidad espacial óptima,
     * maximiza el uso de cada línea de caché cargada.
     *
     * @param bh Blackhole para evitar eliminación por dead code elimination
     * @return suma total de los elementos de la matriz
     */
    @Benchmark
    public long rowMajorSum(Blackhole bh) {
        long sum = 0;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                sum += matrix[i][j];
        bh.consume(sum);
        return sum;
    }

    /**
     * Suma todos los elementos accediendo por columnas (column-major).
     * Stride = n enteros = n*4 bytes — localidad espacial deficiente,
     * genera un cache miss por cada acceso para n grande.
     *
     * @param bh Blackhole para evitar eliminación por dead code elimination
     * @return suma total de los elementos de la matriz
     */
    @Benchmark
    public long columnMajorSum(Blackhole bh) {
        long sum = 0;
        for (int j = 0; j < n; j++)
            for (int i = 0; i < n; i++)
                sum += matrix[i][j];
        bh.consume(sum);
        return sum;
    }
}