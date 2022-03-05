package uni.fmi.news.http.responses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class ReactionEntity {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("createdDate")
    @Expose
    private Date createdDate;

    @SerializedName("user")
    @Expose
    private UserDetails user;

    @SerializedName("news")
    @Expose
    private NewsEntity news;

    public ReactionEntity() {
    }

    public ReactionEntity(int id, UserDetails user, NewsEntity news) {
        this.id = id;
        this.user = user;
        this.news = news;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public NewsEntity getNews() {
        return news;
    }

    public void setNews(NewsEntity news) {
        this.news = news;
    }
}
