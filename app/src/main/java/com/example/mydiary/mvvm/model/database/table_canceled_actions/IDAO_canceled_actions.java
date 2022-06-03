package com.example.mydiary.mvvm.model.database.table_canceled_actions;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.mydiary.mvvm.model.database.tables_action_settings.ActionSettings;

import java.util.List;

@Dao
public interface IDAO_canceled_actions {

    @Insert
    public void insert_canceled_action(CancelledAction cancelledAction);

    @Query("SELECT * FROM cancelled_actions")
    List<CancelledAction> getAllCancelledActions();

    @Query("DELETE FROM cancelled_actions WHERE unix_action < :minUNIX AND unix_action > :maxUNIX")
    void deleteUnusableActions(@NonNull Long minUNIX, @NonNull Long maxUNIX);


}
