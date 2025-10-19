package net.cycastic.sigil.service.feign;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "jplaceholder", url = "https://jsonplaceholder.typicode.com/")
public interface JSONPlaceHolderClient {
    @Data
    public static class Post {
        private String title;
        private String body;
    }

    @GetMapping("/posts")
    List<Post> getPosts();

    @GetMapping(value = "/posts/{postId}", produces = "application/json")
    Post getPostById(@PathVariable("postId") Long postId);
}
