package info.nice_films.androidclient;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class CategoriesActivity extends AppCompatActivity implements View.OnTouchListener {

    private Button btnFilms, btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        btnFilms = (Button) findViewById(R.id.btnFilmsByYear);
        btnAbout = (Button) findViewById(R.id.btnAbout);

        btnFilms.setOnTouchListener(this);
        btnAbout.setOnTouchListener(this);
    }

    public void goToFilmsByYear(View view) {
        Intent intent = new Intent(this, FilmsByYearActivity.class);
        startActivity(intent);
    }

    public void goToAbout(View view) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == btnFilms || v == btnAbout) {
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
        }
        return false;
    }
}
