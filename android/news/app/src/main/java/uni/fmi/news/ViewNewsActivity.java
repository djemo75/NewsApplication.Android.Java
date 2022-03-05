package uni.fmi.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uni.fmi.news.http.APIUtils;
import uni.fmi.news.http.ErrorUtils;
import uni.fmi.news.http.requests.EditNewsRequest;
import uni.fmi.news.http.responses.APIError;
import uni.fmi.news.http.responses.MessageResponse;
import uni.fmi.news.http.responses.NewsEntity;
import uni.fmi.news.http.responses.ReactionEntity;
import uni.fmi.news.http.services.NewsService;
import uni.fmi.news.http.services.ReactionService;

public class ViewNewsActivity extends AppCompatActivity {
    TextView title;
    TextView content;
    TextView date;
    TextView time;
    TextView username;
    TextView likeCount;
    ImageView likeIcon;
    NewsService newsService;
    ReactionService reactionService;
    NewsEntity news;
    ArrayList<ReactionEntity> reactions = new ArrayList<>();
    int newsId;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_news);

        title = findViewById(R.id.view_news_title);
        content = findViewById(R.id.view_news_content);
        date = findViewById(R.id.view_news_date);
        time = findViewById(R.id.view_news_time);
        username = findViewById(R.id.view_news_username);
        likeCount = findViewById(R.id.view_news_like_count);
        likeIcon = findViewById(R.id.view_news_like_icon);
        newsService = APIUtils.getNewsService(getApplicationContext());
        reactionService = APIUtils.getReactionService(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        newsId = intent.getIntExtra("newsId", -1); // -1 is equal for not found

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        userId = preferences.getInt("id", -1);

        new NewsAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.view_news_app_bar_menu, menu);

        SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        int userId = preferences.getInt("id", -1);
        MenuItem editItem = menu.findItem(R.id.view_news_menu_edit);
        MenuItem deleteItem = menu.findItem(R.id.view_news_menu_delete);

        // Protect from third-party editing or deletion
        if (news.getUser().getId() != userId){
            editItem.setVisible(false);
            deleteItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle presses on the action bar items by id
        switch (item.getItemId()) {
            case R.id.view_news_menu_back:
                goBack();
                return true;
            case R.id.view_news_menu_edit:
                showEditDialog();
                return true;
            case R.id.view_news_menu_delete:
                deleteNews(newsId);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class NewsAsyncTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog dialog = new ProgressDialog(ViewNewsActivity.this);

        public NewsAsyncTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setTitle("Loading the news...");
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getNews(newsId);
            getReactions(newsId);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            dialog.hide();
        }
    }

    public void getNews(int newsId){
        Call<NewsEntity> call = newsService.getNewsById(newsId);
        call.enqueue(new Callback<NewsEntity>() {
            @Override
            public void onResponse(Call<NewsEntity> call, Response<NewsEntity> response) {
                if(response.isSuccessful()){
                    news = response.body();
                    String titleValue = news.getTitle().toString();
                    String contentValue = news.getContent().toString();
                    String dateValue = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(news.getCreatedDate());
                    String timeValue = new SimpleDateFormat("HH:mm").format(news.getCreatedDate());
                    String usernameValue = news.getUser().getUsername().toString()
                            +" ("+news.getUser().getName().toString()+")";
                    title.setText(titleValue);
                    content.setText(contentValue);
                    date.setText(dateValue);
                    time.setText(timeValue);
                    username.setText(usernameValue);

                    invalidateOptionsMenu(); // onCreateOptionsMenu(...) called again to hide the menu
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewNewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewNewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<NewsEntity> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewNewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void editNews(int id, EditNewsRequest editNewsRequest){
        Call<MessageResponse> call = newsService.editNews(newsId, editNewsRequest);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewNewsActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    getNews(newsId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewNewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewNewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewNewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteNews(int id){
        Call<MessageResponse> call = newsService.deleteNews(id);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    MessageResponse msgResponse = response.body();
                    Toast.makeText(ViewNewsActivity.this, msgResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ViewNewsActivity.this, NewsActivity.class);
                    startActivity(intent);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewNewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewNewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog()
    {
        // Create a AlertDialog Builder.
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewNewsActivity.this);
        // Set title, icon, can not cancel properties.
        alertDialogBuilder.setTitle("Edit news");
        alertDialogBuilder.setCancelable(false);
        LayoutInflater layoutInflater = LayoutInflater.from(ViewNewsActivity.this);
        final View addNewsView = layoutInflater.inflate(R.layout.add_news_dialog, null);
        alertDialogBuilder.setView(addNewsView);
        final AlertDialog editNewsDialog = alertDialogBuilder.create();
        editNewsDialog.show();

        Button editNewsB = addNewsView.findViewById(R.id.create_button);
        Button cancelNewsB = addNewsView.findViewById(R.id.cancel_button);
        EditText titleET = addNewsView.findViewById(R.id.title_input);
        EditText contentET = addNewsView.findViewById(R.id.content_input);

        titleET.setText(news.getTitle());
        contentET.setText(news.getContent());

        cancelNewsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNewsDialog.hide();
            }
        });

        editNewsB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                EditNewsRequest editNewsRequest = new EditNewsRequest();
                editNewsRequest.setTitle(title);
                editNewsRequest.setContent(content);
                editNews(newsId, editNewsRequest);
                editNewsDialog.hide();
            }
        });
    }

    public void goBack() {
        Intent intent = new Intent(ViewNewsActivity.this, NewsActivity.class);
        startActivity(intent);
    }

    public void getReactions(int newsId){
        Call<ArrayList<ReactionEntity>> call = reactionService.getReactionsByNewsId(newsId);
        call.enqueue(new Callback<ArrayList<ReactionEntity>>() {
            @Override
            public void onResponse(Call<ArrayList<ReactionEntity>> call, Response<ArrayList<ReactionEntity>> response) {
                if(response.isSuccessful()){
                    reactions = response.body();
                    likeCount.setText(""+reactions.size());

                    ReactionEntity currentReaction = null;
                    for(ReactionEntity reaction : reactions){
                        if(reaction.getUser().getId() == userId){
                            currentReaction = reaction;
                        }
                    }

                    likeIcon.setOnClickListener(null);
                    if(currentReaction!=null){
                        // Remove Like
                        likeIcon.setImageResource(R.drawable.icon_remove_like);
                    } else {
                        // Like
                        likeIcon.setImageResource(R.drawable.icon_like);
                    }

                    likeIcon.setOnClickListener(onClickLike);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewNewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewNewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ReactionEntity>> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewNewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    View.OnClickListener onClickLike = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ReactionEntity currentReaction = null;
            for(ReactionEntity reaction : reactions){
                if(reaction.getUser().getId() == userId){
                    currentReaction = reaction;
                }
            }

            if(currentReaction!=null){
                // Remove like action
                deleteReaction(currentReaction.getId());
            } else {
                // Like action
                createReaction(newsId);
            }
        }
    };

    public void createReaction(int id){
        Call<MessageResponse> call = reactionService.createReaction(id);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    getReactions(newsId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewNewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewNewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewNewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteReaction(int id){
        Call<MessageResponse> call = reactionService.deleteReaction(id);
        call.enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if(response.isSuccessful()){
                    getReactions(newsId);
                } else {
                    APIError error = ErrorUtils.parseError(response);
                    Toast.makeText(ViewNewsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 401){
                        Intent intent = new Intent(ViewNewsActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                Log.e("ERROR: ", t.getMessage());
                Toast.makeText(ViewNewsActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}