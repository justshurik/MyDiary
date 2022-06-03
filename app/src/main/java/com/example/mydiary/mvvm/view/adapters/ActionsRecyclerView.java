package com.example.mydiary.mvvm.view.adapters;

import static android.media.CamcorderProfile.get;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mydiary.R;
import com.example.mydiary.mvvm.model.actions.Action;
import com.example.mydiary.mvvm.model.time_and_dates.CurrentTime;
import com.example.mydiary.mvvm.model.time_and_dates.TimeAndDateRepository;
import com.example.mydiary.mvvm.viewmodels.ViewModelChangeActionSettings;
import com.example.mydiary.mvvm.viewmodels.ViewModelUserActions;

import java.util.ArrayList;

public class ActionsRecyclerView extends RecyclerView.Adapter<ActionsRecyclerView.ViewHolder> {

    //идентификаторы действий в popup menu
    private final byte ID_EDIT=0;
    private final byte ID_DELETE=1;

    //слушатель
    IPopupMenuActions listener;

    private ViewModelUserActions user_actions_live_data;
    private ViewModelChangeActionSettings change_user_settings_live_data;

    private LayoutInflater mInflater;
    private ArrayList<Action> actions;
    private Context context;
    public ActionsRecyclerView(Context context, ArrayList<Action> actions){
        this.mInflater=LayoutInflater.from(context);
        this.actions=actions;
        this.context=context;
    }


    //передаем ViewModel
    public void setViewModelUserActions(ViewModelUserActions user_actions_live_data){
        this.user_actions_live_data=user_actions_live_data;
    }

    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.action_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.ivPopupMenu.setTag(actions.get(position));

        //привызяываем элементы
        String sTitleAction=actions.get(position).getsNameAction();
        String sInfoAction=actions.get(position).getsInfoAction();

        //кол-во дней до сбоытия
        Long lDaysToAction= TimeAndDateRepository.
                calcDaysBetweenTwoDates(CurrentTime.getStringCurrentDate(),actions.get(position).getsDateAction());

        String sDaysToAction=null;
        if(lDaysToAction>0) sDaysToAction="+"+lDaysToAction;
        else sDaysToAction=String.valueOf(lDaysToAction);

        String sDateAndTime=context.getResources().getString(R.string.date_action)+": "+
                actions.get(position).getsDateAction()+" "+actions.get(position).getsTimeAction()+ " ("+sDaysToAction+" дней)";


        int iStatus = actions.get(position).getiStatusAction();
        holder.tvTitle.setText(sTitleAction);
        holder.tvInfo.setText(sInfoAction);
        //дата и время события
        holder.tvDate.setText(sDateAndTime);
        //проверка на время
        long currentUnix= System.currentTimeMillis();
        Long actionUnix=actions.get(position).getUnixActionTime();

        if(actionUnix<currentUnix){
            holder.item_area.setBackgroundColor(context.getColor(R.color.past_action));
        }else {

            if (iStatus == 0) {
                holder.item_area.setBackgroundColor(context.getColor(R.color.high_status));
            } else if (iStatus == 1) {
                holder.item_area.setBackgroundColor(context.getColor(R.color.middle_status));
            } else {
                holder.item_area.setBackgroundColor(context.getColor(R.color.low_status));
            }
        }

        holder.ivPopupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupmenu(v);
            }
        });

        holder.cbUnactivated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_actions_live_data.removeUserAction(actions.get(holder.getAdapterPosition()));
            }
        });

    }

    //вызываем подменю
    private void showPopupmenu(View v){
        PopupMenu popup_menu = new PopupMenu(v.getContext(),v);
        popup_menu.getMenu().add(0,ID_EDIT, Menu.NONE,context.getResources().getStringArray(R.array.popup_menu_items)[0]);
        popup_menu.getMenu().add(0,ID_DELETE,Menu.NONE,context.getResources().getStringArray(R.array.popup_menu_items)[1]);
        Action action = (Action) v.getTag();

        popup_menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    //вызываем функции
                    case ID_EDIT:  listener.changeActionSettingByAction(action); break;

                    case ID_DELETE: listener.deleteActionSettingByAction(action); break;

                }
                return true;
            }
        });

        //показыаем всплывающее меню
        popup_menu.show();
    }




    @Override
    public int getItemCount() {
        return actions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Button cbUnactivated;
        TextView tvTitle;
        TextView tvInfo;
        ImageView ivPopupMenu;
        ConstraintLayout item_area;
        TextView tvDate;

        public ViewHolder(@NonNull View v) {
            super(v);
            cbUnactivated=v.findViewById(R.id.unactivate);
            tvTitle=v.findViewById(R.id.action_title);
            tvInfo=v.findViewById(R.id.about_action);
            ivPopupMenu=v.findViewById(R.id.popup);
            item_area=v.findViewById(R.id.item_action_area);
            tvDate=v.findViewById(R.id.date);
        }
    }
    public void setListener(IPopupMenuActions listener) {
        this.listener = listener;
    }

}
