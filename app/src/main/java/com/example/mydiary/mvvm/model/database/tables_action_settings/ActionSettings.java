package com.example.mydiary.mvvm.model.database.tables_action_settings;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


/**
 * сущность Room
 */
@Entity(tableName = "actions_settings")
public class ActionSettings {

    @PrimaryKey(autoGenerate = true) long id;

    @ColumnInfo(name = "title") public String sTitle;
    @ColumnInfo(name = "info") public String sInfo;
    @ColumnInfo(name = "status") public int iStatus;
    @ColumnInfo(name = "frequency") public String sFreq;



}
