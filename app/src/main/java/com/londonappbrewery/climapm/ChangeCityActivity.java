package com.londonappbrewery.climapm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        setContentView( R.layout.change_city_layout );
        final EditText edittextfield = findViewById( R.id.queryET );
        ImageButton backButton = findViewById( R.id.backButton );

        backButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        } );

        edittextfield.setOnEditorActionListener( new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                String newCity = edittextfield.getText().toString();
                Intent newCityIntent = new Intent(ChangeCityActivity.this, WeatherController.class);
                newCityIntent.putExtra( "City", newCity );
                startActivity( newCityIntent );

                return false;
            }
        } );

    }
}
