package com.example.bigmultipartfile;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
class FilesController {

    @PostMapping(value = "/files")
    Mono<Integer> uploadFiles(@RequestBody Flux<Part> parts) {
        return parts
                .filter(FilePart.class::isInstance)
                .map(FilePart.class::cast)
                .flatMap(part -> DataBufferUtils.join(part.content())
                        .map(buffer -> {
                            byte[] data = new byte[buffer.readableByteCount()];
                            buffer.read(data);
                            DataBufferUtils.release(buffer);
                            return data;
                        })
                )
                .map(bytes -> bytes.length)
                .reduce(0, Integer::sum);
    }
}