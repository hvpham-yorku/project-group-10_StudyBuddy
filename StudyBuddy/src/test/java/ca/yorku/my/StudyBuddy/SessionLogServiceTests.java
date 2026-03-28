package ca.yorku.my.StudyBuddy;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.dtos.StudentSessionLogResponseDTO;
import ca.yorku.my.StudyBuddy.services.EventRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

@ExtendWith(MockitoExtension.class)
class SessionLogServiceTests {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private SessionLogService sessionLogService;

    @Test
    void getSessionLogForStudent_includesOnlyPastHostedAndAttendedEvents() throws Exception {
        String studentId = "u1";
        Student student = new Student("u1", "Test", "User");
        student.setAttendedEventIds(List.of("attended-past", "attended-future"));

        Event hostedPast = event("hosted-past", "u1", "2025-01-01", "10:00", 60, "upcoming", List.of());
        Event attendedPast = event("attended-past", "u2", "2025-01-02", "11:00", 90, "upcoming", List.of("u1"));
        Event attendedFuture = event("attended-future", "u2", LocalDate.now().plusDays(2).toString(), "12:00", 45, "upcoming", List.of("u1"));
        Event unrelatedPast = event("unrelated-past", "u2", "2025-01-03", "09:00", 120, "past", List.of("u3"));

        when(studentRepository.getStudent(studentId)).thenReturn(student);
        when(eventRepository.getAllEvents()).thenReturn(List.of(hostedPast, attendedPast, attendedFuture, unrelatedPast));

        StudentSessionLogResponseDTO result = sessionLogService.getSessionLogForStudent(studentId);

        assertEquals(150, result.summary().totalMinutes());
        assertEquals(2, result.summary().totalEvents());
        assertEquals(1, result.summary().hostedCount());
        assertEquals(1, result.summary().attendedCount());
        assertEquals(2, result.events().size());

        // Newest first: attended-past (Jan 2) then hosted-past (Jan 1)
        assertEquals("attended-past", result.events().get(0).id());
        assertEquals("Attended", result.events().get(0).role());
        assertEquals("hosted-past", result.events().get(1).id());
        assertEquals("Hosted", result.events().get(1).role());
    }

    @Test
    void getSessionLogForStudent_usesStatusPastFallbackWhenDateTimeInvalid() throws Exception {
        String studentId = "u1";
        Student student = new Student("u1", "Test", "User");
        student.setAttendedEventIds(List.of("bad-date-past"));

        Event badDatePast = event("bad-date-past", "u2", "not-a-date", "bad-time", 30, "past", List.of("u1"));
        Event badDateUpcoming = event("bad-date-upcoming", "u2", "nope", "also-nope", 30, "upcoming", List.of("u1"));

        when(studentRepository.getStudent(studentId)).thenReturn(student);
        when(eventRepository.getAllEvents()).thenReturn(List.of(badDatePast, badDateUpcoming));

        StudentSessionLogResponseDTO result = sessionLogService.getSessionLogForStudent(studentId);

        assertEquals(1, result.events().size());
        assertEquals("bad-date-past", result.events().get(0).id());
        assertEquals(30, result.summary().totalMinutes());
    }

    @Test
    void getSessionLogForStudent_negativeDurationDoesNotReduceTotals() throws Exception {
        String studentId = "u1";
        Student student = new Student("u1", "Test", "User");
        student.setAttendedEventIds(List.of("neg-duration"));

        Event negativeDurationPast = event("neg-duration", "u2", "2025-01-02", "11:00", -25, "past", List.of("u1"));

        when(studentRepository.getStudent(studentId)).thenReturn(student);
        when(eventRepository.getAllEvents()).thenReturn(List.of(negativeDurationPast));

        StudentSessionLogResponseDTO result = sessionLogService.getSessionLogForStudent(studentId);

        assertEquals(1, result.events().size());
        assertEquals(0, result.summary().totalMinutes());
    }

    @Test
    void addEventToSessionLog_addsOnlyOnce() {
        Student student = new Student("u1", "Test", "User");

        sessionLogService.addEventToSessionLog(student, "e1");
        sessionLogService.addEventToSessionLog(student, "e1");

        assertEquals(1, student.getAttendedEventIds().size());
        assertTrue(student.getAttendedEventIds().contains("e1"));
    }

    @Test
    void filterSessionLogByCourse_returnsOnlyMatchingCourse() {
        Event e1 = event("e1", "u1", "2025-01-01", "10:00", 30, "past", List.of(), "EECS 2311");
        Event e2 = event("e2", "u1", "2025-01-01", "10:00", 30, "past", List.of(), "MATH 1013");
        Event e3 = event("e3", "u1", "2025-01-01", "10:00", 30, "past", List.of(), null);

        List<Event> filtered = sessionLogService.filterSessionLogByCourse(List.of(e1, e2, e3), "EECS 2311");

        assertEquals(1, filtered.size());
        assertEquals("e1", filtered.get(0).getId());
    }

    private Event event(String id, String host, String date, String time, int duration, String status, List<String> attendees) {
        return event(id, host, date, time, duration, status, attendees, "EECS 2311");
    }

    private Event event(String id, String host, String date, String time, int duration, String status, List<String> attendees, String course) {
        Event event = new Event();
        event.setId(id);
        event.setHost(host);
        event.setDate(date);
        event.setTime(time);
        event.setDuration(duration);
        event.setStatus(status);
        event.setAttendees(attendees);
        event.setCourse(course);
        event.setTitle("Test Event " + id);
        event.setLocation("Scott Library");
        return event;
    }
}
