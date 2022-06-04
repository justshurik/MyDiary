package com.example.mydiary.mvvm.model;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.example.mydiary.R;

import com.example.mydiary.mvvm.model.database.table_canceled_actions.DBCalcelledActions;
import com.example.mydiary.mvvm.model.database.tables_action_settings.DBActionSettings;
import com.example.mydiary.mvvm.model.sh_pr.UserSettings;
import com.example.mydiary.mvvm.model.time_and_dates.TimeAndDateRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

public class Singleton extends Application{

    private Context context;
    private static Singleton INSTANCE;
    private UserSettings user_settings;
    private DBActionSettings db_action_settings;
    private DBCalcelledActions db_cancelled_actions;

    public static Singleton getInstance(){
        if(INSTANCE==null){
            INSTANCE = new Singleton();
        }
        return INSTANCE;
    }

    public Singleton(){}

    @Override
    public void onCreate() {
        super.onCreate();

        if(context==null) context=this;
        if(INSTANCE==null) INSTANCE=this;

        //настройки пользовательские
        this.user_settings = new UserSettings();

        //база данных с настройками для генерации событий
        this.db_action_settings = Room.
                databaseBuilder(context,DBActionSettings.class,DBActionSettings.DATABASE_NAME).
                allowMainThreadQueries().   //разрешил доступ в БД из главного потока, потому что иначе я хз как реализовать добавление новой настройки и последующее сразу обновление RecyclerView
                build();

        //база данных для хранения событий, которые пользователь отменил
        this.db_cancelled_actions=Room.
                databaseBuilder(context, DBCalcelledActions.class,DBCalcelledActions.DATABASE_NAME).
                allowMainThreadQueries().
                build();

        //устанавливаем ресурсы для класса TimeAndDateRepository для работы с времени
        TimeAndDateRepository.months_name=getContext().getResources().getStringArray(R.array.month_names);
        TimeAndDateRepository.weekdays_name=getContext().getResources().getStringArray(R.array.week_days);
    }

    public Context getContext() {
        return context;
    }

    public UserSettings getUser_settings() {
        return user_settings;
    }

    public synchronized DBActionSettings getDb_action_settings() {
        return db_action_settings;
    }

    public synchronized DBCalcelledActions getDb_cancelled_actions() {
        return db_cancelled_actions;
    }
}
