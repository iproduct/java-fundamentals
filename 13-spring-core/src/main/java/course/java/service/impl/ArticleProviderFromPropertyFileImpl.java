package course.java.service.impl;

import course.java.model.Article;
import course.java.qualifiers.FromPropertySource;
import course.java.qualifiers.Mock;
import course.java.service.ArticleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@FromPropertySource
@Order(1)
@Service("propertiesProvider")
public class ArticleProviderFromPropertyFileImpl implements ArticleProvider {
    @Value("${blogs.titles}")
    private String[] titles;
    private List<Article> articles;

    @PostConstruct
    private void init() {
        articles = Arrays.stream(titles)
                .map(String::trim)
                .map(title -> new Article(title, title + " content ...",
                        "Default Author",
                        Arrays.stream(title.split(" ")).map(String::toLowerCase).collect(Collectors.toSet())
                )).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Article> getArticles() {
        return articles;
    }

    @Override
    public List<Article> getArticlesByTitleContaining(String titlePart) {
        return articles.stream()
                .filter(art -> art.getTitle().contains(titlePart))
                .collect(Collectors.toList());
    }
}