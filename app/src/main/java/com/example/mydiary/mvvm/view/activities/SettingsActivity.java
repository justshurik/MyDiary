package com.example.mydiary.mvvm.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mydiary.R;
import com.example.mydiary.mvvm.viewmodels.ViewModelUserSettings;

public class SettingsActivity extends AppCompatActivity {

    private ViewModelUserSettings settings_view_model;

    EditText etPastDays;
    EditText etFutureDays;
    Button btnSaveSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.settings_view_model = new ViewModelProvider(this).get(ViewModelUserSettings.class);

        etPastDays=findViewById(R.id.past_days_editText);
        etFutureDays=findViewById(R.id.future_days_editText);
        btnSaveSettings=findViewById(R.id.btn_save_settings);

        ((ImageView) findViewById(R.id.settings_back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });


        //получаем данные из ViewModel
        settings_view_model.getActualSettingsLiveData().observe(this, new Observer<ViewModelUserSettings.ActualSettings>() {
            @Override
            public void onChanged(ViewModelUserSettings.ActualSettings actualSettings) {
                etPastDays.setText(String.valueOf(actualSettings.getiPastDays()));
                etFutureDays.setText(String.valueOf(actualSettings.getiFutureDays()));
            }
        });

        //устанавливаем параметры из SharedPref
        settings_view_model.setStartSettings();

        ((Button) findViewById(R.id.btn_save_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer iPastDays = Integer.valueOf(etPastDays.getText().toString());
                Integer iFutureDays = Integer.valueOf(etFutureDays.getText().toString());

                if (iPastDays == null) {
                    Toast.makeText(getApplicationContext(), "Не задано количество дней для прошедших событий.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(iFutureDays==null){
                    Toast.makeText(getApplicationContext(), "Не задано количество дней для будущих событий.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (iPastDays < 0) {
                    Toast.makeText(getApplicationContext(), "Количество дней для прошлых событий не верное.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (iFutureDays < 0) {
                    Toast.makeText(getApplicationContext(), "Количество дней для будущих событий не верное.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //передаем данные во ViewModel
                settings_view_model.saveSettings(new ViewModelUserSettings.ActualSettings(iPastDays, iFutureDays));
                Toast.makeText(getBaseContext(),"Данные сохранены",Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}