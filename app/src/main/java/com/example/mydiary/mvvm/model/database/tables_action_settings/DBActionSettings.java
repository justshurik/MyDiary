package com.example.mydiary.mvvm.model.database.tables_action_settings;


import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * базы данных
 */

@Database(entities = {ActionSettings.class}, version = 1, exportSchema = false)
public abstract class DBActionSettings extends RoomDatabase {

    public static String DATABASE_NAME="my_diary_database";

    public abstract idao_action_settings idao_action_settings();

}
