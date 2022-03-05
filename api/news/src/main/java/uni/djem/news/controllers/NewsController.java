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

import uni.djem.news.RequestDtos.CreateNewsRequest;
import uni.djem.news.RequestDtos.EditNewsRequest;
import uni.djem.news.ResponseDtos.MessageResponse;
import uni.djem.news.entities.NewsEntity;
import uni.djem.news.entities.UserEntity;
import uni.djem.news.repositories.NewsRepository;

@RequestMapping(path="/news")
@RestController
public class NewsController {
	private NewsRepository newsRepository;
	
	public NewsController(NewsRepository newsRepository) {
		this.newsRepository=newsRepository;
	}
	
	@GetMapping("")
	public ResponseEntity<List<NewsEntity>> getNews(@RequestParam(required = false, defaultValue = "") String phrase) {
		List<NewsEntity> news = newsRepository.findByTitleContainingIgnoreCaseOrderByCreatedDateDesc(phrase);

		return new ResponseEntity<List<NewsEntity>>(news, HttpStatus.OK);
	}
	
	@GetMapping("/")
	public ResponseEntity<NewsEntity> getNewsById(@RequestParam int id) {
		NewsEntity news = newsRepository.findById(id);
		
		if(news==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "News with this id not found!");
		}
		
		return new ResponseEntity<NewsEntity>(news, HttpStatus.OK);
	}
	
	@PostMapping("/")
	public ResponseEntity<NewsEntity> createNews(@RequestBody CreateNewsRequest newsRequest, HttpSession session){
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		if(newsRequest.getTitle()=="" || newsRequest.getContent()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide title and content!");
		}
		
		NewsEntity news = new NewsEntity();
		news.setTitle(newsRequest.getTitle());
		news.setContent(newsRequest.getContent());
		news.setUser(user);
		news.setCreatedDate(new Date());
		
		newsRepository.saveAndFlush(news);
		return new ResponseEntity<NewsEntity>(news, HttpStatus.OK);
	}
	
	@PutMapping("")
	public ResponseEntity<MessageResponse> editNews(@RequestParam int id, @RequestBody EditNewsRequest newsRequest, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		NewsEntity news = newsRepository.findById(id);
		
		if(news==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "News with this id not found!");
		}
		
		if(news.getUser().getId()!=user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't edit other people's news!");
		}
		
		if(newsRequest.getTitle()=="" || newsRequest.getContent()=="") {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You need to provide title and content!");
		}
		
		news.setTitle(newsRequest.getTitle());
		news.setContent(newsRequest.getContent());
		
		newsRepository.save(news);
		
		MessageResponse response = new MessageResponse("You have successfully edited the news");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
	
	
	@DeleteMapping("")
	public ResponseEntity<MessageResponse> deleteNews(@RequestParam int id, HttpSession session) {
		UserEntity user = (UserEntity)session.getAttribute("user");
		
		if(user==null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You need to login!");
		}
		
		NewsEntity news = newsRepository.findById(id);
		
		if(news==null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "News with this id not found!");
		}
		
		if(news.getUser().getId()!=user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't delete other people's news!");
		}
		
		newsRepository.delete(news);
		
		MessageResponse response = new MessageResponse("You have successfully deleted the news");
		return new ResponseEntity<MessageResponse>(response, HttpStatus.OK);
	}
}