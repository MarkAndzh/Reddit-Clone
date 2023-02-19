package com.example.redditclone.service;

import com.example.redditclone.dto.CommentsDto;
import com.example.redditclone.exceptions.PostNotFoundException;
import com.example.redditclone.model.Comment;
import com.example.redditclone.model.NotificationEmail;
import com.example.redditclone.model.Post;
import com.example.redditclone.model.User;
import com.example.redditclone.repository.CommentRepository;
import com.example.redditclone.repository.PostRepository;
import com.example.redditclone.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CommentsService {

    private static final String POST_URL = "";
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AuthService authService;
    private final PostService postService;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;

    public void save(CommentsDto commentsDto) {
        Comment comment = mapCommentDto(commentsDto);
        Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(commentsDto.getPostId().toString()));

        commentRepository.save(comment);
        
        String message = mailContentBuilder.build(post.getUser().getUsername() + " posted a comment on your post. " + POST_URL);
        sendCommentNotification(message, post.getUser());
    }

    private void sendCommentNotification(String message, User user) {
        mailService.sendMail(new NotificationEmail(user.getUsername() + " Commented on your post", user.getEmail(), message));
    }

    public CommentsDto mapToDto(Comment comment){
        return new CommentsDto(comment.getId(), comment.getPost().getPostId(), comment.getCreatedDate(), comment.getText(), comment.getUser().getUsername());
    }

    public Comment mapCommentDto(CommentsDto commentsDto){
        User currentUser = authService.getCurrentUser();
        Post post = postRepository.findById(commentsDto.getPostId())
                .orElseThrow( () -> new PostNotFoundException(commentsDto.getPostId().toString()));
        return Comment.builder().post(post).user(currentUser).createdDate(commentsDto.getCreatedDate()).text(commentsDto.getText()).id(commentsDto.getId()).build();
    }

    public List<CommentsDto> getAllCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow( () -> new PostNotFoundException(postId.toString()));
        return commentRepository.findByPost(post)
                .stream()
                .map( (comment) -> mapToDto(comment))
                .collect(Collectors.toList());

    }

    public List<CommentsDto> getAllCommentsForUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow( () -> new UsernameNotFoundException(username));
        return commentRepository
                .findAllByUser(user)
                .stream()
                .map( (comment) -> mapToDto(comment))
                .collect(Collectors.toList());
    }
}
