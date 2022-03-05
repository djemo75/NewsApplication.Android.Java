package uni.fmi.news;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import uni.fmi.news.http.APIUtils;
import uni.fmi.news.http.services.AuthService;
import uni.fmi.news.http.requests.LoginRequest;
import uni.fmi.news.http.responses.UserDetails;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        boolean remember = preferences.getBoolean("remember", false);

        authService = APIUtils.getAuthService(getApplicationContext());
        if (remember) {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(preferences.getString("username", ""));
            loginRequest.setPassword(preferences.getString("password", ""));
            loginAutomatically(loginRequest);
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void loginAutomatically(LoginRequest loginRequest){
        Call<UserDetails> call = authService.login(loginRequest);
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if(response.isSuccessful()){
                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}