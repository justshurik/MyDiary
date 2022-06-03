package com.example.mydiary.mvvm.model.database.table_canceled_actions;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * сущность Room. событие, которое пользователь уже отменил
 */
@Entity(tableName = "cancelled_actions")
public class CancelledAction {
    @PrimaryKey(autoGenerate = true) long id;

    @ColumnInfo(name = "name_action") public String sNameAction;
    @ColumnInfo(name = "info_action") public String sInfoAction;
    @ColumnInfo(name = "status_action") public int iStatusAction;
    @ColumnInfo(name = "unix_action") public long unixActionTime;


}
