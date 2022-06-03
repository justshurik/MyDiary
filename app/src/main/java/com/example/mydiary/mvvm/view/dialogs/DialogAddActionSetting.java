package com.example.mydiary.mvvm.view.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mydiary.R;
import com.example.mydiary.mvvm.model.actions.Action;
import com.example.mydiary.mvvm.model.database.tables_action_settings.ActionSettings;
import com.example.mydiary.mvvm.model.time_and_dates.CurrentTime;
import com.example.mydiary.mvvm.model.time_and_dates.TimeAndDateRepository;
import com.example.mydiary.mvvm.viewmodels.ViewModelChangeActionSettings;

public class DialogAddActionSetting extends DialogFragment {

    //LiveData
    private ViewModelChangeActionSettings live_data;
    private ActionSettings action_settings;

    public static String TAG_DIALOG=DialogAddActionSetting.class.toString();

    EditText etNameAction;
    EditText etInfoAction;
    Spinner spStatus;
    Spinner spFreq;

    LinearLayout layoutBlockDate;
    EditText etDay;
    Spinner spMonths;
    EditText etYear;

    LinearLayout layoutBlockWeekDays;
    Spinner spWeekdays;

    LinearLayout layoutBlockTime;
    EditText etHours;
    EditText etMinutes;
    EditText etSeconds;

    Button btnAddActionSetting;


    public String[] spinner_items_status;
    public String[] spinner_items_months;
    public String[] spinner_items_freq;
    public String[] spinner_items_weekdays;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //берем LiveData
        this.live_data = new ViewModelProvider(requireActivity()).get(ViewModelChangeActionSettings.class);

        View v = inflater.inflate(R.layout.dialog_add_settings,null);

        etNameAction = v.findViewById(R.id.dialog_name_action_setting);
        etInfoAction=v.findViewById(R.id.dialog_info_action_setting);
        spStatus=v.findViewById(R.id.status);
        spFreq=v.findViewById(R.id.frequency);

        layoutBlockDate=v.findViewById(R.id.block_dates);
        etDay=v.findViewById(R.id.ed_date);
        spMonths=v.findViewById(R.id.months);
        etYear=v.findViewById(R.id.ed_year);

        layoutBlockWeekDays=v.findViewById(R.id.block_week_days);
        spWeekdays=v.findViewById(R.id.weekdays);

        layoutBlockTime=v.findViewById(R.id.block_time);
        etHours=v.findViewById(R.id.ed_hours);
        etMinutes=v.findViewById(R.id.ed_minutes);
        etSeconds=v.findViewById(R.id.ed_seconds);

        btnAddActionSetting=v.findViewById(R.id.add_action_to_database);

        //строковые списки
        spinner_items_status=getResources().getStringArray(R.array.status_items);
        spinner_items_months=getResources().getStringArray(R.array.month_names);
        spinner_items_freq=getResources().getStringArray(R.array.freq_items);
        spinner_items_weekdays=getResources().getStringArray(R.array.week_days);

        live_data.frequency=spinner_items_freq; //передадим частоту событий в LiveData (это поле в LiveData нам нужно будет постоянно)
        live_data.statuses=spinner_items_status;    //передадим статусы в LiveData (это поле в LiveData нам нужно будет постоянно)
        live_data.months=spinner_items_months;  //передадим месяцы в LiveData (это поле в LiveData нам нужно будет постоянно)
        live_data.weekdays=spinner_items_weekdays;  //передадим названия дней в LiveData (это поле в LiveData нам нужно будет постоянно)

        //задаем значения спискам
        spStatus.setAdapter(new ArrayAdapter<String>(v.getContext(),R.layout.spinner_item,spinner_items_status));
        spFreq.setAdapter(new ArrayAdapter<String>(v.getContext(),R.layout.spinner_item,spinner_items_freq));
        spMonths.setAdapter(new ArrayAdapter<String>(v.getContext(),R.layout.spinner_item,spinner_items_months));
        spWeekdays.setAdapter(new ArrayAdapter<String>(v.getContext(),R.layout.spinner_item,spinner_items_weekdays));

        //устанавливаем стартовую видимость элементам
        setVisibleElements(spFreq.getSelectedItem().toString());

        //стартовые значения для упрощения заполнения пользователем
        setStartValues();

        //обработка выбора частоты событий
        spFreq.setOnItemSelectedListener(freq_listener);

        //установка обработчика событий нажатия на кнопку
        btnAddActionSetting.setOnClickListener(addActionListener);
        if(action_settings!=null){
            //парсим данные для случая, когда были заданые какие-то настройки для редактирования
            parceActionSettings();
        }
        return v;
    }

    //обработка события по нажатию на кнопку Добавить событие
    View.OnClickListener addActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //проверка основных задаваемых пользователем данных о событии - название и описание
            if(!checkGeneralDataAction(v.getContext())){
                return;
            }

            //обработка события
            int selected_item_freq = spFreq.getSelectedItemPosition();
            switch (selected_item_freq){
                case 0:
                    //ежегодная частота события
                    if(!checkYearData(v.getContext())) return;  //проверка исходных данных, введенных пользователем

                    break;
                case 1:
                    //ежемесячная частота
                    if(!checkMonthData(v.getContext())) return;  //проверка исходных данных, введенных пользователем

                    break;
                case 2:
                    //еженедельная частота событий
                    if(!checkWeeklyData(v.getContext())) return;  //проверка исходных данных, введенных пользователем

                    break;
                case 3:
                    //ежедневная частота событий
                    if(!checkEverydayData(v.getContext())) return;  //проверка исходных данных, введенных пользователем

                    break;
                case 4:
                    //разовое событие
                    if(!checkOnceActionData(v.getContext())) return;  //проверка исходных данных, введенных пользователем

                    break;

                default: break;
            }

            //обьект для передчи во ViewModel
            ViewModelChangeActionSettings.UserActionSettings userActionSettings = new ViewModelChangeActionSettings.UserActionSettings();
            userActionSettings.sDay=etDay.getText().toString();
            userActionSettings.sMonth=spinner_items_months[spMonths.getSelectedItemPosition()];
            userActionSettings.sYear=etYear.getText().toString();
            userActionSettings.sWeekday=spinner_items_weekdays[spWeekdays.getSelectedItemPosition()];
            userActionSettings.sHour=etHours.getText().toString();
            userActionSettings.sMinute = etMinutes.getText().toString();
            userActionSettings.sSeconds=etSeconds.getText().toString();
            userActionSettings.sFreq=spinner_items_freq[spFreq.getSelectedItemPosition()];
            userActionSettings.sStatus=spinner_items_status[spStatus.getSelectedItemPosition()];
            userActionSettings.sInfoAction=etInfoAction.getText().toString();
            userActionSettings.sNameAction=etNameAction.getText().toString();

            //передаем данные в LiveModel. Дальше они будут обрабатываться в LiveData и затем в MainActivity
            try {
                live_data.addActionSetting(userActionSettings);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //закрываем диалог
            dismiss();

        }
    };


    //проверка ввода разовых событий
    private boolean checkOnceActionData(Context context){
        try{
            String sDate=etDay.getText().toString();
            if(sDate.length()==0) {
                Toast.makeText(context,"Не задана дата.",Toast.LENGTH_SHORT).show();
                return false;
            }
            Integer iDate=Integer.valueOf(sDate);
            if(iDate<=0 || iDate>31){
                Toast.makeText(context,"Не верно задана дата",Toast.LENGTH_SHORT).show();
                return false;
            }
            String sYear=etYear.getText().toString();
            if(sYear.length()==0) {
                Toast.makeText(context,"Не задан год.",Toast.LENGTH_SHORT).show();
                return false;
            }
            Integer iYear=Integer.valueOf(sYear);
            if(iYear<2011){
                Toast.makeText(context,"Год не может быть раньше 2011.",Toast.LENGTH_SHORT).show();
                return false;
            }

            int iMonth=spMonths.getSelectedItemPosition();

            //проверяем дату, которую ввел пользователь
            if(!checkDateInMonth(iDate,iMonth,context)) return false;
            //проверяем введенное время
            if(!checkInputTime(context)) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //проверка ввода ежедневных событий
    private boolean checkEverydayData(Context context){
        try{

            if(!checkInputTime(context)) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //проверка ввода еженедельных событий
    private boolean checkWeeklyData(Context context){
        try{

            if(!checkInputTime(context)) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



    //проверка настроек ежемесячного события
    private boolean checkMonthData(Context context){
        try{

            String sDate=etDay.getText().toString();
            if(sDate.length()==0) {
                Toast.makeText(context,"Не задана дата.",Toast.LENGTH_SHORT).show();
                return false;
            }
            Integer iDate=Integer.valueOf(sDate);
            if(iDate<=0 || iDate>31){
                Toast.makeText(context,"Не верно задана дата",Toast.LENGTH_SHORT).show();
                return false;
            }
            if(!checkInputTime(context)) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //проверка входных данных для случая ежегодных событий
    private boolean checkYearData(Context context){
        try{

            String sDate=etDay.getText().toString();
            if(sDate.length()==0) {
                Toast.makeText(context,"Не задана дата.",Toast.LENGTH_SHORT).show();
                return false;
            }
            Integer iDate=Integer.valueOf(sDate);
            if(iDate<=0 || iDate>31){
                Toast.makeText(context,"Не верно задана дата",Toast.LENGTH_SHORT).show();
                return false;
            }

            int iMonth=spMonths.getSelectedItemPosition();

            //проверяем дату, которую ввел пользователь
            if(!checkDateInMonth(iDate,iMonth,context)) return false;
            //проверяем введенное время
            if(!checkInputTime(context)) return false;

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //проверка введенного времени пользователем
    private boolean checkInputTime(Context context){
        //проверяем поле часов
        String sHour = etHours.getText().toString();
        if(sHour.length()==0){
            Toast.makeText(context,"Не заданы часы.",Toast.LENGTH_SHORT).show();
            return false;
        }
        Integer iHour=Integer.valueOf(sHour);
        if(iHour<0 || iHour>24){
            Toast.makeText(context,"Не верно заданы часы.",Toast.LENGTH_SHORT).show();
            return false;
        }

        //поле минут
        String sMinute = etMinutes.getText().toString();
        if(sMinute.length()==0){
            Toast.makeText(context,"Не заданы минуты.",Toast.LENGTH_SHORT).show();
            return false;
        }
        Integer iMinute=Integer.valueOf(sMinute);
        if(iMinute<0 || iMinute>60){
            Toast.makeText(context,"Не верно заданы минуты.",Toast.LENGTH_SHORT).show();
            return false;
        }

        //поле секунд
        String sSecond = etSeconds.getText().toString();
        if(sSecond.length()==0){
            Toast.makeText(context,"Не заданы секунды. По умолчанию будет установлено значение 00.",Toast.LENGTH_SHORT).show();
            sSecond="00";
            etSeconds.setText(sSecond);

        }
        Integer iSeconds=Integer.valueOf(sSecond);
        if(iSeconds<0 || iSeconds>60){
            Toast.makeText(context,"Не верно заданы секунды.",Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    //проверка даты в месяце
    private boolean checkDateInMonth(int iDayInMonth, int iMonth, Context context){
        switch (iMonth) {
            case 0:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В январе дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 1:
                if (iDayInMonth > 29) {
                    Toast.makeText(context, "В феврале дата не может быть больше 29.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 2:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В марте дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 3:
                if (iDayInMonth > 30) {
                    Toast.makeText(context, "В апреле дата не может быть больше 30.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 4:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В мае дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 5:
                if (iDayInMonth > 30) {
                    Toast.makeText(context, "В июне дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 6:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В июле дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 7:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В августе дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 8:
                if (iDayInMonth > 30) {
                    Toast.makeText(context, "В сентябре дата не может быть больше 30.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 9:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В октябре дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 10:
                if (iDayInMonth > 30) {
                    Toast.makeText(context, "В ноябре дата не может быть больше 30.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 11:
                if (iDayInMonth > 31) {
                    Toast.makeText(context, "В декабре дата не может быть больше 31.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    //проверка, что название и описание заданы
    private boolean checkGeneralDataAction(Context context){
        if(etNameAction.getText().length()==0){
            Toast.makeText(context,"Не задано название события.",Toast.LENGTH_SHORT).show();
            return false;
        }
        if(etInfoAction.getText().length()==0){
            Toast.makeText(context,"Не задано описание события.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //обрабатываем выбор частоты событий
    AdapterView.OnItemSelectedListener freq_listener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            setVisibleElements(spinner_items_freq[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    /**
     * устанавливаем невидимость для некоторых элементов
     * @param sFrequency
     */
    public void setVisibleElements(String sFrequency){
        showAllElements();

        switch (sFrequency){
            case Action.FREQ_ONCE: layoutBlockWeekDays.setVisibility(View.GONE);
                            break;
            case Action.FREQ_YEAR: layoutBlockWeekDays.setVisibility(View.GONE);
                            etYear.setVisibility(View.GONE);
                            break;
            case Action.FREQ_MONTH: layoutBlockWeekDays.setVisibility(View.GONE);
                            etYear.setVisibility(View.GONE);
                            spMonths.setVisibility(View.GONE);
                            break;
            case Action.FREQ_WEEK: layoutBlockDate.setVisibility(View.GONE);
                            break;
            case Action.FREQ_EVERYDAY: layoutBlockDate.setVisibility(View.GONE);
                                layoutBlockWeekDays.setVisibility(View.GONE);
                                break;
            default: break;
        }
    }

    private void setStartValues(){
        try {

            etDay.setText(CurrentTime.getStringCurrentDayOfMonth());

            Integer num_month = Integer.valueOf(CurrentTime.getStringCurrentMonthOfYear());
            if(num_month==null) throw new Exception("Не удалось получить текущий месяц.");

            spMonths.setSelection(num_month-1);

            etYear.setText(CurrentTime.getStringCurrentYear());

            spWeekdays.setSelection(Integer.valueOf(CurrentTime.getStringCurrentDayOfWeek())-1);

            spStatus.setSelection(1);

            spFreq.setSelection(0);

            etHours.setText(CurrentTime.getStringCurrentHour());

            etMinutes.setText(CurrentTime.getStringCurrentMinute());

            etSeconds.setText("00");

        }catch(Exception e){
            e.printStackTrace();
        }
    }



    /**
     * делаем все элементы видимые
     */
    private void showAllElements(){
        etNameAction.setVisibility(View.VISIBLE);
        etInfoAction.setVisibility(View.VISIBLE);
        spStatus.setVisibility(View.VISIBLE);
        spFreq.setVisibility(View.VISIBLE);

        layoutBlockDate.setVisibility(View.VISIBLE);
        etDay.setVisibility(View.VISIBLE);
        spMonths.setVisibility(View.VISIBLE);
        etYear.setVisibility(View.VISIBLE);

        layoutBlockWeekDays.setVisibility(View.VISIBLE);
        spWeekdays.setVisibility(View.VISIBLE);

        layoutBlockTime.setVisibility(View.VISIBLE);
        etHours.setVisibility(View.VISIBLE);
        etMinutes.setVisibility(View.VISIBLE);
        etSeconds.setVisibility(View.VISIBLE);
    }

    //устанавливаем action_settings
    public void setAction_settings(ActionSettings action_settings) {
        if(action_settings!=null) this.action_settings = action_settings;
    }

    private void parceActionSettings(){
         //парсим данные и устанавливаем поля в ActionSettings
        etNameAction.setText(action_settings.sTitle);
        etInfoAction.setText(action_settings.sInfo);
        spStatus.setSelection(action_settings.iStatus);

        //частота событий
        String sFreq = action_settings.sFreq.split("_")[0];
        int iFreqPos = getNumPositionInFrequencySpinner(sFreq);
        spFreq.setSelection(iFreqPos);

        //недельные события
        Integer numWeekday=parceWeekdays(action_settings.sFreq);
        if(numWeekday!=null) spWeekdays.setSelection(numWeekday);

        //ежемесячные события
        Integer numMonths=parceMonthsNumber(action_settings.sFreq);
        if(numMonths!=null) spMonths.setSelection(numMonths);

        //парсим день
        String sDay=getDateOrDataParts(TimeAndDateRepository.TAG_DAY, action_settings.sFreq);
        if(sDay!=null) etDay.setText(sDay);

        //год
        String sYear=getDateOrDataParts(TimeAndDateRepository.TAG_YEAR, action_settings.sFreq);
        if(sYear!=null) etYear.setText(sYear);

        //часы
        String sHour=getDateOrDataParts(TimeAndDateRepository.TAG_HOUR, action_settings.sFreq);
        if(sHour!=null) etHours.setText(sHour);

        //минуты
        String sMinutes=getDateOrDataParts(TimeAndDateRepository.TAG_MINUTE, action_settings.sFreq);
        if(sMinutes!=null) etMinutes.setText(sMinutes);

        //секунды
        String sSeconds=getDateOrDataParts(TimeAndDateRepository.TAG_SECOND, action_settings.sFreq);
        if(sSeconds!=null) etMinutes.setText(sSeconds);

    }


    //парсим поля времени и даты
    private String getDateOrDataParts(@NonNull String TimeAndDateRepositoryTag,@NonNull String sFreq){
        try {
             return sFreq.split(TimeAndDateRepositoryTag+"=")[1].split(";")[0];
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    //находим номер элемента в R.array.month_names из sFreq
    private Integer parceMonthsNumber(String sFreq){
        try{
            String data=sFreq.split(TimeAndDateRepository.TAG_MONTH+"=")[1].split(";")[0];
            if(data==null) return null;
            String[] months=getResources().getStringArray(R.array.month_names);
            Integer pos=Integer.valueOf(data)-1;

            return pos;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    //находим номер элемента в R.array.week_days из sFreq
    private Integer parceWeekdays(String sFreq){
        try{
            String data=sFreq.split(TimeAndDateRepository.TAG_WEEKDAY+"=")[1].split(";")[0];
            String[] weekdays=getResources().getStringArray(R.array.week_days);
            Integer pos=0;
            for(int i=0;i<weekdays.length;i++){
                if(data.contains(weekdays[i])){
                    pos=i;
                    break;
                }
            }
            return pos;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //позиция в списке по названию месяца
    private int getNumPositionInFrequencySpinner(String sFreq){
        String[] freq = getResources().getStringArray(R.array.freq_items);
        int pos=0;
        for(int i=0;i<freq.length;i++){
            if(freq[i].equals(sFreq)){
                pos=i;
                break;
            }
        }
        return pos;

    }
}
