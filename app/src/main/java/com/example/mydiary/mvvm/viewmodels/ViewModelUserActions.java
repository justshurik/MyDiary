package com.example.mydiary.mvvm.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mydiary.mvvm.model.Singleton;
import com.example.mydiary.mvvm.model.actions.Action;
import com.example.mydiary.mvvm.model.database.table_canceled_actions.CancelledAction;
import com.example.mydiary.mvvm.model.database.tables_action_settings.ActionSettings;

import java.util.ArrayList;

/**
 * LiveData для получения сгенерированный на основе настроек событий
 */
public class ViewModelUserActions extends ViewModel {

    private MutableLiveData<ArrayList<Action>> user_action_live_data;

    public ViewModelUserActions(){
        if(this.user_action_live_data==null) this.user_action_live_data = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<Action>> getUser_action_live_data() {
        return user_action_live_data;
    }

    public void getUserActions(){
        Action object = new Action();
        ArrayList<Action> actions_list = object.generateActions();
        user_action_live_data.setValue(actions_list);
    }

    public void deleteActionSettings(@NonNull Action action){
        //удаляем из БД
        Singleton.getInstance().
                getDb_action_settings().
                idao_action_settings().
                delete_some_action_settings(action.getsNameAction(),action.getiStatusAction(),action.getsInfoAction());
        //обновляем данные на основе
        getUserActions();

    }

    //получаем ActionSettings для отображения в диалоговом меню
    public ActionSettings getActionSettingByAction(@NonNull Action action){
        return Singleton.getInstance().
                getDb_action_settings().
                idao_action_settings().
                get_some_action_settings(action.getsNameAction(),action.getiStatusAction(),action.getsInfoAction());
    }

    //удаляем из БД ActionSettings
    public void delete_some_action_setting(@NonNull ActionSettings actionSettings){
        Singleton.getInstance().getDb_action_settings().idao_action_settings().delete_action_setting(actionSettings);
    }

    public void removeUserAction(@NonNull Action action){

        ArrayList<Action> actions_list=user_action_live_data.getValue();
        boolean isHere=false;
        for(int i=0;i<actions_list.size();i++){
            if(action.equals(actions_list.get(i))){
                actions_list.remove(i);

                CancelledAction c_act = new CancelledAction();

                c_act.iStatusAction=action.getiStatusAction();
                c_act.sInfoAction=action.getsInfoAction();
                c_act.sNameAction=action.getsNameAction();
                c_act.unixActionTime=action.getUnixActionTime();

                //вносим в БД
                Singleton.getInstance().getDb_cancelled_actions().IDAO_canceled_actions().insert_canceled_action(c_act);

                isHere = true;
                break;
            }
        }

        if(isHere==true) user_action_live_data.setValue(actions_list);

    }


}
