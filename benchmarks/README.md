# Benchmark JMH con Maven

Il progetto Maven contiene il POM padre nella radice e due moduli:

```text
pom.xml
backend/                  codice applicativo e test esistenti
benchmarks/
  pom.xml                 JMH 1.37, annotation processor e Shade 3.5.3
  src/main/java/          classi con metodi @Benchmark
  target/benchmarks.jar   JAR eseguibile generato da Maven
```

Il frontend continua a essere gestito con npm. JMH è una dipendenza del solo
modulo `benchmarks`.

## Compilazione

Usare **JDK 11** sia per Maven sia per l'esecuzione, come richiesto dal backend.
Su macOS:

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -version
```

Eseguire dalla **radice del repository**:

```sh
mvn clean package
```

Questo comando esegue anche i test del backend. Per ricostruire i benchmark
saltando l'esecuzione dei test:

```sh
mvn -pl benchmarks -am clean package -DskipTests
```

`-am` compila anche il backend necessario ai benchmark. Non serve installare
preventivamente il backend nel repository Maven locale.

Il backend produce sia `backend/target/backend-0.0.1-SNAPSHOT.jar`, eseguibile
Spring Boot, sia `backend/target/backend-0.0.1-SNAPSHOT-plain.jar`, usato come
dipendenza da JMH. Il JAR `plain` espone le classi nel normale classpath Java;
nel JAR Spring Boot le classi applicative sono invece sotto `BOOT-INF/classes`.

## Esecuzione

Elencare i benchmark disponibili:

```sh
java -jar benchmarks/target/benchmarks.jar -l
```

Eseguire tutti i benchmark, oppure selezionarne uno tramite il nome:

```sh
java -jar benchmarks/target/benchmarks.jar
java -jar benchmarks/target/benchmarks.jar 'CategoryMapperBenchmark.mapCategories'
```

Verifica rapida dell'installazione (i tempi ottenuti non sono misure attendibili):

```sh
java -jar benchmarks/target/benchmarks.jar 'CategoryMapperBenchmark.mapCategories' \
  -p size=10 -wi 1 -i 1 -w 100ms -r 100ms -f 1 -foe true
```

Per esportare una misurazione con la configurazione del benchmark:

```sh
java -jar benchmarks/target/benchmarks.jar 'CategoryMapperBenchmark.mapCategories' \
  -rf json -rff benchmarks/target/results.json
```

## Aggiungere benchmark

Creare le nuove classi in `src/main/java/at/ac/tuwien/sepm/groupphase/benchmarks/`.
Usare un package Java esplicito e annotare i metodi pubblici con `@Benchmark`.
Il compilatore configura esplicitamente `jmh-generator-annprocess` per generare
l'harness e `META-INF/BenchmarkList`.

`CategoryMapperBenchmark` misura il mapper MapStruct del backend su liste di
10, 100 e 1000 categorie, senza avviare Spring o un database. I dati vengono
preparati in `@Setup`; ogni operazione misurata converte una lista intera e
restituisce il risultato a JMH per impedirne l'eliminazione da parte del JIT.
I valori sono quindi in **ns per lista**, non per singola categoria.
La configurazione predefinita usa 3 iterazioni di warmup, 5 di misura da un
secondo e 2 JVM separate per ciascuna dimensione della lista.

Per nuove misure, preparare gli input fuori dal metodo misurato e restituire
il risultato oppure consumarlo con `Blackhole`. Eventuali risorse specifiche
dei benchmark vanno in `src/main/resources/` di questo modulo.

Riferimenti: [JMH](https://github.com/openjdk/jmh) e
[JAR aggiuntivi con classifier](https://maven.apache.org/plugins/maven-jar-plugin/examples/attached-jar.html).
