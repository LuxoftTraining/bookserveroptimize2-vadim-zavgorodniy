package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.measure.Measure;
import com.luxoft.highperformance.bookserver.model.Book;
import com.luxoft.highperformance.bookserver.repositories.BookRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("books")
public class BookController {

    public final int BOOKS_AMOUNT=10_000;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookService bookService;

    @Measure(value = "baseline", warmup = 50, baseline = true)
    @GetMapping("keywords0/{keywordsString}")
    public List<Book> getBookByTitle(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");
        if (keywords.length == 1) {
            return bookRepository.findAllByTitleContaining(keywords[0]);
        } else if (keywords.length == 2) {
            return bookRepository.findAllByTitleContainingAndTitleContaining(
                keywords[0], keywords[1]);
        } else if (keywords.length == 3) {
            return bookRepository.findAllByTitleContainingAndTitleContainingAndTitleContaining(
                keywords[0], keywords[1], keywords[2]);
        }
        return null;
    }

    @Measure(value = "3 keywords indexed in DB", warmup = 50)
    @GetMapping("keywords1/{keywordsString}")
    @Transactional(readOnly = true)
    public List<Book> getBookByTitleDB(@PathVariable String keywordsString) {
        String[] keywords = keywordsString.split(" ");
        if (keywords.length == 1) {
            List<Book> list1 = bookRepository.findByKeyword1(keywords[0]);
            List<Book> list2 = bookRepository.findByKeyword2(keywords[0]);
            List<Book> list3 = bookRepository.findByKeyword3(keywords[0]);
            list1.addAll(list2);
            list1.addAll(list3);
            return list1;
        } else if (keywords.length == 2) {
            List<Book> list1 = bookRepository.findByKeyword1AndKeyword2(
                    keywords[0], keywords[1]);
            List<Book> list2 = bookRepository.findByKeyword1AndKeyword2(
                    keywords[1], keywords[0]);
            List<Book> list3 = bookRepository.findByKeyword2AndKeyword3(
                    keywords[0], keywords[1]);
            List<Book> list4 = bookRepository.findByKeyword2AndKeyword3(
                    keywords[1], keywords[0]);
            List<Book> list5 = bookRepository.findByKeyword1AndKeyword3(
                    keywords[0], keywords[1]);
            List<Book> list6 = bookRepository.findByKeyword1AndKeyword3(
                    keywords[1], keywords[0]);
            list1.addAll(list2);
            list1.addAll(list3);
            list1.addAll(list4);
            list1.addAll(list5);
            list1.addAll(list6);
            return list1;
        } else if (keywords.length == 3) {
            List<Book> list1 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[0], keywords[1], keywords[2]);
            List<Book> list2 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[0], keywords[2], keywords[1]);
            List<Book> list3 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[1], keywords[0], keywords[2]);
            List<Book> list4 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[1], keywords[2], keywords[0]);
            List<Book> list5 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[2], keywords[1], keywords[0]);
            List<Book> list6 = bookRepository.findByKeyword1AndKeyword2AndKeyword3(
                    keywords[2], keywords[0], keywords[1]);
            list1.addAll(list2);
            list1.addAll(list3);
            list1.addAll(list4);
            list1.addAll(list5);
            list1.addAll(list6);
            return list1;
        }
        return null;
    }

    @Autowired
    DataSource dataSource;

    @Measure(value = "JDBC", warmup = 50)
    @GetMapping("keywords2/{keywordsString}")
    @Transactional
    public List<Book> getBookByTitleJDBC(@PathVariable String keywordsString) throws SQLException {
        StringBuilder builder = new StringBuilder();
        String[] keywords = keywordsString.split(" ");
        builder.append("'").append(keywords[0]).append("'");
        if (keywords.length>1) {
            builder.append(",'").append(keywords[1]).append("'");
        }
        if (keywords.length>2) {
            builder.append(",'").append(keywords[2]).append("'");
        }
        String keywordsIn = builder.toString();

        List<Book> books = new ArrayList<>();
        String SQL = "SELECT * FROM BOOK ";
        if (keywords.length == 1) {
            SQL += "WHERE KEYWORD1='"+keywords[0]+"' OR " +
                    "KEYWORD2='"+keywords[0]+"' OR " +
                    "KEYWORD3='"+keywords[0]+"'";
        } else if (keywords.length == 2) {
            SQL += "WHERE "+
                    "(KEYWORD1='"+keywords[0]+"' AND KEYWORD2='"+keywords[1]+"') OR " +
                    "(KEYWORD2='"+keywords[0]+"' AND KEYWORD1='"+keywords[1]+"') OR "+
                    "(KEYWORD1='"+keywords[0]+"' AND KEYWORD3='"+keywords[1]+"') OR " +
                    "(KEYWORD3='"+keywords[0]+"' AND KEYWORD1='"+keywords[1]+"') OR "+
                    "(KEYWORD2='"+keywords[0]+"' AND KEYWORD3='"+keywords[1]+"') OR " +
                    "(KEYWORD3='"+keywords[0]+"' AND KEYWORD2='"+keywords[1]+"')";
        } else if (keywords.length == 3) {
            SQL += "WHERE (KEYWORD1 IN (" + keywordsIn + ")) ";
            SQL += "AND (KEYWORD2 IN (" + keywordsIn + ")) ";
            SQL += "AND (KEYWORD3 IN (" + keywordsIn + ")) ";
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection
                     .prepareStatement(SQL)) {
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return books; // nothing found
            Book book = new Book();
            book.setId(resultSet.getInt("ID"));
            book.setTitle(resultSet.getString("TITLE"));
            book.setKeyword1(resultSet.getString("KEYWORD1"));
            book.setKeyword2(resultSet.getString("KEYWORD2"));
            book.setKeyword3(resultSet.getString("KEYWORD3"));
            books.add(book);
        }

        return books;
    }

    @Measure("test")
    @GetMapping("test")
    public String getTest() {
        return "just a test!";
    }

    @GetMapping("/random")
    public Book getBookRandom() {
        Random random = new Random();
        int index = random.nextInt(BOOKS_AMOUNT);
        List<Book> all = bookRepository.findAll();
        return all.get(index);
    }

    @Measure(value = "Title hash map 1", warmup = 50)
    @GetMapping("keywords3/{keywordsString}")
    public Set<Book> getBookByTitleHashMap(@PathVariable String keywordsString) {
        return bookService.getBooksByTitleHashMap(keywordsString);
    }

    @Measure(value = "Title hash map 2", warmup = 50)
    @GetMapping("keywords4/{keywordsString}")
    public List<Book> getBookByTitleHashMap2(@PathVariable String keywordsString) {
        return bookService.getBooksByTitleHashMap2(keywordsString);
    }

    @Measure(value = "Title by HM index", warmup = 50)
    @GetMapping("keywords5/{keywordsString}")
    public List<Book> getBookByHmIndex(@PathVariable String keywordsString) {
        return bookService.getBooksByHashHMIndex(keywordsString);
    }

    @Measure(value = "Title by FastUtil HM index", warmup = 50)
    @GetMapping("keywords6/{keywordsString}")
    public List<Book> getBookByFastUtilIndex(@PathVariable String keywordsString) {
        return bookService.getBooksByHashFastUtilIndex(keywordsString);
    }

    @GetMapping
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("read-all")
    public void readAll() {
        List<Book> all = bookRepository.findAll();
        for (Book book: all) {
            Book.initKeywords(book);
        }
    }

    @PostMapping
    public Book addBook(@RequestBody Book book) {
        Book.initKeywords(book);
        return bookRepository.save(book);
    }
/*
{
  "Title by HM index": {
    "name": "Title by HM index",
    "time": 38409,
    "callsCount": 8723,
    "callsCountMeasured": 8704,
    "latency": 4,
    "percent": 0.0,
    "boost": "420085.75",
    "percentile50": 22,
    "percentile90": 85,
    "percentile99": 1832
  },
  "Title hash map 1": {
    "name": "Title hash map 1",
    "time": 40369,
    "callsCount": 8708,
    "callsCountMeasured": 8693,
    "latency": 4,
    "percent": 0.0,
    "boost": "420085.75",
    "percentile50": 21,
    "percentile90": 69,
    "percentile99": 391
  },
  "baseline": {
    "name": "baseline",
    "time": 904024757,
    "callsCount": 587,
    "callsCountMeasured": 538,
    "latency": 1680343,
    "percent": 100.0,
    "boost": "1",
    "percentile50": 1828821,
    "percentile90": 2038513,
    "percentile99": 3582281
  },
  "Title by FastUtil HM index": {
    "name": "Title by FastUtil HM index",
    "time": 4978283,
    "callsCount": 8736,
    "callsCountMeasured": 8732,
    "latency": 570,
    "percent": 0.03,
    "boost": "2947.97",
    "percentile50": 638,
    "percentile90": 5172,
    "percentile99": 21880
  },
  "Title hash map 2": {
    "name": "Title hash map 2",
    "time": 36940,
    "callsCount": 8707,
    "callsCountMeasured": 8682,
    "latency": 4,
    "percent": 0.0,
    "boost": "420085.75",
    "percentile50": 20,
    "percentile90": 98,
    "percentile99": 2951
  },
  "3 keywords indexed in DB": {
    "name": "3 keywords indexed in DB",
    "time": 8900288,
    "callsCount": 8966,
    "callsCountMeasured": 8921,
    "latency": 997,
    "percent": 0.06,
    "boost": "1685.4",
    "percentile50": 1290,
    "percentile90": 12040,
    "percentile99": 28443
  }
}
 */

}
