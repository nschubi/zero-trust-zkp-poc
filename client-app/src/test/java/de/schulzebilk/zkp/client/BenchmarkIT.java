package de.schulzebilk.zkp.client;

import de.schulzebilk.zkp.client.helper.StatisticsHelper;
import de.schulzebilk.zkp.client.helper.UserHelper;
import de.schulzebilk.zkp.client.service.BookService;
import de.schulzebilk.zkp.client.service.PersonService;
import de.schulzebilk.zkp.core.model.Book;
import de.schulzebilk.zkp.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SpringBootTest
public class BenchmarkIT {

    @Autowired
    private BookService bookService;

    @Autowired
    private PersonService personService;

    private static final int MAX_ITERATIONS = 1000;
    private static final int WARMUP_ITERATIONS = 200;

    @Test
    public void fiatShamirBenchmark_GetBook() {
        User user = UserHelper.getFiatShamirUser();
        getBook_Benchmark(user);
    }

    @Test
    public void fiatShamirBenchmark_CreateBook() {
        User user = UserHelper.getFiatShamirUser();
        createBook_Benchmark(user);
    }

    @Test
    public void fiatShamirSignatureBenchmark_GetBook() {
        User user = UserHelper.getSignatureUser();
        getBook_Benchmark(user);
    }

    @Test
    public void fiatShamirSignatureBenchmark_CreateBook() {
        User user = UserHelper.getSignatureUser();
        createBook_Benchmark(user);
    }

    @Test
    public void passwordBenchmark_GetBook() {
        User user = UserHelper.getPasswordUser();
        getBook_Benchmark(user);
    }

    @Test
    public void passwordBenchmark_CreateBook() {
        User user = UserHelper.getPasswordUser();
        createBook_Benchmark(user);
    }

    private void getBook_Benchmark(User user) {
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Long id = new Random().nextLong(1,4);
            try {
                Book book = bookService.getBookById(id, user);
            } catch (Exception e) {
                System.out.println("Error during warmup at iteration " + i);
            }
        }
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            StopWatch sw = new StopWatch();
            Long id = new Random().nextLong(1,4);
            sw.start(user.getAuthType().name() + " Authentication");
            try{
                Book book = bookService.getBookById(id, user);
            } catch (Exception e) {
                System.out.println("Error during benchmark at iteration " + i);
            }
            sw.stop();
            durations.add(sw.getTotalTimeMillis());
        }

        System.out.println("Benchmarking " + user.getAuthType().name() + " Authentication for GetBook");
        StatisticsHelper.printStatistics(durations);
    }

    private void createBook_Benchmark(User user) {
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            Book book = new Book("Testbook " + i, "Test Author", 2025);
            try {
                bookService.createBook(book, user);
            } catch (Exception e) {
                System.out.println("Error during warmup at iteration " + i);
            }
        }
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            StopWatch sw = new StopWatch();
            Book book = new Book("Testbook " + i, "Test Author", 2025);
            sw.start(user.getAuthType().name() + " Authentication");
            try {
                bookService.createBook(book, user);
            } catch (Exception e) {
                System.out.println("Error during benchmark at iteration " + i);
            }
            sw.stop();
            durations.add(sw.getTotalTimeMillis());
        }

        System.out.println("Benchmarking " + user.getAuthType().name() + " Authentication for CreateBook");
        StatisticsHelper.printStatistics(durations);
    }
}
