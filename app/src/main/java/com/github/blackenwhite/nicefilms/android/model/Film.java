package com.github.blackenwhite.nicefilms.android.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created on 22.10.2015.
 */
public class Film implements Comparable {
    private String title;
    private String director;
    private String release;
    private String runtime;
    private String genre;
    private String actors;
    private String country;
    private String plot;
    private String awards;

    private String id;
    private Double imdbRating;
    private String imdbUrl;
    private Integer imdbVotes;

    private Integer tomatoMeter;
    private Double tomatoRating;
    private Integer tomatoReviews;
    private Integer tomatoUserMeter;
    private String tomatoUserRating;
    private String tomatoUserReviews;

    private String metascore;

    public Film(JSONObject o) {
        try {
            title = o.get("title").toString();
            director = o.get("director").toString();
            imdbRating = Double.parseDouble(o.get("imdbRating").toString());
            imdbVotes = Integer.parseInt(o.get("imdbVotes").toString());
            tomatoUserRating = o.get("tomatoUserRating").toString();
            tomatoUserReviews = o.get("tomatoUserReviews").toString();
            metascore = o.get("metascore").toString();
            genre = o.get("genre").toString();
            runtime = o.get("runtime").toString();
            release = o.get("release").toString();
            country = o.get("country").toString();
            actors = o.get("actors").toString();
            plot = o.get("plot").toString();
            awards = o.get("awards").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPopupHeader() {
        StringBuilder header = new StringBuilder();
        header.append(title.toUpperCase()).append("");

        return header.toString();
    }

    public String getPopupSubHeader() {
        StringBuilder subHeader = new StringBuilder();
        if (!runtime.equals("N/A")) {
            subHeader.append(runtime).append(" | ");
        }
        subHeader.append(genre).append(" | ").append(release);
        String country = (Arrays.asList(this.country.split(","))).get(0);
        subHeader.append(" (").append(country).append(")");
        return subHeader.toString();
    }

    public String getPopupBody() {
        StringBuilder body = new StringBuilder();

        int len = 12;
        int spaces = len - "IMDB: ".length();
        String format = String.format("\n%%-10s%%%ds%%s/10 | %%s votes\n", spaces);
        body.append(String.format(format,
                "IMDB: ", " ", imdbRating, imdbVotes));
        if (!tomatoUserRating.equals("0.0")) {
            spaces = len - "Tomatoes: ".length();
            format = String.format("%%-10s%%%ds%%s/5    | %%s votes\n", spaces);
            body.append(String.format(format,
                    "Tomatoes: ", " ", tomatoUserRating, tomatoUserReviews));
        }
        if (!metascore.equals("0")) {
            int score = Integer.parseInt(metascore);
            String review = "";
            if (score >= 40 && score <= 60) {
                review = "[ Average ]";
            } else if (score >= 61 && score <= 80) {
                review = "[ Favorable ]";
            } else if (score >= 81) {
                review = "[ Universal Acclaim ]";
            }

            spaces = len - "Metascore: ".length();
            format = String.format("%%-10s%%%ds%%s %%s\n", spaces);
            body.append(String.format(format, "Metascore: ", " ", metascore, review));
        }
        body.append("\nDIRECTOR: ").append(director).
                append("\nSTARRING: ").append(actors);
        if (!awards.equals("N/A")) {
            body.append("\n\nAWARDS: ").append(awards);
        }

        body.append("\n\nPLOT\n").append(plot);

        return body.toString();
    }

    public JSONObject getJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put("title", title);
            o.put("director", director);
            o.put("imdbRating", imdbRating);
            o.put("imdbVotes", imdbVotes);
            o.put("country", country);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Film another = (Film) o;
        int lhs = Integer.parseInt(getImdbVotes());
        int rhs = Integer.parseInt(another.getImdbVotes());
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public String toString() {
        return String.format("%s\n%s/10 from %s users", title, imdbRating,  imdbVotes);
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public String getCountry() {
        return country;
    }

    public String getGenre() {
        return genre;
    }

    public String getImdbRating() {
        return imdbRating.toString();
    }

    public String getImdbVotes() {
        return imdbVotes.toString();
    }

    public void setTomatoUserRating(String tomatoUserRating) {
        this.tomatoUserRating = tomatoUserRating;
    }

    public void setTomatoUserReviews(String tomatoUserReviews) {
        this.tomatoUserReviews = tomatoUserReviews;
    }
}
