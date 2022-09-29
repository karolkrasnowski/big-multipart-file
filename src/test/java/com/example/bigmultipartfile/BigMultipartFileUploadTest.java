package com.example.bigmultipartfile;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@SpringBootTest
class BigMultipartFileUploadTest {

    private static final int NUMBER_OF_RESOURCES = 30;
    private WebTestClient webTestClient;

    @BeforeEach
    void before(ApplicationContext applicationContext) {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    /**
     * This test passes all the time
     */
    @Test
    void shouldUploadSmallFile() {
        // given
        int resourceSize = 5_000;
        var body = buildMultiValueMap(resourceSize);

        // expect
        sendPostRequest(body).isEqualTo(resourceSize * NUMBER_OF_RESOURCES);
    }

    /**
     * This test fails most of the time
     */
    @Test
    void shouldUploadBigFile() {
        // given
        int resourceSize = 5_000_000;
        var body = buildMultiValueMap(resourceSize);

        // expect
        sendPostRequest(body).isEqualTo(resourceSize * NUMBER_OF_RESOURCES);
    }

    private WebTestClient.BodySpec<Integer, ?> sendPostRequest(MultiValueMap<String, HttpEntity<?>> body) {
        return webTestClient.post()
                .uri("/files")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .exchange()
                .expectBody(Integer.class);
    }

    private MultiValueMap<String, HttpEntity<?>> buildMultiValueMap(int resourceSize) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        for (int i = 0; i < NUMBER_OF_RESOURCES; i++) {
            builder.part("item-" + i, newByteArrayResource(i, resourceSize));
        }
        return builder.build();
    }

    private ByteArrayResource newByteArrayResource(int fileIndex, int resourceSize) {
        return new ByteArrayResource(RandomUtils.nextBytes(resourceSize)) {
            @Override
            public String getFilename() {
                return "filename-" + fileIndex;
            }
        };
    }
}
