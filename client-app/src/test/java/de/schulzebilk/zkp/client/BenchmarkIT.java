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

@SpringBootTest
public class BenchmarkIT {

    @Autowired
    private BookService bookService;

    @Autowired
    private PersonService personService;

    private static final int MAX_ITERATIONS = 1000;

    @Test
    public void fiatShamirBenchmark_GetBook() {
        User user = UserHelper.getFiatShamirUser();

        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            StopWatch sw = new StopWatch();
            sw.start("Fiat-Shamir Authentication");
            Book book = bookService.getBookById(1L, user);
            sw.stop();
            durations.add(sw.getTotalTimeMillis());
        }

        StatisticsHelper.printStatistics(durations);
    }

    @Test
    public void passwordBenchmark_GetBook() {
        User user = UserHelper.getPasswordUser();

        var counter = 0;
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            counter++;
            StopWatch sw = new StopWatch();
            sw.start("Password Authentication");
            try {
                Book book = bookService.getBookById(1L, user);
            } catch (Exception e) {
                System.out.println("Error during benchmark at iteration " + counter);
            }
            sw.stop();
            durations.add(sw.getTotalTimeMillis());
        }

        StatisticsHelper.printStatistics(durations);
    }
}
