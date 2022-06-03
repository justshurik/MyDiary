package com.example.mydiary.mvvm.model.database.table_canceled_actions;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.mydiary.mvvm.model.database.tables_action_settings.DBActionSettings;

@Database(entities = {CancelledAction.class}, version = 1, exportSchema = false)
public abstract class DBCalcelledActions extends RoomDatabase {

    public static String DATABASE_NAME= "cancelled_actions_database";

    public abstract IDAO_canceled_actions IDAO_canceled_actions();
}
