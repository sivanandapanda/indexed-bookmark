package com.example.bookmark.jsoup;

import com.example.bookmark.domain.BookmarkDto;
import com.example.bookmark.elasticsearch.LocalSearchService;
import com.example.bookmark.model.Bookmark;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class BookmarkParser {

    @Inject
    LocalSearchService searchService;

    public void load() {
        List<String> failedLinks = new ArrayList<>();

        try {
            getAllLinks().parallelStream().forEach(link -> {
                try {
                    Document document = getDocument(link);
                    Set<String> description = findDescription(document);
                    String title = findTitle(document);
                    description.add(title);

                    Bookmark bookmarkForm = new Bookmark();
                    bookmarkForm.setUrl(link);
                    bookmarkForm.setName(title);
                    bookmarkForm.setTags(String.join(",", description));

                    BookmarkDto bookmarkDto = BookmarkDto.createFromModel(bookmarkForm);
                    BookmarkDto.persist(bookmarkDto);
                    searchService.index(Bookmark.fromDto(bookmarkDto));
                } catch (IOException e) {
                    //System.out.println("================");
                    //System.out.println("Failed for " + link);
                    //System.out.println("================");
                    //e.printStackTrace();
                    failedLinks.add(link);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println("Failed links => " + failedLinks);
    }

    private Document getDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .data("query", "Java")
                .userAgent("Mozilla")
                .cookie("auth", "token")
                .timeout(10000)
                .get();
    }

    private Set<String> findDescription(Document doc) throws IOException {
        return doc.select("meta")
                .parallelStream()
                .filter(e -> e.attr("name").equals("description"))
                .map(e -> e.attr("content"))
                .collect(Collectors.toSet());
    }

    private String findTitle(Document doc) {
        Elements elements = doc.select("head title");
        if(elements.size() == 0) {
            return "NOT FOUND";
        }

        return elements.get(0).text();
    }

    private List<String> getAllLinks() throws IOException {
        String html = new String(Files.readAllBytes(Paths.get(BookmarkParser.class.getResource("/bookmarks_1_10_21.html").getFile())));
        Document document = Jsoup.parse(html);
        Elements elements = document.select("dt a");
        return elements.parallelStream().map(e -> e.attr("href")).collect(Collectors.toList());
    }
}