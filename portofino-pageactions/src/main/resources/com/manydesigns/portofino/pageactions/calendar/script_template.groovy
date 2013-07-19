import javax.servlet.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.portofino.*
import com.manydesigns.portofino.buttons.*
import com.manydesigns.portofino.buttons.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.pageactions.*
import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.shiro.*

import net.sourceforge.stripes.action.*
import org.apache.shiro.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.pageactions.calendar.*
import org.joda.time.*
import java.awt.Color

@RequiresPermissions(level = AccessLevel.VIEW)
class MyCalendar extends CalendarAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Example below. Adapt it to your needs.

    @Override
    void loadObjects(Interval interval) {
        def start = interval.getStart()
        loadObjects(start)
    }

    @Override
    void loadObjects(DateTime instant, int maxEvents) {
        loadCalendars();
        for(int i = 0; i < maxEvents / 4; i++) {
            loadEvents(instant.plusDays(i * 42));
        }
        for(Iterator<Event> it = events.iterator(); it.hasNext();) {
            if(it.next().interval.end.minusMillis(1).isBefore(instant)) {
                it.remove();
            }
        }
    }

    def loadObjects(DateTime start) {
        loadCalendars();
        loadEvents(start);
    }

    def loadEvents(DateTime start) {
        Calendar testCal1 = calendars.get(0);
        Calendar testCal2 = calendars.get(1);
        Calendar testCal3 = calendars.get(2);
        Calendar testCal4 = calendars.get(3);
        Calendar testCal5 = calendars.get(4);

        def time = start;

        Interval eventInterval = new Interval(time.plusHours(8).plusMinutes(30), time.plusHours(10));
        Event event = new Event(
                testCal1, "e0", "Visita Medica", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(10), time.plusHours(12));
        event = new Event(
                testCal2, "e1", "Consegna prototipo", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(10), time.plusHours(12));
        event = new Event(
                testCal2, "e2", "Formazione", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(14).plusMinutes(30), time.plusHours(16).plusMinutes(30));
        event = new Event(
                testCal1, "e3", "Riunione", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time.plusHours(10), time.plusHours(12));
        event = new Event(
                testCal2, "e4", "Formazione", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(14).plusMinutes(30), time.plusHours(18));
        event = new Event(
                testCal1, "e5", "Demo", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(14).plusMinutes(30), time.plusHours(18));
        event = new Event(
                testCal2, "e6", "Demo", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time.plusHours(10), time.plusHours(12));
        event = new Event(
                testCal2, "e7", "Formazione", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(15), time.plusHours(18));
        event = new Event(
                testCal4, "e8", "Intervento cliente", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal3, "e9", "Effettuare Bonifico", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time.plusHours(15), time.plusDays(1));
        event = new Event(
                testCal3, "e10", "App.to con Rossi", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(15), time.plusHours(17));
        event = new Event(
                testCal4, "e11", "Installazione", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time.plusHours(14).plusMinutes(30), time.plusHours(18));
        event = new Event(
                testCal1, "e12", "Riunione", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(14).plusMinutes(30), time.plusHours(18));
        event = new Event(
                testCal2, "e13", "Riunione", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(5);

        eventInterval = new Interval(time.plusHours(12).plusMinutes(30), time.plusHours(13).plusMinutes(30));
        event = new Event(
                testCal4, "e14", "Pranzo con Michele", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal5, "e15", "Immacolata concezione", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal3, "e16", "Ferie", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(3);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal2, "e17", "Consegna progetto", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal4, "e18", "Avvio nuovo progetto", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(15), time.plusDays(1));
        event = new Event(
                testCal1, "e19", "Riunione", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(15), time.plusDays(1));
        event = new Event(
                testCal3, "e20", "Riunione", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(11), time.plusDays(1));
        event = new Event(
                testCal3, "e21", "App.to cliente", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time.plusHours(11), time.plusHours(12));
        event = new Event(
                testCal4, "e22", "Matteo Verdi", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(16), time.plusHours(17));
        event = new Event(
                testCal4, "e23", "Discussione bozza", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal1, "e24", "Bugfixing", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(11), time.plusDays(1));
        event = new Event(
                testCal3, "e25", "Banca", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time.plusHours(16), time.plusDays(1));
        event = new Event(
                testCal2, "e26", "Uscita anticipata", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal4, "e23", "Rilascio nuova versione", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal1, "e27", "Seminario", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(3);

        eventInterval = new Interval(time.plusHours(9), time.plusDays(1));
        event = new Event(
                testCal1, "e28", "Chiamare Verdi", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(15), time.plusDays(1));
        event = new Event(
                testCal1, "e29", "Incontro per interviste", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time.plusHours(15), time.plusDays(1));
        event = new Event(
                testCal4, "e30", "Incontro per interviste", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(2);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal3, "e31", "Inviare auguri", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(2);

        eventInterval = new Interval(time.plusHours(20).plusMinutes(30), time.plusDays(1));
        event = new Event(
                testCal3, "e31b", "Cena aziendale", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(2);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal5, "e32", "Natale", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal5, "e33", "S. Stefano", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(4));
        event = new Event(
                testCal3, "e34", "Ferie", eventInterval, null, null);
        events.add(event);

        eventInterval = new Interval(time, time.plusDays(4));
        event = new Event(
                testCal4, "e34", "Ferie", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(3);

        eventInterval = new Interval(time, time.plusDays(7));
        event = new Event(
                testCal2, "e34", "Ferie", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(2);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal5, "e35", "Primo dell'anno", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(1);

        eventInterval = new Interval(time, time.plusDays(4));
        event = new Event(
                testCal1, "e34", "Ferie", eventInterval, null, null);
        events.add(event);

        time = time.plusDays(4);

        eventInterval = new Interval(time, time.plusDays(1));
        event = new Event(
                testCal5, "e36", "Epifania", eventInterval, null, null);
        events.add(event);
    }

    def loadCalendars() {
        Calendar testCal1 = new Calendar("alice", "Alice", new Color(0xe0, 0x40, 0x30));
        Calendar testCal2 = new Calendar("bob", "Bob", Color.GREEN.darker());
        Calendar testCal3 = new Calendar("carlo", "Carlo", new Color(0x40, 0xA0, 0xe0));
        Calendar testCal4 = new Calendar("davide", "Davide", new Color(0xd0, 0xd0, 0x20));
        Calendar testCal5 = new Calendar("festivita", "Festivita", Color.CYAN.darker());
        calendars.add(testCal1);
        calendars.add(testCal2);
        calendars.add(testCal3);
        calendars.add(testCal4);
        calendars.add(testCal5);
    }

}