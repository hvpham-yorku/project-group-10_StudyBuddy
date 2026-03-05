package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.Event;
import ca.yorku.my.StudyBuddy.SessionLogService;
import ca.yorku.my.StudyBuddy.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for SessionLogService business rules and time aggregation logic.
 */
class SessionLogServiceUnitTests {

    private final SessionLogService sessionLogService = new SessionLogService();

    /**
     * Verifies adding the same event twice does not create duplicate attendance entries.
     */
    @Test
    void addEventToSessionLogAddsUniqueEventId() {
        Student student = new Student("u1", "Test", "User", new String[]{});

        sessionLogService.addEventToSessionLog(student, "event-1");
        sessionLogService.addEventToSessionLog(student, "event-1");

        assertEquals(1, student.getAttendedEventIds().size());
        assertEquals("event-1", student.getAttendedEventIds().get(0));
    }

    /**
     * Verifies course filtering returns only events that match the requested course code.
     */
    @Test
    void filterSessionLogByCourseReturnsOnlyMatchingCourse() {
        Event eventA = new Event("h1", "A", "EECS2311", "L1", "d", "2026-03-01T10:00:00Z", "2026-03-01T11:00:00Z", 5);
        Event eventB = new Event("h2", "B", "EECS3311", "L2", "d", "2026-03-01T12:00:00Z", "2026-03-01T13:00:00Z", 5);

        List<Event> filtered = sessionLogService.filterSessionLogByCourse(List.of(eventA, eventB), "EECS2311");

        assertEquals(1, filtered.size());
        assertEquals("EECS2311", filtered.get(0).getCourse());
    }

    /**
     * Verifies total-minute calculation supports multiple timestamp formats and skips invalid rows.
     */
    @Test
    void getTotalStudyMinutesSumsAcrossParseableFormatsAndSkipsInvalid() {
        Event isoEvent = new Event("h1", "ISO", "EECS", "L1", "d", "2026-03-01T10:00:00Z", "2026-03-01T11:30:00Z", 5);
        Event offsetEvent = new Event("h2", "OFFSET", "EECS", "L2", "d", "2026-03-01T12:00:00-05:00", "2026-03-01T13:00:00-05:00", 5);
        Event invalidEvent = new Event("h3", "BAD", "EECS", "L3", "d", "not-a-time", "also-bad", 5);

        long totalMinutes = sessionLogService.getTotalStudyMinutes(List.of(isoEvent, offsetEvent, invalidEvent));

        assertEquals(150, totalMinutes);
    }

    /**
     * Verifies empty logs produce zero total study minutes.
     */
    @Test
    void getTotalStudyMinutesReturnsZeroForEmptyLog() {
        assertTrue(sessionLogService.getTotalStudyMinutes(List.of()) == 0);
    }
}
