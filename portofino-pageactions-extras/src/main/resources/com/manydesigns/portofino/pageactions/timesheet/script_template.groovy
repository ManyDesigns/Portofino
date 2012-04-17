import com.manydesigns.portofino.pageactions.timesheet.TimesheetAction
import com.manydesigns.portofino.pageactions.timesheet.util.PersonDay
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import com.manydesigns.portofino.security.SupportsPermissions
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import com.manydesigns.portofino.pageactions.timesheet.model.*

@RequiresPermissions(level = AccessLevel.VIEW)
@SupportsPermissions(TimesheetAction.PERMISSION_MANAGE_NON_WORKING_DAYS)
class MyTimesheetAction extends TimesheetAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    //**************************************************************************
    // Sample timesheet implementation
    //**************************************************************************

    static ActivityType at0 = new ActivityType("at0", "fatturabile", ActivityMetaType.BILLABLE);
    static ActivityType at1 = new ActivityType("at1", "non fatturabile", ActivityMetaType.NON_BILLABLE);
    static ActivityType at2 = new ActivityType("at2", "assenza", ActivityMetaType.LEAVE);

    static Activity ac1 = new Activity("ac1", "#0100", "Acme System", "Analisi", "Analisi dei requisiti", at0, null, null, null);
    static Activity ac2 = new Activity("ac2", "#0100", "Acme System", "Progettazione", null, at0, null, null, null);
    static Activity ac3 = new Activity("ac3", "#0100", "Acme System", "Sviluppo", null, at0, null, null, null);

    static Activity ac4 = new Activity("ac4", "#0101", "Kinetic website", "Supporto e gestione", null, at0, null, null, null);

    static Activity ac5 = new Activity("ac5", "#0001", "Processi interni", "Marketing", null, at1, null, null, "http://www.manydesigns.com/");
    static Activity ac6 = new Activity("ac6", "#0001", "Processi interni", "Formazione", null, at1, null, null, null);

    static Activity ac7 = new Activity("ac7", "#0002", "Assenze", "Malattia", null, at2, null, null, null);
    static Activity ac8 = new Activity("ac8", "#0002", "Assenze", "Ferie", null, at2, null, null, null);
    static Activity ac9 = new Activity("ac9", "#0002", "Assenze", "Permesso", null, at2, null, null, null);

    static Person mario    = new Person("mario", "Mario Rossi", null, null, true);
    static Person giovanni = new Person("giovanni", "Giovanni Bianchi", null, null, false);

    static Map<LocalDate, PersonDay> marioDayDb    =
            new HashMap<LocalDate, PersonDay>();
    static Map<LocalDate, PersonDay> giovanniDayDb =
            new HashMap<LocalDate, PersonDay>();

    static Set<LocalDate> nonWorkingDaysDb = new HashSet<LocalDate>();

    static {
        LocalDate today = new LocalDate();
        LocalDate currentDay = today;
        boolean locked = false;
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_1 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_2 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_3 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_4 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_5 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_6 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_7 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_8 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_9 = new PersonDay(mario, currentDay, null, locked);

        currentDay = currentDay.minusDays(1);
        locked = checkSunday(currentDay, locked);
        currentDay = skipNonWorkingDays(currentDay);
        PersonDay mario_10 = new PersonDay(mario, currentDay, null, locked);

        marioDayDb.put(mario_1.getDate(), mario_1);
        marioDayDb.put(mario_2.getDate(), mario_2);
        marioDayDb.put(mario_3.getDate(), mario_3);
        marioDayDb.put(mario_4.getDate(), mario_4);
        marioDayDb.put(mario_5.getDate(), mario_5);
        marioDayDb.put(mario_6.getDate(), mario_6);
        marioDayDb.put(mario_7.getDate(), mario_7);
        marioDayDb.put(mario_8.getDate(), mario_8);
        marioDayDb.put(mario_9.getDate(), mario_9);
        marioDayDb.put(mario_10.getDate(), mario_10);


        mario_1.addEntry(ac2, 150, null);
        mario_1.addEntry(ac4, 60, null);

        mario_2.addEntry(ac1, 60, null);
        mario_2.addEntry(ac2, 180, "Studio di fattibilità");
        mario_2.addEntry(ac6, 180, null);
        mario_2.addEntry(ac9, 60, null);

        mario_3.addEntry(ac1, 210, null);
        mario_3.addEntry(ac2, 270, null);

        mario_4.addEntry(ac1, 480, null);

        mario_5.addEntry(ac1, 90, "Intervista con cliente");
        mario_5.addEntry(ac4, 210, null);
        mario_5.addEntry(ac5, 180, null);

        mario_6.addEntry(ac1, 60, null);
        mario_6.addEntry(ac4, 180, null);
        mario_6.addEntry(ac5, 300, null);

        mario_7.addEntry(ac5, 480, null);

        mario_8.addEntry(ac2, 240, null);
        mario_8.addEntry(ac4, 240, null);

        mario_9.addEntry(ac2, 120, null);
        mario_9.addEntry(ac4, 360, null);

        mario_10.addEntry(ac2, 300, null);
        mario_10.addEntry(ac6, 180, null);

        // set non-working days 60 days before to 60 days after
        currentDay = today.minusDays(60);
        for (int i = 0; i < 120; i++) {
            if (currentDay.getDayOfWeek() >= DateTimeConstants.SATURDAY) {
                nonWorkingDaysDb.add(currentDay);
            }
            currentDay = currentDay.plusDays(1);
        }
    }

    public static boolean checkSunday(LocalDate currentDay, boolean locked) {
        return locked || (currentDay.getDayOfWeek() == DateTimeConstants.SUNDAY);
    }

    public static LocalDate skipNonWorkingDays(LocalDate currentDay) {
        while (currentDay.getDayOfWeek() >= DateTimeConstants.SATURDAY) {
            currentDay = currentDay.minusDays(1);
        }
        return currentDay;
    }

    //**************************************************************************
    // Index view
    //**************************************************************************

    @Override
    public void loadExecuteModel() {
        availablePersons.add(mario);
        availablePersons.add(giovanni);
    }

    //**************************************************************************
    // Week data entry
    //**************************************************************************

    @Override
    public void loadWeekEntryModel() throws Exception {
        super.loadWeekEntryModel();
        LocalDate today = new LocalDate();
        Map<LocalDate, PersonDay> personDayDb;
        if (mario.getId().equals(personId)) {
            weekEntryModel.setPersonId(mario.id);
            weekEntryModel.setPersonName(mario.longName);
            personDayDb = marioDayDb;
        } else if (giovanni.getId().equals(personId)) {
            weekEntryModel.setPersonId(giovanni.id);
            weekEntryModel.setPersonName(giovanni.longName);
            personDayDb = giovanniDayDb;
        } else {
            throw new Exception("Person non found");
        }

        List<Activity> weekActivities =
                weekEntryModel.getActivities();
        weekActivities.add(ac1);
        weekActivities.add(ac2);
        weekActivities.add(ac3);
        weekActivities.add(ac4);
        weekActivities.add(ac5);
        weekActivities.add(ac6);
        weekActivities.add(ac7);
        weekActivities.add(ac8);
        weekActivities.add(ac9);

        for (int i = 0; i < 7; i++) {
            logger.debug("Setting day standard working minutes");
            WeekEntryModel.Day day = weekEntryModel.getDay(i);
            if (day.isNonWorking()) {
                day.setStandardWorkingMinutes(0);
            } else {
                day.setStandardWorkingMinutes(8 * 60);
            }

            logger.debug("Setting today flag");
            LocalDate dayDate = day.getDate();
            if (dayDate.equals(today)) {
                day.setToday(true);
            }
            if (nonWorkingDaysDb.contains(dayDate)) {
                day.setNonWorking(true);
            }

            logger.debug("Setting day status and entries");
            PersonDay personDay = personDayDb.get(dayDate);
            if (personDay == null) {
                day.setStatus(null);
            } else {
                if (personDay.isLocked()) {
                    day.setStatus(WeekEntryModel.DayStatus.LOCKED);
                } else {
                    day.setStatus(WeekEntryModel.DayStatus.OPEN);
                }

                for (Map.Entry<Activity, WeekEntryModel.Entry> current : personDay.getEntries().entrySet()) {
                    WeekEntryModel.Entry value = current.getValue();
                    day.addEntry(
                            current.getKey(),
                            value.getMinutes(),
                            value.getNote()
                    );
                }
            }

        }
    }


    @Override
    public void saveWeekEntry(WeekEntryModel.Day day, Activity activity, int minutes, String note) {
        PersonDay personDay = marioDayDb.get(day.getDate());
        WeekEntryModel.Entry entry = new WeekEntryModel.Entry();
        entry.setMinutes(minutes);
        entry.setNote(note);
        personDay.getEntries().put(activity, entry);
    }

    @Override
    public void updateWeekEntry(WeekEntryModel.Day day, Activity activity, int minutes, String note) {
        PersonDay personDay = marioDayDb.get(day.getDate());
        Map<Activity, WeekEntryModel.Entry> entries = personDay.getEntries();
        WeekEntryModel.Entry entry = entries.get(activity);
        entry.setMinutes(minutes);
        entry.setNote(note);
    }

    @Override
    public void deleteWeekEntry(WeekEntryModel.Day day, Activity activity) {
        PersonDay personDay = marioDayDb.get(day.getDate());
        Map<Activity, WeekEntryModel.Entry> entries = personDay.getEntries();
        entries.remove(activity);
    }

    //**************************************************************************
    // Non working days view
    //**************************************************************************

    @Override
    public void loadNonWorkingDaysModel() {
        for (LocalDate current : nonWorkingDaysDb) {
            DateTime dateTime = current.toDateTimeAtCurrentTime(dtz);
            NonWorkingDaysModel.NWDDay day =
                    nonWorkingDaysModel.findDayByDateTime(dateTime);
            if (day != null) {
                day.setNonWorking(true);
            }
        }
    }

    @Override
    public void saveNonWorkingDay(LocalDate date, boolean nonWorking) {
        if (nonWorking) {
            nonWorkingDaysDb.add(date);
        } else {
            nonWorkingDaysDb.remove(date);
        }
    }


}