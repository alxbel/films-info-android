package info.nice_films.androidclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FilmsByYearResultsActivity extends AppCompatActivity {
    private final String LOG_TAG = FilmsByYearResultsActivity.class.getSimpleName();

    private static final String LOCALHOST_EMULATOR = "http://10.0.2.2:8080/MovServer";
    private static final String LOCALHOST_REAL_DEVICE = "http://192.168.1.34:8080/MovServer";
    private static final String INTERNET_HOST = "http://www.nice-films.info";
    private static final String SEARCH_REQUEST = "/search";
    private static final String DEBUG_REQUEST = "/debug";
    private static final String SRV_ERROR_REQUEST = "/error";

    private Context context;
    private RelativeLayout root;

    private PopupWindow popupMessage;
    private Button btnDismissPopup;
    private View popupView;
    private LinearLayout filmDetailsContainer;
    private LinearLayout filmDetailsContent;
    private NestedScrollView filmsDetailsScrollView;
    private LinearLayout rowsInPopupFilmDetails;

    private ListView filmListView;
    private ArrayAdapter<Film> filmsAdapter;
    private Map<String, String> requestParams;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_films_by_year_results);

        root = (RelativeLayout) findViewById(R.id.findFilmsByYearResultsView);
        context = this;

        initPopup();
        initFilmListView();

        startSearch();
    }

    private void initPopup() {
        popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);
        filmDetailsContainer = (LinearLayout) popupView.findViewById(R.id.layout_film_details_container);
        filmDetailsContent = (LinearLayout) popupView.findViewById(R.id.layout_film_details_content);
        filmsDetailsScrollView = (NestedScrollView) popupView.findViewById(R.id.scrollview_film_details);

        popupMessage = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        popupMessage.setContentView(popupView);

        btnDismissPopup = (Button) popupView.findViewById(R.id.button_dismiss_popup);
        btnDismissPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMessage.dismiss();
            }
        });
        btnDismissPopup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.getBackground().setColorFilter(Color.parseColor("#c0d9ee"), PorterDuff.Mode.SRC_ATOP);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.getBackground().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return false;
            }
        });


        rowsInPopupFilmDetails = new LinearLayout(context);
        rowsInPopupFilmDetails.setOrientation(LinearLayout.VERTICAL);
    }

    private void initFilmListView() {
        filmListView = (ListView) findViewById(R.id.listview_films);
        int[] colors = {0, 0xFFFF0000, 0}; // red
        filmListView.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        filmListView.setDividerHeight(1);

        filmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Remove previous details
                filmsDetailsScrollView.removeAllViews();

                LinearLayout rowsInPopupFilmDetails = new LinearLayout(context);
                rowsInPopupFilmDetails.setOrientation(LinearLayout.VERTICAL);

                Film film = (Film) parent.getAdapter().getItem(position);
                rowsInPopupFilmDetails.addView(getFormattedPopupText(film.getPopupHeader(), 30));
                rowsInPopupFilmDetails.addView(getFormattedPopupText(film.getPopupSubHeader(), 15));
                rowsInPopupFilmDetails.addView(getFormattedPopupText(film.getPopupBody(), 18));
                filmsDetailsScrollView.addView(rowsInPopupFilmDetails);

                popupMessage.showAtLocation(root, Gravity.TOP | Gravity.CENTER, 0, -300);

                Log.d(LOG_TAG, film.toString());
            }
        });
    }

    private void startSearch() {
        intent = getIntent();
        requestParams = new HashMap<String, String>();
        requestParams.put("genre", intent.getStringExtra("genre"));
        requestParams.put("year", new Integer(intent.getIntExtra("year", 1900)).toString());
        requestParams.put("rating", new Double(intent.getDoubleExtra("rating", 7.0)).toString());

        new GetFilmsInfoTask().execute();
    }

    private TextView getFormattedPopupText(String text, int textSize) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(textSize);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(20, 0, 20, 0);

        return tv;
    }

    private class GetFilmsInfoTask extends AsyncTask<Void, Void, Void> {

        final String LOG_TAG = GetFilmsInfoTask.class.getSimpleName();
        boolean isServerAvailable = true;
        String responseMessage;
        int responseCode = -1;

        String content = null;
        List<Film> filmList = new LinkedList<Film>();


        @Override
        protected Void doInBackground(Void ... params) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                String host = LOCALHOST_REAL_DEVICE;
                isServerAvailable = isHostAvailable(host);
                if (!isServerAvailable) {
                    return null;
                }

                String request = SEARCH_REQUEST;
                URL url = new URL(host + request);

                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("genre", requestParams.get("genre"))
                        .appendQueryParameter("year", requestParams.get("year"))
                        .appendQueryParameter("minRating", requestParams.get("rating"));
                String query = builder.build().getEncodedQuery();
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8")
                );
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                // Read the input stream into a String
                InputStream inputStream = conn.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                content = buffer.toString();

                responseCode = conn.getResponseCode();
                responseMessage = conn.getResponseMessage();

                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
                try {
                    Log.d(LOG_TAG, String.format("Response: %d: %s", conn.getResponseCode(), conn.getResponseMessage()));
                    responseMessage = conn.getResponseMessage();
                    responseCode = conn.getResponseCode();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            if (!isServerAvailable) {
                showMessageAndFinishActivity("Server unavailable", Color.RED, -30);
                return;
            }

            if (responseCode != 200) {
                showMessageAndFinishActivity(
                        String.format("Server returned error:\n%d: %s",
                                responseCode != -1 ? responseCode : "",
                                responseMessage != null ? responseMessage : ""),
                        Color.RED, 50);
                return;
            }

            if (parseJSON() == 0) {
                showMessageAndFinishActivity("Nothing found", Color.WHITE, -30);
                return;
            }

            filmsAdapter = new ArrayAdapter<Film>(
                    FilmsByYearResultsActivity.this, R.layout.list_item_film,
                    R.id.list_item_film_textview,
                    filmList
            );
            filmListView.setAdapter(filmsAdapter);

        }

        private void showMessageAndFinishActivity(String message, int color, int padding) {
            filmsDetailsScrollView.removeAllViews();
            LinearLayout error = new LinearLayout(context);
            error.setOrientation(LinearLayout.VERTICAL);

            error.addView(getErrorPopupText(message, padding));

            GradientDrawable gd = new GradientDrawable();
            gd.setCornerRadius(2);
            gd.setStroke(5, color);
            filmDetailsContent.setBackgroundDrawable(gd);

            filmsDetailsScrollView.addView(error);

            popupMessage.showAtLocation(root, Gravity.TOP | Gravity.CENTER, 0, 0);
            btnDismissPopup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupMessage.dismiss();
                    finish();
                }
            });
        }

        private TextView getErrorPopupText(String text, int padding) {
            TextView error = getFormattedPopupText(text, 30);
            error.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            error.setPadding(10, width / 2 - padding, 10, 100);
            return error;
        }

        private boolean isHostAvailable(String host) {
            Socket socket = null;
            host = host.replaceAll("http://|www.|:8080/MovServer", "");
            Log.d(LOG_TAG, "checking " + host);
            try {
                int port = 80;
                if (host.contains("192.168")) {
                    port = 8080;
                }
                socket = new Socket(host, port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, e.getMessage());
                return false;
            }

            if (socket != null) {
                Log.d(LOG_TAG, host + " available");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            Log.d(LOG_TAG, host + " unavailable");
            return false;
        }

        private int parseJSON() {
            try {
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject o = (JSONObject) jsonArray.get(i);
                    Film film = new Film(o);
                    filmList.add(film);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Collections.sort(filmList, Collections.reverseOrder());
            return filmList.size();
        }
    }
}
