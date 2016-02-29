package com.github.blackenwhite.nicefilms.android.scenarios.search;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.view.KeyEvent;
import android.widget.Toast;

import com.github.blackenwhite.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class FilmsByYearActivity extends AppCompatActivity {
    private final String LOG_TAG = FilmsByYearActivity.class.getSimpleName();
    private static final Integer YEAR_MIN = 1900;
    private static final Integer YEAR_MAX = new GregorianCalendar().get(Calendar.YEAR);

    private Intent intent;

    private Spinner genresSpinner;
    private Spinner ratingsSpinner;
    private EditText yearField;
    private Button searchBtn;

    private String selectedGenre;
    private int selectedYear;
    private double selectedRating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_films_by_year);

        searchBtn = (Button) findViewById(R.id.btnSearch);
        searchBtn.setOnTouchListener(new View.OnTouchListener() {
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

        genresSpinner = (Spinner) findViewById(R.id.genres_spinner);
        ArrayAdapter<CharSequence> genresAdapter = ArrayAdapter.createFromResource(this,
                R.array.genres_array, R.layout.spinner_item);
        genresAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genresSpinner.setAdapter(genresAdapter);
        genresSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGenre = parent.getItemAtPosition(position).toString().toLowerCase();
                if (selectedGenre.contains("any")) {
                    selectedGenre = "any";
                }
                Log.d(LOG_TAG, "GENRE=" + selectedGenre);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ratingsSpinner = (Spinner) findViewById(R.id.ratings_spinner);
        ArrayAdapter<CharSequence> ratingsAdapter = ArrayAdapter.createFromResource(this,
                R.array.ratings_array, R.layout.spinner_item);
        ratingsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ratingsSpinner.setAdapter(ratingsAdapter);
        ratingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRating = Double.parseDouble(parent.getItemAtPosition(position).toString());
                Log.d(LOG_TAG, "RATING=" + selectedRating);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ratingsSpinner.setSelection(ratingsAdapter.getPosition("8.0"));

        yearField = (EditText) findViewById(R.id.year_field);
        yearField.setFilters(new InputFilter[]{new InputFilterYears(0, YEAR_MAX)});
        yearField.setText(String.format("%d", 2008));

        yearField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    InputMethodManager imm = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(yearField.getWindowToken(), 0);
                    if (isCorrectYear(v.getText())) {
                        selectedYear = Integer.parseInt(v.getText().toString());
                        Log.d(LOG_TAG, "YEAR=" + selectedYear);
                        searchBtn.setEnabled(true);
                        searchBtn.setBackgroundColor(Color.parseColor("#2B75AD"));
                    } else {
                        searchBtn.setEnabled(false);
                        searchBtn.setBackgroundColor(Color.parseColor("#ADABAA"));
                        Toast.makeText(getBaseContext(),
                                "Year: " + YEAR_MIN + " ... " + YEAR_MAX,
                                Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }

            private boolean isCorrectYear(CharSequence year) {
                int yearInt = Integer.parseInt(year.toString());
                return yearInt >= YEAR_MIN && yearInt <= YEAR_MAX;
            }
        });
    }

    public void search(View view) {
        selectedYear = Integer.parseInt(yearField.getText().toString());
        intent = new Intent(this, FilmsByYearResultsActivity.class);
        intent.putExtra("genre", selectedGenre);
        intent.putExtra("year", selectedYear);
        intent.putExtra("rating", selectedRating);
        startActivity(intent);
    }
}
