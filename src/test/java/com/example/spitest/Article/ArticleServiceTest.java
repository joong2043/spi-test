package com.example.spitest.Article;

import com.example.spitest.domain.Article;
import com.example.spitest.domain.Editor;
import com.example.spitest.repository.EditorRepository;
import com.example.spitest.service.ArticleService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArticleServiceTest {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private EditorRepository editorRepository;

    @BeforeAll
    public void 데이터_넣기() {

        for (int i = 1; i <= 10; i++) {
            editorRepository.save(new Editor());
        }

        articleService.save("첫번째 게시글", "content0");
        articleService.save("두번째 게시글", "content0");
    }

    @Test
    public void 락이_잘_작동하는지_테스트() throws Exception {

        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 다른 스레드에서 수행하는 작업을 기다리도록 도와줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger collisionCount = new AtomicInteger(0);

        executorService.submit(() -> {

            for (int i = 1; i <= 10; i++) {
                long editorId = i;

                try {
                    Article article = articleService.edit(2L, editorId, "content edit " + editorId);

                    System.out.println("글 수정 성공 : " + article.getContent());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }

            }
        });

        latch.await();

        assert (collisionCount.get() == 0);
    }

    @Test
    public void 여러명이_동시에_글을_수정할_때_충돌이_발생합니다() throws Exception {

        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 다른 스레드에서 수행하는 작업을 기다리도록 도와줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger collisionCount = new AtomicInteger(0);

        for (int i = 1; i <= 10; i++) {

            long editorId = i;

            executorService.submit(() -> {

                try {
                    Article article = articleService.edit(1L, editorId, "content edit " + editorId);
                    System.out.println("글 수정 성공 : " + article.getContent());
                } catch (Exception e) {
                    collisionCount.incrementAndGet();
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        assert(collisionCount.get() == 9);

    }

    @Test
    public void 첫번째_글과_두번째_글을_동시에_수정_가능한지_테스트() throws InterruptedException {
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 다른 스레드에서 수행하는 작업을 기다리도록 도와줌
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger collisionCount1 = new AtomicInteger(0);
        AtomicInteger collisionCount2 = new AtomicInteger(0);


        executorService.submit(() -> {

            for (int i = 1; i <= 5; i++) {

                long editorId = i;

                try {
                    Article article = articleService.edit(1L, editorId, "content edit " + editorId);

                    System.out.println("글 수정 성공 : " + article.getContent());
                } catch (Exception e) {
                    collisionCount1.getAndIncrement();
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            }

        });

        executorService.submit(() -> {

            for (int i = 6; i <= 10; i++) {

                long editorId = i;

                try {
                    Article article = articleService.edit(2L, editorId, "content edit " + editorId);

                    System.out.println("글 수정 성공 : " + article.getContent());
                } catch (Exception e) {
                    collisionCount2.getAndIncrement();
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            }
        });

        latch.await();

        assert (collisionCount1.get()==0 && collisionCount2.get()==0);
    }
}
