package uni.djem.news.controllers;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uni.djem.news.ResponseDtos.MessageResponse;
import uni.djem.news.entities.NewsEntity;
import uni.djem.news.entities.ReactionEntity;
import uni.djem.news.entities.UserEntity;
import uni.djem.news.repositories.NewsRepository;
import uni.djem.news.repositories.ReactionRepository;

@RequestMapping(path="/reactions")
@RestController
public class ReactionController {
	private ReactionRepository reactionRepository;
	private NewsRepository newsRepository;
	
	public ReactionController(ReactionRepository reactionRepository, NewsRepository newsRepository) {
		this.reactionRepository=reactionRepository;
		this.newsRepository=newsRepository;
	}
	
	@GetMapping("")
	public ResponseEntity<List<ReactionEntity>> getLikesByNewsId(@RequestParam int newsId){
		List<ReactionEntity> reactions = reactionRepository.findByNewsIdOrderByCreatedDateDesc(newsId);
		
		return new ResponseEntity<List<ReactionEntity>>(reactions,HttpStatus.OK);
	}
	
	@PostMapping("/")
	public ResponseEntity<ReactionEntity> createReaction (@RequestParam int newsId, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		NewsEntity news = newsRepository.findById(newsId);
		
		if(news==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found news with this id!");
		}
		
		ReactionEntity existingReaction = reactionRepository.findByNewsIdAndUserId(news.getId(), user.getId());
		
		if(existingReaction!=null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have reaction on this news!");
		}
		
		ReactionEntity reaction = new ReactionEntity();
		reaction.setNews(news);
		reaction.setUser(user);
		reaction.setCreatedDate(new Date());
		
		reactionRepository.saveAndFlush(reaction);
		return new ResponseEntity<ReactionEntity>(reaction, HttpStatus.OK);
	}

	@DeleteMapping("")
	public ResponseEntity<MessageResponse> deleteReaction(@RequestParam int id, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		ReactionEntity reaction = reactionRepository.findById(id);
		
		if(reaction==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reaction with this id not found!");
		}
		
		if(reaction.getUser().getId()!=user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete other people's reaction!");
		}
		
		reactionRepository.delete(reaction);
		
		MessageResponse response = new MessageResponse("You have successfully deleted the reaction");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
}
