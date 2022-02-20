package com.luxoft.highperformance.bookserver;

import com.luxoft.highperformance.bookserver.model.Book;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    public final int BOOKS_AMOUNT=100_000;

    public List<Book> generateBooks() {
        List<Book> books = new ArrayList<>(BOOKS_AMOUNT);
        Random random = new Random();
        for (int i=0; i<BOOKS_AMOUNT; i++) {
            String title = "Book"+random.nextInt(BOOKS_AMOUNT);
            String authorName = "AuthorName"+random.nextInt(BOOKS_AMOUNT)+" ";
            String authorSurname = "AuthorSurname"+random.nextInt(BOOKS_AMOUNT);
            Book book = new Book();
            book.setTitle(title+" by "+authorName+authorSurname);
            Book.initKeywords(book);
        }
        return books;
    }

    public Set<Book> getBooksByTitleHashMap(String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        Set<Book> bookSet = null;
        for (String keyword : keywords) {
            Set<Book> booksWithKeywordSet = new HashSet<>();
            Map<String, Set<Book>> map = Book.keywordMap;
            if (map.containsKey(keyword)) {
                booksWithKeywordSet.addAll(map.get(keyword));
            }
            if (bookSet == null) {
                bookSet = booksWithKeywordSet;
            } else {
                bookSet.retainAll(booksWithKeywordSet);
            }
        }

        return bookSet;
    }

    public List<Book> getBooksByTitleHashMap2(String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        List<Book> res = new ArrayList<>();
        Map<String, Set<Book>> map = Book.keywordMap;
        Set<Book> books = map.get(keywords[0]);
        if (books != null) {
            for (Book book : books) {
                if (book.getKeywords().containsAll(Arrays.asList(keywords))) {
                    res.add(book);
                }
            }
        }
        return res;
    }

    public List<Book> getBooksByHashHMIndex(String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        List<Book> res = new ArrayList<>();
        Map<Integer, Set<Book>> map = Book.booksByHashHM;
        Set<Book> books = map.get(keywords[0].hashCode());
        if (books != null) {
            for (Book book : books) {
                if (book.getKeywords().containsAll(Arrays.asList(keywords))) {
                    res.add(book);
                }
            }
        }
        return res;
    }

    public List<Book> getBooksByHashFastUtilIndex(String keywordsString) {
        String[] keywords = keywordsString.split(" ");

        List<Book> res = new ArrayList<>();
        Int2ObjectMap<Set<Book>> map = Book.booksByHashFastUtil;
        Set<Book> books = map.get(keywords[0].hashCode());
        if (books != null) {
            for (Book book : books) {
                if (book.getKeywords().containsAll(Arrays.asList(keywords))) {
                    res.add(book);
                }
            }
        }
        return res;
    }
}
