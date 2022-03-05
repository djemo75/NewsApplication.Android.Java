package uni.fmi.news.http;

import android.content.Context;

import uni.fmi.news.http.services.AuthService;
import uni.fmi.news.http.services.NewsService;
import uni.fmi.news.http.services.ReactionService;

public class APIUtils {

    private APIUtils(){
    };

    public static final String API_URL = "http://192.168.1.130:8080/";

    public static AuthService getAuthService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(AuthService.class);
    }

    public static NewsService getNewsService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(NewsService.class);
    }

    public static ReactionService getReactionService(Context context){
        return RetrofitClient.getClient(API_URL, context).create(ReactionService.class);
    }
}