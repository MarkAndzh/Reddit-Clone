package com.example.redditclone.service;

import com.example.redditclone.dto.PostRequest;
import com.example.redditclone.dto.PostResponse;
import com.example.redditclone.exceptions.PostNotFoundException;
import com.example.redditclone.exceptions.SubredditNotFoundException;
import com.example.redditclone.model.Post;
import com.example.redditclone.model.Subreddit;
import com.example.redditclone.model.User;
import com.example.redditclone.repository.CommentRepository;
import com.example.redditclone.repository.PostRepository;
import com.example.redditclone.repository.SubredditRepository;
import com.example.redditclone.repository.UserRepository;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AuthService authService;

    @Transactional
    public Post save(PostRequest postRequest) {
        Post post = mapPostDto(postRequest);
        User currentUser = authService.getCurrentUser();
        post.setUser(currentUser);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id){
        Post post = postRepository.findById(id)
                .orElseThrow( () -> new PostNotFoundException(id.toString()));
        return mapToDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(){
        return postRepository.findAll()
                .stream()
                .map((post) -> mapToDto(post))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow( () -> new SubredditNotFoundException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit).orElseThrow( () -> new PostNotFoundException("No posts found"));
        return posts.stream().map( (post) -> mapToDto(post)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow( () -> new UsernameNotFoundException(username));
        List<Post> posts = postRepository.findByUser(user)
                .orElseThrow( () -> new PostNotFoundException("No posts found"));
        return posts.stream()
                .map( (post) -> mapToDto(post))
                        .collect(Collectors.toList());
    }

    public PostResponse mapToDto(Post post){
        User user = authService.getCurrentUser();

        PostResponse.PostResponseBuilder postResponse = PostResponse.builder();

        postResponse.id(post.getPostId());
        postResponse.postName(post.getPostName());
        postResponse.url(post.getUrl());
        postResponse.description(post.getDescription());
        postResponse.userName(user.getUsername());
        postResponse.subredditName(post.getSubreddit().getName());
        postResponse.voteCount(post.getVoteCount());

        postResponse.commentCount(commentRepository.findByPost(post).size());
        postResponse.duration(TimeAgo.using(post.getCreatedDate().toEpochMilli()));

        return postResponse.build();
    }

    public Post mapPostDto(PostRequest postRequest){
        return new Post().builder()
                .postId(postRequest.getPostId())
                .user(authService.getCurrentUser())
                .subreddit(subredditRepository.findByName(postRequest.getSubredditName())
                        .orElseThrow( () -> new SubredditNotFoundException(postRequest.getSubredditName())))
                .postName(postRequest.getPostName())
                .url(postRequest.getUrl())
                .description(postRequest.getDescription())
                .voteCount(0)
                .createdDate(java.time.Instant.now())
                .build();
    }
}
