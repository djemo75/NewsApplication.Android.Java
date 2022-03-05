package uni.fmi.news.http.services;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import uni.fmi.news.http.requests.LoginRequest;
import uni.fmi.news.http.requests.RegisterRequest;
import uni.fmi.news.http.responses.MessageResponse;
import uni.fmi.news.http.responses.UserDetails;

public interface AuthService {

    @GET("auth/profile")
    Call<UserDetails> getProfile();

    @POST("auth/login")
    Call<UserDetails> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<MessageResponse> register(@Body RegisterRequest registerRequest);

    @POST("auth/logout")
    Call<MessageResponse> logout();
}