package com.example.spitest.Article;

import com.example.spitest.domain.Article;
import com.example.spitest.domain.Editor;
import com.example.spitest.repository.EditorRepository;
import com.example.spitest.service.ArticleService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private EditorRepository editorRepository;

    @Test
    public void 여러명이_동시에_글을_수정() throws Exception {
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 다른 스레드에서 수행하는 작업을 기다리도록 도와줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 1; i <= 10; i++) {
            editorRepository.save(new Editor());
        }

        articleService.save("첫번째 게시글","content0");
        articleService.save("두번째 게시글","content0");

        for (int i = 1; i <= 5; i++) {

            long editorId = i;

            executorService.submit(() -> {
                try {
                    Article article = articleService.edit(1L, editorId, "content edit "+editorId);

                    System.out.println("글 수정 성공 : " + article.getContent());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });

            Thread.sleep(5000);
        }

        for (int i = 6; i <= 10; i++) {

            long editorId = i;

            executorService.submit(() -> {
                try {
                    Article article = articleService.edit(2L, editorId, "content edit "+editorId);

                    System.out.println("글 수정 성공 : " + article.getContent());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }
}
