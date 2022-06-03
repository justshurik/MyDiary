package com.example.mydiary.mvvm.model.database.tables_action_settings;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mydiary.mvvm.model.database.table_canceled_actions.CancelledAction;

import java.util.List;

@Dao
public interface idao_action_settings {

    @Insert
    public void insert_action_setting(ActionSettings actions_setting);

    @Update
    public void update_action_setting(ActionSettings actionSettings);

    @Update void update_action_settings(List<ActionSettings> list_actions);

    @Query("SELECT * FROM actions_settings") List<ActionSettings> getAllActions();

    @Query("DELETE FROM actions_settings WHERE title = :title_action AND status = :status_action AND info = :info_action")
    void delete_some_action_settings(String title_action, int status_action, String info_action);

    @Query("SELECT * FROM actions_settings WHERE title = :title_action AND status = :status_action AND info = :info_action")
    ActionSettings get_some_action_settings(String title_action, int status_action, String info_action);

    @Delete void delete_action_setting(ActionSettings actionSettings);
}
