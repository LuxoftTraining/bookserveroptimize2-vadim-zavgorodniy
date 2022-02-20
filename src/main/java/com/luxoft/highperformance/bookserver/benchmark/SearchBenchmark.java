package com.luxoft.highperformance.bookserver.benchmark;

import com.luxoft.highperformance.bookserver.BookService;
import com.luxoft.highperformance.bookserver.model.Book;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 3, time = 2)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class SearchBenchmark {
    private BookService bc = new BookService();

    @Setup(Level.Trial)
    public void loadData() {
        bc.generateBooks();
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public Set<Book> test_usingTitleHmIndexByThreeKeywords1() {
        return bc.getBooksByTitleHashMap("AuthorName89465 AuthorSurname2993 Book75178");
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public List<Book> test_usingTitleHmIndexByThreeKeywords2() {
        return bc.getBooksByTitleHashMap2("AuthorName89465 AuthorSurname2993 Book75178");
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public List<Book> test_usingHashHmIndexByThreeKeywords() {
        return bc.getBooksByHashHMIndex("AuthorName89465 AuthorSurname2993 Book75178");
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime, Mode.Throughput})
    public List<Book> test_usingHashFastUtilIndexByThreeKeywords() {
        return bc.getBooksByHashFastUtilIndex("AuthorName89465 AuthorSurname2993 Book75178");
    }

/*
Benchmark                                                    Mode  Cnt    Score    Error   Units
SearchBenchmark.test_usingHashFastUtilIndexByThreeKeywords  thrpt    3    0.027 ±  0.005  ops/us
SearchBenchmark.test_usingHashHmIndexByThreeKeywords        thrpt    3    7.465 ±  0.905  ops/us
SearchBenchmark.test_usingTitleHmIndexByThreeKeywords1      thrpt    3    2.046 ±  0.228  ops/us
SearchBenchmark.test_usingTitleHmIndexByThreeKeywords2      thrpt    3    4.350 ±  1.417  ops/us
SearchBenchmark.test_usingHashFastUtilIndexByThreeKeywords   avgt    3  112.174 ± 10.694   us/op
SearchBenchmark.test_usingHashHmIndexByThreeKeywords         avgt    3    0.229 ±  0.055   us/op
SearchBenchmark.test_usingTitleHmIndexByThreeKeywords1       avgt    3    0.567 ±  0.127   us/op
SearchBenchmark.test_usingTitleHmIndexByThreeKeywords2       avgt    3    0.242 ±  0.106   us/op
 */
}
