# Framework de Selección Algorítmica por Restricciones
## Problema: Servicio de Gestión de Eventos

**Contexto:** Un servicio de gestión de eventos procesa listas de `n` eventos con los
siguientes requerimientos operacionales:
- (a) Inserción al final frecuente
- (b) Acceso por índice ocasional (top-10 por prioridad)
- (c) Iteración completa una vez por segundo

**Restricción de infraestructura:** contenedor con límite de 512 MB de RAM  
**Tamaño típico de entrada:** n = 50.000 | n máximo estimado: 200.000

---

## Paso 1 — Caracterización de la Entrada

| Parámetro            | Valor                          |
|----------------------|-------------------------------|
| n típico             | 50.000 eventos                |
| n máximo             | 200.000 eventos               |
| Tipo de elemento     | `Integer` (boxed, 16 bytes en heap) |
| Distribución         | Inserción al final, acceso al índice central ocasional |
| Patrón de lectura    | Iteración secuencial completa 1 vez/segundo |

**Observación:** el tipo `Integer` (boxed) ya introduce una indirección de puntero por
elemento en comparación con `int[]`. Esto es relevante al analizar la presión de GC.

---

## Paso 2 — Restricciones de Entorno

| Restricción          | Detalle                                                         |
|----------------------|-----------------------------------------------------------------|
| Memoria RAM          | 512 MB límite de contenedor → se deben evitar estructuras con alto overhead por nodo |
| Latencia             | No crítica a nivel de microsegundos; el ciclo de iteración es de 1 segundo |
| Garbage Collection   | Entorno de contenedor → alta presión de GC puede causar pausas visibles y saturar el heap |
| Plataforma           | JVM 64-bit con punteros comprimidos (CompressedOops activo por defecto) |

**Conclusión del paso:** la restricción de memoria y GC descarta estructuras que
crean objetos adicionales por elemento, como los nodos internos de `LinkedList`.

---

## Paso 3 — Candidatos Asintóticos Viables

| Estructura     | add al final     | get(i)   | iterate  | Overhead por elemento     |
|----------------|-----------------|----------|----------|--------------------------|
| `ArrayList`    | O(1) amortizado | O(1)     | O(n)     | ~0 bytes extra            |
| `LinkedList`   | O(1)            | O(n)     | O(n)     | ~48 bytes por nodo (JVM 64-bit) |
| `ArrayDeque`   | O(1) amortizado | ❌ sin get(i) | O(n) | ~0 bytes extra          |

**`ArrayDeque` queda descartado** en este paso porque no expone acceso por índice
(`get(i)`), lo que incumple el requisito (b).

**`LinkedList` y `ArrayList` son los candidatos que pasan el filtro asintótico.**

---

## Paso 4 — Análisis de Constantes y Comportamiento de Caché

Esta sección referencia directamente los datos obtenidos en los benchmarks del
laboratorio (ver `results/list-results.json`).

### 4.1 Acceso aleatorio por índice

Los resultados del benchmark `ListBenchmark` confirman que:

- `ArrayList.get(n/2)` mantiene tiempo **constante** independientemente de n,
  consistente con O(1) real.
- `LinkedList.get(n/2)` escala **linealmente** con n, ya que debe recorrer la
  cadena de nodos desde el inicio hasta la posición media.

Para n = 100.000, la diferencia observada es de varios órdenes de magnitud.
Esto descalifica a `LinkedList` para el requisito (b).

### 4.2 Iteración secuencial completa

Aunque ambas estructuras son O(n) en iteración teórica, el comportamiento de caché
es radicalmente diferente:

- **`ArrayList`** almacena elementos en un array contiguo en memoria. El hardware
  prefetcher carga líneas de caché de forma anticipada → iteración **cache-friendly**.
- **`LinkedList`** almacena cada nodo como un objeto separado disperso en el heap.
  Cada acceso al siguiente nodo puede provocar un cache miss → iteración
  **cache-hostile**.

Los benchmarks muestran que `LinkedList` es entre **3x y 8x más lento** en
iteración para n = 50.000, aun cuando ambos tienen la misma clase O().

### 4.3 Presión de Garbage Collection (GC profiler)

La métrica `gc.alloc.rate.norm` obtenida con `-prof gc` muestra:

- **`LinkedList`** genera aproximadamente **~48 bytes adicionales por elemento**
  debido a los objetos `Node` internos (objeto wrapper con referencias `prev`,
  `next` e `item` en JVM 64-bit con punteros comprimidos).
- Para n = 50.000 esto representa **~2.4 MB adicionales** solo en nodos, más la
  presión de GC generada por la creación y destrucción de iteradores.
- **`ArrayList`** genera una asignación cercana a cero bytes extra por operación de
  iteración, ya que el array subyacente ya existe en memoria.

| Métrica                   | ArrayList  | LinkedList        |
|---------------------------|------------|-------------------|
| gc.alloc.rate.norm (iter) | ~0 B/op    | ~48 B/elem + iter overhead |
| Estructura interna        | array[]    | Node { prev, next, item } |
| Comportamiento caché      | Contiguo   | Disperso en heap  |
| Iteración n=50.000        | baseline   | 3x–8x más lento   |

---

## Paso 5 — Decisión Justificada

### ✅ Estructura seleccionada: `ArrayList<Integer>`

### Justificación

**Requisito (a) — Inserción al final frecuente:**  
Ambas estructuras ofrecen O(1) amortizado. Sin embargo, `ArrayList` es más rápido
en la práctica porque la inserción no requiere alojar un nuevo objeto `Node` en el
heap, reduciendo la presión de GC. El costo de redimensionamiento de `ArrayList`
es negligible para n = 50.000: ocurre aproximadamente 1 vez cada ~25.000
inserciones y copia el array en un único `System.arraycopy()` altamente optimizado.

**Requisito (b) — Acceso por índice ocasional:**  
`ArrayList.get(i)` es O(1) real (acceso directo al array). `LinkedList.get(i)` es
O(n) real, lo que para n = 50.000 puede implicar hasta 25.000 saltos de puntero
en el peor caso. Los benchmarks confirman esta diferencia con datos empíricos.

**Requisito (c) — Iteración completa 1 vez/segundo:**  
Los benchmarks muestran que `ArrayList` es entre 3x y 8x más rápido en iteración
para n = 50.000 gracias a la localidad espacial de su array contiguo. Este es el
requisito de mayor frecuencia en el sistema, y el que más impacto tiene en el
rendimiento total.

**Restricción de memoria (512 MB RAM):**  
`LinkedList` consumiría ~2.4 MB adicionales en nodos para n = 50.000, y hasta
~9.6 MB para n = 200.000. Aunque estos valores son técnicamente aceptables en
512 MB, la alta presión de GC generada por los objetos `Node` puede causar pausas
de recolección más frecuentes en el contenedor, lo que contradice la estabilidad
operacional requerida.

### Trade-off reconocido

`LinkedList` tiene ventaja teórica en inserciones al inicio o en el medio de la lista
(O(1) con iterador posicionado vs O(n) de `ArrayList`). Sin embargo, el problema
de diseño especifica **inserción al final**, donde `ArrayList` es equivalente o
superior en la práctica. La penalización de `LinkedList` en caché, iteración y
presión de GC supera ampliamente su ventaja teórica en este caso de uso específico.

### Conclusión

> Una estructura con la misma clase O() puede ser significativamente peor en
> producción cuando las constantes ocultas, el comportamiento de caché y la presión
> de GC son relevantes para el entorno de despliegue. La notación asintótica es
> necesaria pero no suficiente para tomar decisiones de diseño en sistemas reales.
