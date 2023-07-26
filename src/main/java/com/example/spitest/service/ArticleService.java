package com.example.spitest.service;

import com.example.spitest.domain.Article;
import com.example.spitest.repository.ArticleRepository;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final Lock lock = new ReentrantLock();


    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public void save(String title, String content) {
        articleRepository.save(new Article(title, content));
    }

    public void findById(Long articleId) {
        articleRepository.findById(articleId);
    }

    @Transactional
    public Article edit(Long articleId, Long editorId, String editContent) throws Exception {
        if (lock.tryLock()) {
            try {
                Article article = articleRepository.findArticleById(articleId).orElseThrow(() -> new Exception("Not Found Id"));

                System.out.println("락이 걸립니다 에디터:" + editorId + " | 락이 걸린 시간:" + LocalDateTime.now());

                Thread.sleep(5000);

                article.updateContent(editContent);

                return article;
            } finally {
                lock.unlock();
                System.out.println("락이 해제됐습니다 에디터:" + editorId + " | 락이 해제된 시간:" + LocalDateTime.now());

            }
        }
        else {
            System.out.println("이미 다른 에디터가 글을 수정중입니다. 수정이 완료될 때 까지 기다려주세요");
            return null;
        }
    }
}
