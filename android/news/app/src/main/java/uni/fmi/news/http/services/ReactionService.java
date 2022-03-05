package uni.fmi.news.http.services;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import uni.fmi.news.http.requests.CreateNewsRequest;
import uni.fmi.news.http.requests.EditNewsRequest;
import uni.fmi.news.http.responses.MessageResponse;
import uni.fmi.news.http.responses.ReactionEntity;

public interface ReactionService {
    @GET("reactions/")
    Call<ArrayList<ReactionEntity>> getReactionsByNewsId(@Query("newsId") int newsId);

    @POST("reactions/")
    Call<MessageResponse> createReaction(@Query("newsId") int newsId);

    @DELETE("reactions/")
    Call<MessageResponse> deleteReaction(@Query("id") int id);
}