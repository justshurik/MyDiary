package com.example.mydiary.mvvm.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydiary.mvvm.model.Singleton;
import com.example.mydiary.mvvm.model.sh_pr.UserSettings;

public class ViewModelUserSettings extends ViewModel {

    private MutableLiveData<ActualSettings> user_settings_live_data ;

    public ViewModelUserSettings() {

        if(this.user_settings_live_data==null) this.user_settings_live_data = new MutableLiveData<>();

    }

    public void setStartSettings(){
        ActualSettings actual_settings = new ActualSettings(
                Singleton.getInstance().getUser_settings().getPast_days(),
                Singleton.getInstance().getUser_settings().getFuture_days()
        );
        saveSettings(actual_settings);
    }

    public MutableLiveData<ActualSettings> getActualSettingsLiveData(){
        return this.user_settings_live_data;
    }

    /**
     * устанавливаем настройки в ShPr
     * @param settings
     */
    public void saveSettings(ActualSettings settings){
        user_settings_live_data.setValue(settings);
        //записываем данные в ShPr
        Singleton.getInstance().getUser_settings().setFutureDays(settings.getiFutureDays());
        Singleton.getInstance().getUser_settings().setPastDays(settings.getiPastDays());
        //корректируем удаленные события

    }

    //получаем настройки из ShPr
    public ActualSettings takeSettings(){
        return new ActualSettings(Singleton.getInstance().getUser_settings());
    }


    //подкласс-контейнер
    public static class ActualSettings{

        private int iPastDays;
        private int iFutureDays;
        public ActualSettings(int iPastDays,int iFutureDays){
            this.iPastDays=iPastDays;
            this.iFutureDays=iFutureDays;
        }

        public ActualSettings(UserSettings settings){
            this.iPastDays=settings.getPast_days();
            this.iFutureDays= settings.getFuture_days();
        }

        public int getiPastDays() {
            return iPastDays;
        }

        public int getiFutureDays() {
            return iFutureDays;
        }

    }


}
