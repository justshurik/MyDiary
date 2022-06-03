package com.example.mydiary.mvvm.viewmodels;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydiary.mvvm.model.Singleton;
import com.example.mydiary.mvvm.model.actions.Action;
import com.example.mydiary.mvvm.model.database.tables_action_settings.ActionSettings;
import com.example.mydiary.mvvm.model.time_and_dates.TimeAndDateRepository;

import java.util.ArrayList;
import java.util.concurrent.Executor;


public class ViewModelChangeActionSettings extends ViewModel {

    //списки
    public String[] frequency;
    public String[] statuses;
    public String[] weekdays;
    public String[] months;

    private MutableLiveData<UserActionSettings> change_user_settings_live_data;

    public ViewModelChangeActionSettings(){
        if(this.change_user_settings_live_data==null) change_user_settings_live_data=new MutableLiveData<>();
    }

    public MutableLiveData<UserActionSettings> getChange_user_settings_live_data() {
        return change_user_settings_live_data;
    }


    public void addActionSetting(UserActionSettings actionSettings) throws Exception {
        try {

            //формируем Entities для записи в БД
            ActionSettings entitie = convertUserActionSettingsToActionSettings(actionSettings, statuses, frequency);
            if (entitie == null)
                throw new Exception("Не удалось получить ActionSettings для внесения в БД.");

            //вносим в БД запись (изначально поставлен режим внесения записей в основном потоке, чтобы одновременно с внесением записи было обновление актуальных событий)
            Singleton.getInstance().getDb_action_settings().idao_action_settings().insert_action_setting(entitie);

            change_user_settings_live_data.setValue(actionSettings);    //вот тут срабатвает LiveData в MainActivity и начинается построение RecyclerView

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //конвертив пользовательские данные в ActionSettings, который можно записать в БД
    private ActionSettings convertUserActionSettingsToActionSettings(UserActionSettings sett, @NonNull String[] statuses, @NonNull String[] sFreq){
        try {
            ActionSettings actionSettings = new ActionSettings();
            actionSettings.sTitle = sett.sNameAction;
            actionSettings.sInfo = sett.sInfoAction;
            String sStatus = sett.sStatus;
            sett.sMonth = TimeAndDateRepository.convertMonthNameToNumber(sett.sMonth);
            //статус
            int iStatus;
            if (sStatus.equals(statuses[0])) iStatus = 0;     //высокий статус
            else if (sStatus.equals(statuses[1])) iStatus = 1; //средний статус
            else iStatus = 2; //низкий статус

            //формируем поле для частоты
            String freq = sett.sFreq;

            if (freq.equals(sFreq[0])) {
                //ежегодная частота
                freq += "_date:"+
                        TimeAndDateRepository.TAG_DAY +"=" + sett.sDay + ";"+
                        TimeAndDateRepository.TAG_MONTH+"=" + sett.sMonth +";"+
                        "_time:"+
                        TimeAndDateRepository.TAG_HOUR + "=" + sett.sHour + ";"+
                        TimeAndDateRepository.TAG_MINUTE + "=" + sett.sMinute + ";"+
                        TimeAndDateRepository.TAG_SECOND + "=" + sett.sSeconds+";";

            } else if (freq.equals(sFreq[1])) {
                //ежемесячная частота
                freq += "_date:"+
                        TimeAndDateRepository.TAG_DAY+"=" + sett.sDay + ";" +
                        "_time:"+
                        TimeAndDateRepository.TAG_HOUR + "=" + sett.sHour + ";"+
                        TimeAndDateRepository.TAG_MINUTE + "=" + sett.sMinute + ";"+
                        TimeAndDateRepository.TAG_SECOND + "=" + sett.sSeconds+";";

            } else if (freq.equals(sFreq[2])) {
                //еженедельная частота
                freq += "_"+TimeAndDateRepository.TAG_WEEKDAY+"=" + sett.sWeekday+";"+
                        "_time:"+
                        TimeAndDateRepository.TAG_HOUR + "=" + sett.sHour + ";"+
                        TimeAndDateRepository.TAG_MINUTE + "=" + sett.sMinute + ";"+
                        TimeAndDateRepository.TAG_SECOND + "=" + sett.sSeconds+";";

            } else if (freq.equals(sFreq[3])) {
                //ежедневно
                freq += "_time:"+
                        TimeAndDateRepository.TAG_HOUR + "=" + sett.sHour + ";"+
                        TimeAndDateRepository.TAG_MINUTE + "=" + sett.sMinute + ";"+
                        TimeAndDateRepository.TAG_SECOND + "=" + sett.sSeconds+";";

            } else if (freq.equals(sFreq[4])){
                //единоразовое событие
                freq += "_date:"+
                        TimeAndDateRepository.TAG_DAY +"=" + sett.sDay + ";"+
                        TimeAndDateRepository.TAG_MONTH +"=" + sett.sMonth +";"+
                        TimeAndDateRepository.TAG_YEAR + "=" + sett.sMonth +";"+
                        "_time:"+
                        TimeAndDateRepository.TAG_HOUR + "=" + sett.sHour + ";"+
                        TimeAndDateRepository.TAG_MINUTE + "=" + sett.sMinute + ";"+
                        TimeAndDateRepository.TAG_SECOND + "=" + sett.sSeconds+";";

            }else{
                throw new Exception("Не удалось найти частоту событий в пользовательский настройках UserActionSettings.");
            }

            actionSettings.sFreq=freq;
            actionSettings.iStatus=iStatus;

            return actionSettings;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }



    }


    //класс-контейнер для передачи во ViewModel
    public static class UserActionSettings{

        public String sNameAction;
        public String sInfoAction;
        public String sStatus;
        public String sFreq;
        public String sDay;
        public String sMonth;
        public String sYear;
        public String sWeekday;
        public String sHour;
        public String sMinute;
        public String sSeconds;
        public UserActionSettings(){}

    }

}
