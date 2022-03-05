package uni.fmi.news.http.services;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import uni.fmi.news.http.requests.CreateNewsRequest;
import uni.fmi.news.http.requests.EditNewsRequest;
import uni.fmi.news.http.responses.MessageResponse;
import uni.fmi.news.http.responses.NewsEntity;
import uni.fmi.news.http.responses.UserDetails;

public interface NewsService {

    @GET("news")
    Call<ArrayList<NewsEntity>> getNews(@Query("phrase") String phrase);

    @GET("news/")
    Call<NewsEntity> getNewsById(@Query("id") int id);

    @POST("news/")
    Call<NewsEntity> createNews(@Body CreateNewsRequest createNewsRequest);

    @PUT("news")
    Call<MessageResponse> editNews(@Query("id") int id, @Body EditNewsRequest editNewsRequest);

    @DELETE("news/")
    Call<MessageResponse> deleteNews(@Query("id") int id);
}