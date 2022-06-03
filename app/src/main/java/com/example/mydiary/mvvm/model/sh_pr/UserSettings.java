package com.example.mydiary.mvvm.model.sh_pr;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.mydiary.mvvm.model.Singleton;

/**
 * класс для управления настройками пользователя
 */
public class UserSettings {
    private int DEFAULT_PAST_DAYS=3;
    private int DEFAULT_FUTURE_DAYS=10;

    private Integer past_days;    //кол-во дней по-умолчанию в прошлое
    private Integer future_days; //кол-во дней по-умолчанию в будущее

    private String KEY_PAST_DAYS="key_past_days";
    private String KEY_FUTURE_DAYS="key_future_days";

    private String USER_SETTINGS="user_settings";
    private SharedPreferences sh_pr_settings;

    public UserSettings(){

        this.sh_pr_settings = Singleton.getInstance().getContext().getSharedPreferences(USER_SETTINGS, Context.MODE_PRIVATE);
        this.past_days = sh_pr_settings.getInt(KEY_PAST_DAYS,DEFAULT_PAST_DAYS);
        this.future_days=sh_pr_settings.getInt(KEY_FUTURE_DAYS,DEFAULT_FUTURE_DAYS);

    }

    /**
     * записываем дни для отображения прошлых событий
     * @param iPastDays
     */
    public void setPastDays(@NonNull Integer iPastDays){
        try{
            if(sh_pr_settings==null) throw new Exception("Не найдено SharedPreferences.");

            SharedPreferences.Editor editor = sh_pr_settings.edit();
            editor.putInt(KEY_PAST_DAYS,iPastDays);
            editor.commit();

            this.past_days=iPastDays;

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * записываем дни для отображения будущих событий
     * @param iFutureDays
     */
    public void setFutureDays(@NonNull Integer iFutureDays){
        try{
            if(sh_pr_settings==null) throw new Exception("Не найдено SharedPreferences.");

            SharedPreferences.Editor editor = sh_pr_settings.edit();
            editor.putInt(KEY_FUTURE_DAYS,iFutureDays);
            editor.commit();

            this.future_days=iFutureDays;

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public int getPast_days() {
        if(this.past_days==null) this.past_days=DEFAULT_PAST_DAYS;
        return this.past_days;
    }

    public int getFuture_days() {
        if(this.future_days==null) this.future_days=DEFAULT_FUTURE_DAYS;
        return this.future_days;
    }
}
