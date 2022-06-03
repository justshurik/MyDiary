package com.example.mydiary.mvvm.model.time_and_dates;

import java.text.SimpleDateFormat;

/**
 * класс для получения текущего времени и даты и по отделности каждую
 */
public abstract class CurrentTime {


    public final static SimpleDateFormat sdf_current_time_and_date = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
    public final static SimpleDateFormat sdf_time_and_date = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public final static SimpleDateFormat sdf_current_date=new SimpleDateFormat("dd.MM.yyyy");
    public final static SimpleDateFormat sdf_current_time=new SimpleDateFormat("HH:mm:ss");

    public final static SimpleDateFormat sdf_day= new SimpleDateFormat("dd");
    public final static SimpleDateFormat sdf_month= new SimpleDateFormat("MM");
    public final static SimpleDateFormat sdf_year= new SimpleDateFormat("yyyy");

    public final static SimpleDateFormat sdf_hour= new SimpleDateFormat("HH");
    public final static SimpleDateFormat sdf_minute= new SimpleDateFormat("mm");
    public final static SimpleDateFormat sdf_second= new SimpleDateFormat("ss");

    public final static SimpleDateFormat sdf_number_days_od_week= new SimpleDateFormat("u");

    public static String getStringCurrentDayOfWeek(){
        return sdf_number_days_od_week.format(System.currentTimeMillis());
    }

    public static String getStringCurrentSecond(){
        return sdf_second.format(System.currentTimeMillis());
    }

    public static String getStringCurrentMinute(){
        return sdf_minute.format(System.currentTimeMillis());
    }

    public static String getStringCurrentHour(){
        return sdf_hour.format(System.currentTimeMillis());
    }

    public static String getStringCurrentYear(){
        return sdf_year.format(System.currentTimeMillis());
    }

    public static String getStringCurrentMonthOfYear(){
        return sdf_month.format(System.currentTimeMillis());
    }

    public static String getStringCurrentDayOfMonth(){
        return sdf_day.format(System.currentTimeMillis());
    }

    public static String getStringCurrentTimeAndDate(){
        return sdf_current_time_and_date.format(System.currentTimeMillis());
    }

    public static String getStringCurrentDate(){
        return sdf_current_date.format(System.currentTimeMillis());
    }

    public static String getStringCurrentTime(){
        return sdf_current_time.format(System.currentTimeMillis());
    }


}
