import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.manydesigns.portofino.pageactions.timesheet.util.PersonDay
import com.manydesigns.portofino.security.AccessLevel
import com.manydesigns.portofino.security.RequiresPermissions
import java.awt.Color
import javax.servlet.ServletContext
import org.joda.time.DateMidnight
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate
import com.manydesigns.portofino.pageactions.monthreport.model.*
import com.manydesigns.portofino.pageactions.timesheet.model.*
import com.manydesigns.portofino.pageactions.monthreport.MonthReportAction

@RequiresPermissions(level = AccessLevel.VIEW)
class MyMonthReportAction extends MonthReportAction {

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
    public void setupParameterForm() {
        super.setupParameterForm();
    }


    //**************************************************************************
    // Month report
    //**************************************************************************

    @Override
    public void loadMonthReportModel() {
        monthReportModel.setPersonId("MR");
        monthReportModel.setPersonName("Mario Rossi");

        MonthReportModel.Node rootNode =
                monthReportModel.createNode("root", "root node");
        rootNode.setColor(new Color(0x88a1c7));
        monthReportModel.setRootNode(rootNode);


        MonthReportModel.Node node;
        Color color = new Color(0xfff8cb);

        MonthReportModel.Node euProjectsNode = addReportNode(rootNode, "eu", "EU-Projects", new Color(50, 50, 255));
        node = addReportNode(euProjectsNode, "prjx", "Project x", color);
        node = addReportNode(euProjectsNode, "prjy", "Project y", color);
        node = addReportNode(euProjectsNode, "prjz", "Project z", color);

        Color euColor = new Color(200, 200, 255);
        MonthReportModel.Node rdActivitiesNode = addReportNode(euProjectsNode, "rd", "R&D Activities", euColor);
        node = addReportNode(rdActivitiesNode, "prjx", "Project x", color);
        node = addReportNode(rdActivitiesNode, "prjy", "Project y", color);
        node = addReportNode(rdActivitiesNode, "prjz", "Project z", color);

        MonthReportModel.Node demonstrationNode = addReportNode(euProjectsNode, "demo", "Demonstration", euColor);
        node = addReportNode(demonstrationNode, "prjx", "Project x", color);
        node = addReportNode(demonstrationNode, "prjy", "Project y", color);
        node = addReportNode(demonstrationNode, "prjz", "Project z", color);

        MonthReportModel.Node managementNode = addReportNode(euProjectsNode, "mgmt", "Management", euColor);
        node = addReportNode(managementNode, "prjx", "Project x", color);
        node = addReportNode(managementNode, "prjy", "Project y", color);
        node = addReportNode(managementNode, "prjz", "Project z", color);

        MonthReportModel.Node otherActivitiesNode = addReportNode(euProjectsNode, "oa", "Other Activities", euColor);
        node = addReportNode(otherActivitiesNode, "prjx", "Project x", color);
        node = addReportNode(otherActivitiesNode, "prjy", "Project y", color);
        node = addReportNode(otherActivitiesNode, "prjz", "Project z", color);

        MonthReportModel.Node internalNode = addReportNode(rootNode, "in", "Internal and Other Projects", new Color(30, 255, 30));
        node = addReportNode(internalNode, "te", "Teaching", color);
        node = addReportNode(internalNode, "b", "B", color);
        node = addReportNode(internalNode, "c", "C", color);

        MonthReportModel.Node absencesNode = addReportNode(rootNode, "abs", "Absences", new Color(30, 255, 30));
        node = addReportNode(absencesNode, "al", "Annual Leave", color);
        node = addReportNode(absencesNode, "sl", "Special Leave", color);
        node = addReportNode(absencesNode, "ill", "Illness", color);

        node.setMinutes(7, 9);
        node.setMinutes(3, 10);
        node.setMinutes(7, 40);
        node.setMinutes(3, 12);
        node.setMinutes(7, 66);
        node.setMinutes(3, 42);
        node.setMinutes(7, 40);

        absencesNode.calculateMinutesFromChildNodes();
        internalNode.calculateMinutesFromChildNodes();
        otherActivitiesNode.calculateMinutesFromChildNodes();
        managementNode.calculateMinutesFromChildNodes();
        demonstrationNode.calculateMinutesFromChildNodes();
        rdActivitiesNode.calculateMinutesFromChildNodes();
        euProjectsNode.calculateMinutesFromChildNodes();
        rootNode.calculateMinutesFromChildNodes();

        for (int i = 0; i < monthReportModel.getDaysCount(); i++) {
            MonthReportModel.Day day = monthReportModel.getDay(i);
            DateMidnight date = day.getDayStart();
            int dayOfWeek = date.getDayOfWeek();
            if (dayOfWeek == DateTimeConstants.SATURDAY
                    || dayOfWeek == DateTimeConstants.SUNDAY) {
                day.setNonWorking(true);
            }
        }
    }

    public void addMonthReportHeaderLeft(PdfPTable headerTable) throws Exception {
        ServletContext context = getContext().getServletContext();
        String imagePath = context.getRealPath("/famfamfam_mini_icons/action_back.gif");
        Image image = Image.getInstance(imagePath);
        image.scalePercent(100f);
        PdfPCell headerCell = new PdfPCell(image);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(headerCell);
    }

    public void setupReferenceDateMidnight() {
        super.setupReferenceDateMidnight();
    }

    public boolean parameterFormValidate() {
        return super.parameterFormValidate();
    }

}