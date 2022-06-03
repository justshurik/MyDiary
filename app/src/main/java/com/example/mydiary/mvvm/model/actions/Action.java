package com.example.mydiary.mvvm.model.actions;

import androidx.annotation.NonNull;

import com.example.mydiary.mvvm.model.Singleton;
import com.example.mydiary.mvvm.model.database.table_canceled_actions.CancelledAction;
import com.example.mydiary.mvvm.model.database.tables_action_settings.ActionSettings;
import com.example.mydiary.mvvm.model.time_and_dates.CurrentTime;
import com.example.mydiary.mvvm.model.time_and_dates.TimeAndDateRepository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * класс, содержащий в себе все параметры выводимого события
 */
enum PART_TIME_IN_CONDITION {DAY, MONTH, YEAR, WEEKDAY, HOUR, MINUTE,SECOND, DATE, TIME};

public class Action {
    //теги для парсинга из ActionSettings данных о времени
    public final static String FREQ_ONCE="Один раз";
    public final static String FREQ_YEAR="Ежегодно";
    public final static String FREQ_MONTH="Ежемесячно";
    public final static String FREQ_WEEK="Еженедельно";
    public final static String FREQ_EVERYDAY="Ежедневно";

    private String sNameAction;
    private String sInfoAction;
    private String sDateAction;
    private String sTimeAction;
    private int iStatusAction;
    private long unixActionTime;
    private boolean isPastAction;

    public Action(){}

    public Action(String sNameAction, String sInfoAction, String sDateAction, String sTimeAction, int iStatusAction, long unixActionTime){
        this.sNameAction=sNameAction;
        this.sInfoAction=sInfoAction;
        this.sDateAction=sDateAction;
        this.sTimeAction=sTimeAction;
        this.iStatusAction=iStatusAction;
        this.unixActionTime=unixActionTime;
    }

    //сравнение двух пользовательских событий
    public boolean equals(Action action){
        if(action.sNameAction.equals(sNameAction) &&
            action.sInfoAction.equals(sInfoAction) &&
            action.unixActionTime==unixActionTime &&
            action.iStatusAction==iStatusAction)
            return true;
        else return false;

    }

    /**
     * функция генерации событий из всех инструкций в БД
     * @return
     */
    public ArrayList<Action> generateActions(){
        try{
            //удаляем все неактуальные отмененные события
            clearUnActualCanceledActionsInDB();

            //берем все инструкции из БД
            List<ActionSettings> action_settings = Singleton.
                                                    getInstance().
                                                    getDb_action_settings().
                                                    idao_action_settings().
                                                    getAllActions();

            ArrayList<Action> actions = new ArrayList<>();

            //генерируем события из инструкций, которые приведены в Базе данных
            for(ActionSettings e:action_settings){
                ArrayList<Action> buff_actions=generateActionFromActionSettings(e);
                for(int i=0;i<buff_actions.size();i++){
                    actions.add(buff_actions.get(i));
                }
            }

            //проверка на события, которые уже были отменены пользователем ранее
            //выгружаем все события, которые пользователь отменил
            List<CancelledAction> cancelledActions = getActualCancelledActions();

            //убираем из выборки те, что уже были отменены
            if(cancelledActions.size()>0){
                for(int j=0;j<cancelledActions.size();j++){
                    for(int i=0;i<actions.size();i++){
                           if(actions.get(i).sNameAction.equals(cancelledActions.get(j).sNameAction) &&
                                actions.get(i).sInfoAction.equals(cancelledActions.get(j).sInfoAction) &&
                                actions.get(i).iStatusAction==cancelledActions.get(j).iStatusAction &&
                                actions.get(i).unixActionTime==cancelledActions.get(j).unixActionTime){
                            actions.remove(i);
                            i--;
                        }
                    }
                }
            }
            //сортируем по возрастанию unix времени
            actions=sortActions(actions);

            return actions;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private List<CancelledAction> getActualCancelledActions(){
        //стираем устарешие отмененные события
        clearUnActualCanceledActionsInDB();

        List<CancelledAction> cancelledActions = Singleton.getInstance().
                getDb_cancelled_actions().
                IDAO_canceled_actions().
                getAllCancelledActions();

        return cancelledActions;
    }

    //стираем в БД события, которые не актуальны
    public void clearUnActualCanceledActionsInDB(){
        String currentDate=CurrentTime.getStringCurrentDate();
        int daysPast=Singleton.getInstance().getUser_settings().getPast_days();
        int futureDays=Singleton.getInstance().getUser_settings().getFuture_days()+1;
        String minDate=TimeAndDateRepository.calcNewShiftedDate(currentDate,-daysPast);
        String maxDate=TimeAndDateRepository.calcNewShiftedDate(currentDate,futureDays);

        Long minUnix=TimeAndDateRepository.calcUnixTime(minDate,"00:00:00");
        Long maxUnix=TimeAndDateRepository.calcUnixTime(maxDate,"00:00:00");
        maxUnix--;
        //удаляем все неактуальные события, которые отменены пользователем
        Singleton.getInstance().
                getDb_cancelled_actions().
                IDAO_canceled_actions().
                deleteUnusableActions(minUnix,maxUnix);

    }

    /**
     * функция для генерации из настройки события, заданной в БД (класс ActionSettings) коллекции готовых для отображения событий
     * @param action_settings
     * @return
     */
    private ArrayList<Action> generateActionFromActionSettings(ActionSettings action_settings){
        try{
            ArrayList<Action> actions = new ArrayList<>();

            if(action_settings.sFreq.contains(FREQ_ONCE)){

                actions=generateListOfOneAction(action_settings);
                if(actions==null) throw new Exception("Не удалось сгенерировать разовое событие.");

            }else if(action_settings.sFreq.contains(FREQ_YEAR)){

                actions=generateListOfYearAction(action_settings);
                if(actions==null) throw new Exception("Не удалось сгенерировать ежегодные события");

            }else if(action_settings.sFreq.contains(FREQ_MONTH)){

                actions=generateListOfMonthAction(action_settings);
                if(actions==null) throw new Exception("Не удалось сгенерировать ежемесячные события");

            }else if(action_settings.sFreq.contains(FREQ_WEEK)){

                actions=generateListOfWeekAction(action_settings);
                if(actions==null) throw new Exception("Не удалось сгенерировать еженедельные события");

            }else if(action_settings.sFreq.contains(FREQ_EVERYDAY)){

                actions=generateListOfEverydayAction(action_settings);
                if(actions==null) throw new Exception("Не удалось сгенерировать еженедельные события");

            }

            if(actions.size()>1){
                //убираем повторяющуюся дату и время - она всегда будет идти нулевой
                actions.remove(0);
            }

            //проставляем тег, является ли событие прошедшим или нет


            return actions;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //генерируем ежедневные события
    private ArrayList<Action> generateListOfEverydayAction(ActionSettings actionSettings){
        try{

            //текущая дата
            String currentDate= CurrentTime.getStringCurrentDate();

            //время
            String sTime=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.TIME);

            String leftDate=currentDate, rightDate=currentDate;

            ArrayList<Action> actions = new ArrayList<>();


            //идем влево (в прошлое)
            while (isDateInUserSettingsDiapazon(leftDate,currentDate)) {
                //unix-время события
                Long unixTime = TimeAndDateRepository.calcUnixTime(leftDate, sTime);
                if (unixTime == null)
                    throw new Exception("Не удалось получить unix-время для движения влево из " + leftDate + " " + sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        leftDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //уменьшаем дату на 1 день
                leftDate = TimeAndDateRepository.calcNewShiftedDate(leftDate, -1);
            }

            //идем вправо (в будущее)
            while (isDateInUserSettingsDiapazon(rightDate,currentDate)) {
                //unix-время события
                Long unixTime = TimeAndDateRepository.calcUnixTime(rightDate, sTime);
                if (unixTime == null)
                    throw new Exception("Не удалось получить unix-время для движения вправо из " + rightDate + " " + sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        rightDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //уменьшаем дату на 1 день
                rightDate = TimeAndDateRepository.calcNewShiftedDate(rightDate, 1);
            }


            return actions;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //генерируем еженедельные события
    private ArrayList<Action> generateListOfWeekAction(ActionSettings actionSettings){
        try{
            //текущая дата
            String currentDate= CurrentTime.getStringCurrentDate();

            //время
            String sTime=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.TIME);

            Integer iCurrentWeekDay=TimeAndDateRepository.getDayOfWeekNumber(currentDate);
            if(iCurrentWeekDay==null) throw new Exception("Не удалось получить номер для недели из даты "+currentDate);

            String sNeededWeekDay=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.WEEKDAY);
            if(sNeededWeekDay==null) throw new Exception("Не удалось получить день недели из условия события "+actionSettings.sFreq);
            Integer iNeededWeekDay=TimeAndDateRepository.calcNumberDaysOfWeekFromStringResourceToInt(sNeededWeekDay);
            if(iNeededWeekDay==null) throw new Exception("Не удалось получить номер дня недели из "+sNeededWeekDay);

            int addDays=iNeededWeekDay-iCurrentWeekDay;
            String leftDate=TimeAndDateRepository.calcNewShiftedDate(currentDate,addDays);
            String rightDate=leftDate;

            ArrayList<Action> actions = new ArrayList<>();

            //идем влево (в прошлое)
            while (isDateInUserSettingsDiapazon(leftDate,currentDate)){
                //unix-время события
                Long unixTime=TimeAndDateRepository.calcUnixTime(leftDate,sTime);
                if(unixTime==null) throw new Exception("Не удалось получить unix-время для движения влево из "+leftDate+" "+sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        leftDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //уменьшаем дату на 7 дней
                leftDate=TimeAndDateRepository.calcNewShiftedDate(leftDate,-7);

            }

            //идем в право
            while (isDateInUserSettingsDiapazon(rightDate,currentDate)){
                //unix-время события
                Long unixTime=TimeAndDateRepository.calcUnixTime(rightDate,sTime);
                if(unixTime==null) throw new Exception("Не удалось получить unix-время для движения вправо из "+rightDate+" "+sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        rightDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //уменьшаем дату на 7 дней
                rightDate=TimeAndDateRepository.calcNewShiftedDate(rightDate,7);

            }

            return actions;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //генерируем ежемесячные события
    private ArrayList<Action> generateListOfMonthAction(ActionSettings actionSettings){
        try{

            //текущая дата
            String currentDate= CurrentTime.getStringCurrentDate();
            //время
            String sTime=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.TIME);

            //берем число
            String sDay=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.DAY);

            //берем текущий месяц
            String currentMonth = CurrentTime.getStringCurrentMonthOfYear();
            int iMonth=Integer.valueOf(currentMonth);

            //берем текущий год
            String sCurrentYear=CurrentTime.getStringCurrentYear();
            int iYear=Integer.valueOf(sCurrentYear);

            //левая дата
            String leftDate=sDay+"."+currentMonth+"."+sCurrentYear;
            //корректируем вот эти значения
            int iLeftMonth=iMonth;
            int iLeftYear=iYear;
            leftDate=checkDateForCorrection(leftDate);

            //правая дата
            String rightDate=leftDate;
            //корректируем вот эти значения
            int iRightMonth=iMonth;
            int iRightYear=iYear;

            ArrayList<Action> actions = new ArrayList<>();

            //прошлые события
            while (isDateInUserSettingsDiapazon(leftDate,currentDate)){

                //двигаемся влево пока каждая дата события не выйдет за заданный пользователем интервал

                //unix-время события
                Long unixTime=TimeAndDateRepository.calcUnixTime(leftDate,sTime);
                if(unixTime==null) throw new Exception("Не удалось получить unix-время для движения влево из "+leftDate+" "+sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        leftDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //формируем новую дату в прошлом, уменьшенную на 1 месяц
                iLeftMonth--;
                if(iLeftMonth==0){
                    iLeftMonth=12;
                    iLeftYear--;
                }
                String sM=String.valueOf(iLeftMonth);
                if(iLeftMonth<10) sM="0"+sM;
                leftDate=sDay+"."+sM+"."+iLeftYear;

                //проверяем на корректность новую дату
                leftDate=checkDateForCorrection(leftDate);

            }

            //будущие события
            while (isDateInUserSettingsDiapazon(rightDate,currentDate)) {
                //двигаемся вправо пока каждая дата события не выйдет за заданный пользователем интервал

                //unix-время события
                Long unixTime=TimeAndDateRepository.calcUnixTime(rightDate,sTime);
                if(unixTime==null) throw new Exception("Не удалось получить unix-время для движения вправо из "+rightDate+" "+sTime);

                Action act = new Action(actionSettings.sTitle,
                                    actionSettings.sInfo,
                                    rightDate,
                                    sTime,
                                    actionSettings.iStatus,
                                    unixTime);

                actions.add(act);

                //прибавляем месяц
                iRightMonth++;
                if(iRightMonth>12){
                    iRightMonth=1;
                    iRightYear++;
                }
                String sM=String.valueOf(iRightMonth);
                if(iRightMonth<10) sM="0"+sM;

                rightDate=sDay+"."+sM+"."+iRightYear;

                //проверяем на корректность новую дату
                rightDate=checkDateForCorrection(rightDate);

            }

            return actions;

        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    //генерируем ежегодные события
    private ArrayList<Action> generateListOfYearAction(ActionSettings actionSettings){
        try{

            //текущая дата
            String currentDate= CurrentTime.getStringCurrentDate();

            //месяц
            String sMonth=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.MONTH);
            //число месяца
            String sDay=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.DAY);
            //время
            String sTime=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.TIME);
            if(sTime==null) throw new Exception("Не удалось запарсить время в настройках "+actionSettings.sFreq);

            //текущий год
            String sCurrYear=CurrentTime.getStringCurrentYear();
            //начальные приближения
            String leftDate=sDay+"."+sMonth+"."+sCurrYear;
            leftDate=checkDateForCorrection(leftDate);  //проверяем на корректность новую дату
            //год левой даты, который будем корректировать
            Integer iLeftYear=Integer.valueOf(sCurrYear);

            String rightDate=leftDate;

            //год правой даты, который будем корректировать
            Integer iRightYear=Integer.valueOf(sCurrYear);

            ArrayList<Action> actions = new ArrayList<>();

            //цикл для прошлых дат событий
            while (isDateInUserSettingsDiapazon(leftDate,currentDate)){
                //двигаемся влево пока каждая дата события не выйдет за заданный пользователем интервал

                //unix-время события
                Long unixTime=TimeAndDateRepository.calcUnixTime(leftDate,sTime);
                if(unixTime==null) throw new Exception("Не удалось получить unix-время для движения влево из "+leftDate+" "+sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        leftDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //уменьшаем на 1 год дату влево (в прошлое)
                iLeftYear--;
                leftDate=sDay+"."+sMonth+"."+iLeftYear;
                //проверяем на корректность новую дату
                leftDate=checkDateForCorrection(leftDate);

            }

            //цикл для будущих дат событий
            while (isDateInUserSettingsDiapazon(rightDate,currentDate)){
                //двигаемся вправо пока каждая дата события не выйдет за заданный пользователем интервал

                //unix-время события
                Long unixTime=TimeAndDateRepository.calcUnixTime(rightDate,sTime);
                if(unixTime==null) throw new Exception("Не удалось получить unix-время для движения вправо из "+rightDate+" "+sTime);

                Action act = new Action(actionSettings.sTitle,
                        actionSettings.sInfo,
                        rightDate,
                        sTime,
                        actionSettings.iStatus,
                        unixTime);

                actions.add(act);

                //увеличиваем на 1 год дату вправо (в будущее)
                iRightYear++;
                rightDate=sDay+"."+sMonth+"."+iRightYear;
                //проверяем на корректность новую дату
                rightDate=checkDateForCorrection(rightDate);
            }

            return  actions;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //вспомогательная функция проверки, что дата является корректной. если дата не корректна (например, 29.02 в то время как в текущем годе
    //максимальная дата 28.02, то возвращается самая поздняя корректная дата, т.е. 28.02)
    private String checkDateForCorrection(String sDate) throws ParseException {
        String correctDate=sDate;

        String sDay=CurrentTime.sdf_day.format(CurrentTime.sdf_current_date.parse(sDate));
        int this_day_of_month=Integer.valueOf(sDay);

        String sMonth=CurrentTime.sdf_month.format(CurrentTime.sdf_current_date.parse(sDate));

        String sYear=CurrentTime.sdf_year.format(CurrentTime.sdf_current_date.parse(sDate));
        //первое число месяцадля определения кол-ва дней в месяце
        String sD="01."+sMonth+"."+sYear;
        Date d = CurrentTime.sdf_current_date.parse(sD);
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int maxDaysInMonth=c.getActualMaximum(Calendar.DAY_OF_MONTH);

        if(this_day_of_month>maxDaysInMonth){
            //если текущая дата превышает максимальную дату в месяце, то устанавливаем в последнюю дату месяца в место текущей
            correctDate=maxDaysInMonth+"."+sMonth+"."+sYear;
        }
        return correctDate;
    }

    //генерируем список разовых событий
    private ArrayList<Action> generateListOfOneAction(ActionSettings actionSettings){
        try{
            //дата, указанная в разовом событии actionSettings
            String sDate=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.DATE);
            if(sDate==null) throw new Exception("Не удалось запарсить дату из "+actionSettings.sFreq);

            //текущая дата
            String currentDate= CurrentTime.getStringCurrentDate();

            //проверка на совпадение даты событий с настройками пользователя
            //сами настройки берутся от Singletone. От туда берутся количество дней за которые надо геерировать события из прошлого и будущего
            //относительно текущей даты currentDate. Если дата sDate проходит в заданный диапазон, то ОК, если нет,
            // то возвращаем пустую коллекцию событий
            if(!isDateInUserSettingsDiapazon(sDate,currentDate)) return new ArrayList<Action>();

            //собираем остальные данные
            String sTime=parceTimeCondition(actionSettings.sFreq,PART_TIME_IN_CONDITION.TIME);
            if(sTime==null) throw new Exception("Не удалось запарсить время из "+actionSettings.sFreq);

            Long unixTime=TimeAndDateRepository.calcUnixTime(sDate,sTime);
            if(unixTime==null) throw new Exception("Не удалось получить unix время из даты "+sDate+" времени "+sTime);

            ArrayList<Action> actions = new ArrayList<>();

            Action act = new Action(actionSettings.sTitle,
                    actionSettings.sInfo,
                    sDate,
                    sTime,
                    actionSettings.iStatus,
                    unixTime);

            actions.add(act);
            return actions;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //проверка, лежит ли дата sDate в диапазоне, который задал пользователь, относительно даты currentDate
    private boolean isDateInUserSettingsDiapazon(String sDate, String currentDate){

        int pastDays= -Singleton.getInstance().getUser_settings().getPast_days();
        int futureDays=Singleton.getInstance().getUser_settings().getFuture_days();
        //минимальная дата события относительно currentDate
        String minDate=TimeAndDateRepository.calcNewShiftedDate(currentDate,pastDays);

        //максимально возможная даа события относительно currentDate
        String maxDate=TimeAndDateRepository.calcNewShiftedDate(currentDate,futureDays);

        //проверка попадает ли sDate в заданный диапазон minDate и maxDate (включительно)
        if(TimeAndDateRepository.calcDaysBetweenTwoDates(minDate,sDate)>=0 &&
                TimeAndDateRepository.calcDaysBetweenTwoDates(sDate,maxDate)>=0)
            return true;
        else
            return false;

    }

    //функция парсинга временных условий из настройки события
    private String parceTimeCondition(@NonNull String sFreqConditon, @NonNull PART_TIME_IN_CONDITION PART_TAG) {

        try {

            if(PART_TAG==PART_TIME_IN_CONDITION.DAY){

                if(!sFreqConditon.contains(TimeAndDateRepository.TAG_DAY)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_DAY+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.MONTH){

                if(!sFreqConditon.contains(TimeAndDateRepository.TAG_MONTH)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_MONTH+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.YEAR){
                if(!sFreqConditon.contains(TimeAndDateRepository.TAG_YEAR)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_YEAR+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.HOUR) {

                if (!sFreqConditon.contains(TimeAndDateRepository.TAG_HOUR)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_HOUR+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.MINUTE) {

                if (!sFreqConditon.contains(TimeAndDateRepository.TAG_MINUTE)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_MINUTE+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.SECOND) {

                if (!sFreqConditon.contains(TimeAndDateRepository.TAG_SECOND)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_SECOND+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.WEEKDAY) {
                if (!sFreqConditon.contains(TimeAndDateRepository.TAG_WEEKDAY)) return null;
                return sFreqConditon.split(TimeAndDateRepository.TAG_WEEKDAY+"=")[1].split(";")[0];

            }else if(PART_TAG==PART_TIME_IN_CONDITION.DATE) {
                //берем дату в формате dd.MM.yyyy
                //провека на теги

                if (!sFreqConditon.contains(TimeAndDateRepository.TAG_DAY) || !sFreqConditon.contains(TimeAndDateRepository.TAG_MONTH) || !sFreqConditon.contains(TimeAndDateRepository.TAG_YEAR)) return null;
                //день
                String sDay = parceTimeCondition(sFreqConditon, PART_TIME_IN_CONDITION.DATE);
                if (sDay == null) return null;
                //месяц
                String sMonth = parceTimeCondition(sFreqConditon, PART_TIME_IN_CONDITION.MONTH);
                if (sMonth == null) return null;
                // год
                String sYear = parceTimeCondition(sFreqConditon, PART_TIME_IN_CONDITION.YEAR);
                if (sYear == null) return null;
                return sDay+"."+sMonth+"."+sYear;

            }else if((PART_TAG==PART_TIME_IN_CONDITION.TIME)){
                //парсим время в формате hh:mm:ss
                if (!sFreqConditon.contains(TimeAndDateRepository.TAG_HOUR) || !sFreqConditon.contains(TimeAndDateRepository.TAG_MINUTE) || !sFreqConditon.contains(TimeAndDateRepository.TAG_SECOND)) return null;

                //часы
                String sHour=parceTimeCondition(sFreqConditon, PART_TIME_IN_CONDITION.HOUR);
                if(sHour==null) return null;
                //минуты
                String sMinute=parceTimeCondition(sFreqConditon, PART_TIME_IN_CONDITION.MINUTE);
                if(sMinute==null) return null;
                //секунды
                String sSeconds=parceTimeCondition(sFreqConditon, PART_TIME_IN_CONDITION.SECOND);
                if(sSeconds==null) return null;

                return sHour+":"+sMinute+":"+sSeconds;

            }else throw  new Exception("Не верно задан тег для парсинга элемента события "+sFreqConditon);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    //быстрая сортировка событий по-возрастанию unix-времени
    private ArrayList<Action> sortActions(ArrayList<Action> actions){
        if(actions.size()<2) return actions;
        return quick_sort(actions,0,actions.size()-1);
    }

    private ArrayList<Action> quick_sort(ArrayList<Action> list, int l, int r){
        if(l<r){
            int q=calc_q(list,l,r);
            quick_sort(list,l,q-1);
            quick_sort(list,q+1,r);
        }
        return list;
    }

    private int calc_q(ArrayList<Action> list, int l, int r){
        Action x=list.get(r);
        int q=l-1;
        Action b;
        for(int i=l;i<r;i++){
            if(list.get(i).unixActionTime<=x.unixActionTime){
                q++;
                b=list.get(i);
                list.set(i,list.get(q));
                list.set(q,b);

            }
        }
        q++;
        b=list.get(r);
        list.set(r,list.get(q));
        list.set(q,b);
        return q;
    }

    public String getsNameAction() {
        return sNameAction;
    }

    public String getsInfoAction() {
        return sInfoAction;
    }

    public String getsDateAction() {
        return sDateAction;
    }

    public String getsTimeAction() {
        return sTimeAction;
    }

    public int getiStatusAction() {
        return iStatusAction;
    }

    public void setPastAction(boolean pastAction) {
        isPastAction = pastAction;
    }

    public long getUnixActionTime() {
        return unixActionTime;
    }
}
