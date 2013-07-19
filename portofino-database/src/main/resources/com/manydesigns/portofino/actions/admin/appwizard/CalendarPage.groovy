import javax.servlet.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.elements.util.Util
import com.manydesigns.portofino.*
import com.manydesigns.portofino.buttons.*
import com.manydesigns.portofino.buttons.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.pageactions.*
import com.manydesigns.portofino.reflection.TableAccessor
import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.shiro.*
import com.manydesigns.portofino.util.ShortNameUtils

import net.sourceforge.stripes.action.*
import org.apache.shiro.*
import org.hibernate.*
import org.hibernate.criterion.*
import org.joda.time.*

import com.manydesigns.portofino.pageactions.calendar.*

@RequiresPermissions(level = AccessLevel.VIEW)
class CalendarPage extends CalendarAction {

    def calDefs = $calendarDefinitions; //Es. [["Cal 1", "db1.schema1.table1", ["column1", "column2"], Color.RED], ["Cal 2", "db1.schema1.table2", ["column1", "column2"], Color.BLUE]]

    @Override
    void loadObjects(Interval interval) {
        for(cal in calDefs) {
            def qname = DatabaseLogic.splitQualifiedTableName((cal[1]))
            Table table = DatabaseLogic.findTableByName(application.model, qname[0], qname[1], qname[2])
            Session session = application.getSession(table.schema.databaseName)
            def objects = []
            for(col in cal[2]) {
                Criteria criteria = session.createCriteria(table.actualEntityName)
                criteria.add(Restrictions.ge(col, interval.start.toDate()))
                criteria.add(Restrictions.lt(col, interval.end.toDate()))
                objects.add([col, criteria.list()])
            }
            addCalendar(cal, table, objects)
        }
    }

    protected def addCalendar(calDef, Table table, List data) {
        Calendar calendar = new Calendar(calDef[1], calDef[0], calDef[3])
        ClassAccessor classAccessor = new TableAccessor(table)
        for(tuple in data) {
            String property = tuple[0]
            def objects = tuple[1]
            for (object in objects) {
                addEvent(classAccessor, object, property, calendar);
            }
        }
        calendars.add(calendar)
    }

    protected def addEvent(TableAccessor classAccessor, object, String property, Calendar calendar) {
        def name = ShortNameUtils.getName(classAccessor, object)
        def evtInterval
        if(object[property] instanceof java.sql.Date) {
            def eventStart = new DateMidnight(object[property], DateTimeZone.UTC)
            evtInterval = new Interval(eventStart, eventStart.plusDays(1));
        } else {
            def eventStart = new DateTime(object[property], DateTimeZone.UTC)
            evtInterval = new Interval(eventStart, eventStart);
        }
        def prettyProperty = Util.guessToWords(property);
        def event = new Event(
                calendar, object.id + "_" + property + "_" + events.size(),
                name + " - " + prettyProperty,
                evtInterval, null, null);
        events.add(event)
    }

    @Override
    void loadObjects(DateTime instant, int maxEvents) {
        //TODO andrebbero ordinati per giorni....
        def allObjects = []
        for(cal in calDefs) {
            Calendar calendar = new Calendar(cal[1], cal[0], cal[3])
            calendars.add(calendar)
            def qname = DatabaseLogic.splitQualifiedTableName(cal[1]);
            Table table = DatabaseLogic.findTableByName(model, qname[0], qname[1], qname[2])
            Session session = application.getSession(table.schema.databaseName)
            for(col in cal[2]) {
                Criteria criteria = session.createCriteria(table.actualEntityName)
                criteria.add(Restrictions.ge(col, instant.toDate()))
                criteria.setMaxResults(maxEvents)
                criteria.addOrder(Order.asc(col))
                def objects = criteria.list()
                if(!objects.isEmpty()) {
                    allObjects.add([col, calendar, table, objects])
                }
            }
        }
        for(int i = 0; i < maxEvents && !allObjects.isEmpty(); i++) {
            int index = (int) (i % allObjects.size())
            def ccto = allObjects[index]
            def col = ccto[0]
            def calendar = ccto[1]
            def table = ccto[2]
            def objects = ccto[3]
            if(objects.isEmpty()) {
                allObjects.remove(index)
                i--;
            } else {
                ClassAccessor classAccessor = new TableAccessor(table)
                addEvent(classAccessor, objects.remove(0), col, calendar)
            }
        }
    }

}
