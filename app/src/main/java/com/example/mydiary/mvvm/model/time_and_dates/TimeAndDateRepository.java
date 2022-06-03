package com.example.mydiary.mvvm.model.time_and_dates;

import androidx.annotation.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * класс для работы с датой и временем
 */
public abstract class TimeAndDateRepository {

    public final static String TAG_YEAR="yyyy";
    public final static String TAG_MONTH="MM";
    public final static String TAG_DAY="dd";
    public final static String TAG_HOUR="hh";
    public final static String TAG_MINUTE="mm";
    public final static String TAG_SECOND="ss";
    public final static String TAG_WEEKDAY="weekday";
    public static String[] months_name=null;    //вот тут массив подгрузится в момент загрузки приложения.
    public static String[] weekdays_name=null;  //вот тут массив подгрузится в момент загрузки приложения.

    /**
     * получаем порядковый номер дня недели из String даты в формате dd.MM.yyyy
     * 1 - для понедельника, 2 - для вторника, ..., 7 - воскресенье
     * @param sDate
     * @return
     */
    public static Integer getDayOfWeekNumber(String sDate){
        try{

            int year=Integer.valueOf(CurrentTime.sdf_year.format(CurrentTime.sdf_current_date.parse(sDate)));
            int month=Integer.valueOf(CurrentTime.sdf_month.format(CurrentTime.sdf_current_date.parse(sDate)));
            int day=Integer.valueOf(CurrentTime.sdf_day.format(CurrentTime.sdf_current_date.parse(sDate)));

            LocalDate localDate = LocalDate.of(year,month,day);
            return DayOfWeek.from(localDate).getValue();

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * конвертируем название для, заданное пользователем, в численное значение.
     * 1 - понедельник, 2 - вторник,..., 7 - воскресенье.
     * данные парсятся из string-array name=week_days
     * @param sWeekdays
     * @return
     */
    public static Integer calcNumberDaysOfWeekFromStringResourceToInt(String sWeekdays){
        int numDay=-1;
        for(int i=0;i<weekdays_name.length;i++){
            if(weekdays_name[i].contains(sWeekdays)){
                numDay=i;
                break;
            }
        }
        if(numDay==-1) return null;

        return numDay++;
    }

    /**
     * конвертируем название месяца в String, содержащий номер месяца для формата dd.MM.yyyy.
     * тюею например, для месяца мая будет получено число 05
     * @param monthName
     * @return
     */
    public static String convertMonthNameToNumber(String monthName){
        int numberMonth=-1;
        if(months_name==null) return null;
        for(int i=0;i<months_name.length;i++){
            if(months_name[i].contains(monthName)){
                numberMonth=i;
                break;
            }
        }

        if(numberMonth==-1) return null;

        numberMonth++;
        String month=String.valueOf(numberMonth);
        if(numberMonth<10) month="0"+month;

        return month;
    }

    /**
     * функция для получения даты, сдвинутой на заданное количество календарных дней от текущей в прошлое или будущее
     * @param startDate - стартовая дата в формате dd.MM.yyyy
     * @param numDaysToShift - количество дней для сдвига. меньше нуля - дата сдвигается в прошлое, больше нуля - дата сдвигается в будущее
     * @return
     */
    public  static String calcNewShiftedDate(@NonNull String startDate, int numDaysToShift){
        try{
            if(numDaysToShift==0) return startDate;
            Date date = CurrentTime.sdf_current_date.parse(startDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.DATE,numDaysToShift);

            return CurrentTime.sdf_current_date.format(calendar.getTime());

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * конвертация даты в dd.MM.yyyy и времени в формате hh:mm:ss в unix время
     * @param sDate dd.MM.yyyy
     * @param sTime hh:mm:ss
     * @return
     */
    public static Long calcUnixTime(@NonNull String sDate,@NonNull String sTime){
        try{
            return CurrentTime.sdf_time_and_date.parse(sDate+" "+sTime).getTime();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * подсчитываем количество дней между двумя датами в формате dd.MM.yyyy
     * @param earlyDate - дата слева (рання дата)
     * @param lateDate - дата справа (поздняя дата)
     * @return
     */
    public static Long calcDaysBetweenTwoDates(@NonNull String earlyDate,@NonNull String lateDate){
        try{

            Date minDate = CurrentTime.sdf_current_date.parse(earlyDate);
            Date maxDate = CurrentTime.sdf_current_date.parse(lateDate);

            return ChronoUnit.DAYS.between(minDate.toInstant(),maxDate.toInstant());

        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }



}
