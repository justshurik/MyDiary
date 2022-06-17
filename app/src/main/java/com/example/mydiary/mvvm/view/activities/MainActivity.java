package com.example.mydiary.mvvm.view.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydiary.R;
import com.example.mydiary.mvvm.model.Singleton;
import com.example.mydiary.mvvm.model.actions.Action;
import com.example.mydiary.mvvm.model.database.tables_action_settings.ActionSettings;
import com.example.mydiary.mvvm.model.time_and_dates.CurrentTime;
import com.example.mydiary.mvvm.model.time_and_dates.TimeAndDateRepository;
import com.example.mydiary.mvvm.view.adapters.ActionsRecyclerView;
import com.example.mydiary.mvvm.view.adapters.IPopupMenuActions;
import com.example.mydiary.mvvm.view.dialogs.DialogAddActionSetting;
import com.example.mydiary.mvvm.viewmodels.ViewModelChangeActionSettings;
import com.example.mydiary.mvvm.viewmodels.ViewModelUserActions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IPopupMenuActions {

    Handler handler;
    ViewModelChangeActionSettings change_settings_live_data;
    ViewModelUserActions user_actions_live_data;

    ActionsRecyclerView adapter;
    private long curr_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(Looper.getMainLooper());

        //LiveData ддля случая изменения настроек в БД
        this.change_settings_live_data = new ViewModelProvider(this).get(ViewModelChangeActionSettings.class);

        //LiveData для получения событий
        this.user_actions_live_data = new ViewModelProvider(this).get(ViewModelUserActions.class);

        getActionsFromViewMode();

        ((ImageView) findViewById(R.id.btn_settings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        //показываем текущее время на экране
        showCurrentTime(findViewById(R.id.tv_current_time));

        //recyclerView
        RecyclerView actions_recycler_view = findViewById(R.id.actions_recycler_view);
        actions_recycler_view.setLayoutManager(new LinearLayoutManager(this));

        //вызов диалога на экране
        ((FloatingActionButton) findViewById(R.id.add_action)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogAddActionSetting().show(getSupportFragmentManager(),DialogAddActionSetting.TAG_DIALOG);
            }
        });

        //вызывается, когда пользователь ввел новую настройку по событию
        this.change_settings_live_data.getChange_user_settings_live_data().observe(this, new Observer<ViewModelChangeActionSettings.UserActionSettings>() {
            @Override
            public void onChanged(ViewModelChangeActionSettings.UserActionSettings userActionSettings) {
                 //вызываем LiveData с сгенерированными данными о пользовательских событиях
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //генерируем все события из настроек UserActionSettings и вызываем LiveData с этими настройками
                        user_actions_live_data.getUserActions();

                    }
                });


            }
        });

        this.user_actions_live_data.getUser_action_live_data().observe(this, new Observer<ArrayList<Action>>() {
            @Override
            public void onChanged(ArrayList<Action> actions) {

                if(adapter==null) {
                    adapter = new ActionsRecyclerView(getApplicationContext(), actions);
                    adapter.setViewModelUserActions(user_actions_live_data);
                    actions_recycler_view.setAdapter(adapter);
                    adapter.setListener(MainActivity.this);

                }else{

                    adapter.setActions(actions);
                }


            }
        });

    }

    @Override
    public void onBackPressed() {
        if (curr_time + 2000 > System.currentTimeMillis()) {
            System.exit(0);
        }else{
            Toast.makeText(getBaseContext(),"Нажмите еще раз для выхода.",Toast.LENGTH_SHORT).show();
        }
        curr_time=System.currentTimeMillis();
    }

    //получаем данные о событиях и записываем во ViewModel
    private void getActionsFromViewMode(){

        handler.post(new Runnable() {
            @Override
            public void run() {

                user_actions_live_data.getUserActions();
            }
        });

    }


    //функция просто выводит текущее время в заданный TextView
    private void showCurrentTime(TextView tvCurrentTime){
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvCurrentTime.setText(CurrentTime.getStringCurrentTimeAndDate());
                        handler.postDelayed(this,1000);
                    }
                },0);
            }
        }).start();
    }

    //работа с popup функциями

    //удаляем событие
    @Override
    public void deleteActionSettingByAction(Action action) {
        user_actions_live_data.deleteActionSettings(action);
    }

    //изменяем событие
    @Override
    public void changeActionSettingByAction(Action action) {

        ActionSettings actionSettings = user_actions_live_data.getActionSettingByAction(action);

        //удаляем из БД обьект с данным actonSettings
        user_actions_live_data.delete_some_action_setting(actionSettings);

        //вывызвем диалог
        DialogAddActionSetting dialog = new DialogAddActionSetting();
        dialog.setAction_settings(actionSettings);
        dialog.show(getSupportFragmentManager(),DialogAddActionSetting.TAG_DIALOG);

    }
}