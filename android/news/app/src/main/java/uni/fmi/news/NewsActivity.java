package uni.fmi.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.news.adapters.NewsAdapter;
import uni.fmi.news.http.APIUtils;
import uni.fmi.news.http.ErrorUtils;
import uni.fmi.news.http.requests.CreateNewsRequest;
import uni.fmi.news.http.responses.APIError;
import uni.fmi.news.http.responses.MessageResponse;
import uni.fmi.news.http.responses.NewsEntity;
import uni.fmi.news.http.services.AuthService;
import uni.fmi.news.http.services.NewsService;

public class NewsActivity extends AppCompatActivity {
    ArrayList<NewsEntity> news = new ArrayList<>();
    RecyclerView newsRV;
    RecyclerView.Adapter newsRVAdapter;
    FloatingActionButton addNewsB;
    AuthService authService;
    NewsService newsService;
    EditText searchET;
    String phrase = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        newsRV = findViewById(R.id.newsRecyclerView);
        addNewsB = findViewById(R.id.news_page_add_news);
        searchET = findViewById(R.id.news_page_search_input);
        addNewsB.setOnClickListener(onClickAddNewsB);
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getNews(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        authService = APIUtils.getAuthService(getApplicationContext());
        newsService = APIUtils.getNewsService(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        newsRV = (RecyclerView) findViewById(R.id.newsRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        newsRV.setLayoutManager(mLayoutManager);

        new NewsAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle presses on the action bar items by id
        switch (item.getItemId()) {
            case R.id.app_bar_menu_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class NewsAsyncTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog = new ProgressDialog(NewsActivity.this);

        public NewsAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("Loading all news...");
            dialog.show();
            news.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getNews(phrase);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            dialog.cancel();
        }
    }

    public void logout(){
        Call<MessageResponse> call = authService.logout();
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();
                    Toast.makeText(NewsActivity.this, "Logout successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(NewsActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(NewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(NewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    View.OnClickListener onClickAddNewsB = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showAddDialog();
        }
    };

    private void showAddDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewsActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Create news");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(NewsActivity.this);
        final View addNewsView = layoutInflater.inflate(R.layout.add_news_dialog, null);
        alertDialogBuilder.setView(addNewsView);
        final AlertDialog addNewsDialog = alertDialogBuilder.create();
        addNewsDialog.show();

        Button createNewsB = addNewsView.findViewById(R.id.create_button);
        Button cancelNewsB = addNewsView.findViewById(R.id.cancel_button);

        cancelNewsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewsDialog.hide();
            }
        });

        createNewsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText titleET = addNewsView.findViewById(R.id.title_input);
                EditText contentET = addNewsView.findViewById(R.id.content_input);
                String title = titleET.getText().toString();
                String content = contentET.getText().toString();

                if(title.length() == 0){
                    Toast.makeText(getApplicationContext(), "Title is required", Toast.LENGTH_LONG).show();
                    return;
                }

                if(content.length() == 0){
                    Toast.makeText(getApplicationContext(), "Content is required", Toast.LENGTH_LONG).show();
                    return;
                }

                CreateNewsRequest createNewsRequest = new CreateNewsRequest();
                createNewsRequest.setTitle(title);
                createNewsRequest.setContent(content);
                addNews(createNewsRequest);
                addNewsDialog.hide();
            }
        });
    }

    public void addNews(CreateNewsRequest createNewsRequest){
        Call<NewsEntity> call = newsService.createNews(createNewsRequest);
        call.enqueue(new Callback<NewsEntity>() {
            @Override
            public void onResponse(Call<NewsEntity> call, Response<NewsEntity> response) {
                if(response.isSuccessful()){
                    Toast.makeText(NewsActivity.this, "The news was created successfully!", Toast.LENGTH_SHORT).show();
                    getNews("");
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(NewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(NewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(NewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getNews(String filter){
        Call<ArrayList<NewsEntity>> call = newsService.getNews(filter);
        call.enqueue(new Callback<ArrayList<NewsEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<NewsEntity>> call, Response<ArrayList<NewsEntity>> response) {
                if(response.isSuccessful()){
                    news.clear();
                    news.addAll(response.body());

                    newsRVAdapter = new NewsAdapter(news);
                    newsRV.setAdapter(newsRVAdapter);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(NewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(NewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<NewsEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(NewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}