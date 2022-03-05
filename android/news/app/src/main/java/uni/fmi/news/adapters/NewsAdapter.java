package uni.fmi.news.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import uni.fmi.news.R;
import uni.fmi.news.ViewNewsActivity;
import uni.fmi.news.http.responses.NewsEntity;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private ArrayList<NewsEntity> news;
    private Context context;

    public NewsAdapter(ArrayList<NewsEntity> news) {
        this.news = news;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item, parent, false);

        context = parent.getContext();
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsEntity oneNews = news.get(position);

        holder.title.setText(oneNews.getTitle());
        holder.content.setText(oneNews.getContent());

        String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(oneNews.getCreatedDate());
        String time = new SimpleDateFormat("HH:mm").format(oneNews.getCreatedDate());
        holder.date.setText(date);
        holder.time.setText(time);


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewNewsActivity.class);
                intent.putExtra("newsId", oneNews.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (news != null) {
            return news.size();
        } else {
            return 0;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView title;
        public final TextView content;
        public final TextView date;
        public final TextView time;
        public final CardView card;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            title = view.findViewById(R.id.news_list_item_title);
            content = view.findViewById(R.id.news_list_item_content);
            date = view.findViewById(R.id.news_list_item_date);
            time = view.findViewById(R.id.news_list_item_time);
            card = view.findViewById(R.id.news_list_item_card);
        }
    }
}