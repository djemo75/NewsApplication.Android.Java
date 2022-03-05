package uni.fmi.news.http;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uni.fmi.news.utils.AddCookiesInterceptor;
import uni.fmi.news.utils.ReceivedCookiesInterceptor;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    Context context;

    public static Retrofit getClient(String url, Context context){
        if(retrofit == null){
            // Configuration for sending the cookies for authentication
            OkHttpClient client = new OkHttpClient(); // Configuration
            OkHttpClient.Builder builder = new OkHttpClient.Builder();// Configuration
            builder.addInterceptor(new AddCookiesInterceptor(context)); // Configuration
            builder.addInterceptor(new ReceivedCookiesInterceptor(context)); // Configuration
            client = builder.build(); // Configuration

            retrofit = new Retrofit.Builder().baseUrl(url)
                    .client(client) // Configuration
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

}
