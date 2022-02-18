package com.luxoft.highperformance.bookserver.model;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter @ToString
@Table(indexes = {
        @Index(columnList = "KEYWORD1"),
        @Index(columnList = "KEYWORD2"),
        @Index(columnList = "KEYWORD3")
})
public class Book {
    @Id
    @GeneratedValue
    private Integer id;
    private String title;
    private String keyword1;
    private String keyword2;
    private String keyword3;

    @Transient
    @XmlTransient
    private Set<String> keywords = new HashSet<>();

    public final static int KEYWORDS_AMOUNT = 3;
    public static Map<String, Set<Book>> keywordMap = new ConcurrentHashMap<>();

    public static void initKeywords(Book book) {
        String[] keywords = book.getTitle().split(" ");
        if (keywords.length > 0) book.setKeyword1(keywords[0]);
        if (keywords.length > 1) book.setKeyword2(keywords[2]);
        if (keywords.length > 2) book.setKeyword3(keywords[3]);
        addToHashMaps(book, List.of(keywords[0],keywords[2],keywords[3]));
    }

    private static void addToHashMaps(Book book, List<String> keywords) {
        for (int i=0; i< KEYWORDS_AMOUNT; i++) {
            String keyword = keywords.get(i);
            if (keywordMap.containsKey(keyword)) {
                keywordMap.get(keyword).add(book);
            } else {
                HashSet<Book> set = new HashSet<>();
                set.add(book);
                keywordMap.put(keyword, set);
            }
        }
        book.keywords.addAll(keywords);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        return title != null ? title.equals(book.title) : book.title == null;
    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }
}
