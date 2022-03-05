package uni.fmi.news;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.news.http.APIUtils;
import uni.fmi.news.http.ErrorUtils;
import uni.fmi.news.http.requests.RegisterRequest;
import uni.fmi.news.http.responses.APIError;
import uni.fmi.news.http.responses.MessageResponse;
import uni.fmi.news.http.services.AuthService;

public class RegisterActivity extends AppCompatActivity {
    EditText usernameET;
    EditText passwordET;
    EditText rePasswordET;
    EditText nameET;
    Button loginB;
    Button registerB;
    AuthService authService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameET = findViewById(R.id.registerpage_username_input);
        passwordET = findViewById(R.id.registerpage_password_input);
        rePasswordET = findViewById(R.id.registerpage_repassword_input);
        nameET = findViewById(R.id.registerpage_name_input);

        loginB = findViewById(R.id.registerpage_login_btn);
        loginB.setOnClickListener(onClickLogin);

        registerB = findViewById(R.id.registerpage_register_btn);
        registerB.setOnClickListener(onClickRegister);

        authService = APIUtils.getAuthService(getApplicationContext());
    }

    View.OnClickListener onClickLogin = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener onClickRegister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String username = usernameET.getText().toString();
            String name = nameET.getText().toString();
            String password = passwordET.getText().toString();
            String rePassword = rePasswordET.getText().toString();

            // Validations
            if(username.length() == 0){
                Toast.makeText(getApplicationContext(), "Username is required", Toast.LENGTH_LONG).show();
                return;
            }

            if(password.length() == 0){
                Toast.makeText(getApplicationContext(), "Password is required", Toast.LENGTH_LONG).show();
                return;
            }

            if(rePassword.length() == 0){
                Toast.makeText(getApplicationContext(), "Password is required", Toast.LENGTH_LONG).show();
                return;
            }

            if(!password.equals(rePassword)) {
                Toast.makeText(getApplicationContext(), "The passwords do not match", Toast.LENGTH_LONG).show();
                return;
            }

            if(name.length() == 0){
                Toast.makeText(getApplicationContext(), "Name is required", Toast.LENGTH_LONG).show();
                return;
            }

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername(username);
            registerRequest.setPassword(password);
            registerRequest.setName(name);
            register(registerRequest);
        }
    };

    public void register(RegisterRequest registerRequest){
        Call<MessageResponse> call = authService.register(registerRequest);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Register successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}