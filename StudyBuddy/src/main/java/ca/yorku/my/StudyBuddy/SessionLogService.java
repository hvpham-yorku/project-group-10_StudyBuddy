package ca.yorku.my.StudyBuddy;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.dtos.StudentSessionLogEventDTO;
import ca.yorku.my.StudyBuddy.dtos.StudentSessionLogResponseDTO;
import ca.yorku.my.StudyBuddy.dtos.StudentSessionLogSummaryDTO;
import ca.yorku.my.StudyBuddy.services.EventRepository;
import ca.yorku.my.StudyBuddy.services.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * This class manages student session-log operations such as retrieval,
 * filtering, and total study time calculation.
 */
@Service
public class SessionLogService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StudentRepository studentRepository;


    public StudentSessionLogResponseDTO getSessionLogForStudent(String studentId) throws Exception {
        Student student = studentRepository.getStudent(studentId);
        List<Event> events = eventRepository.getAllEvents();

        Set<String> attendedEventIds = student.getAttendedEventIds() != null
                ? new HashSet<>(student.getAttendedEventIds())
                : new HashSet<>();

        int totalMinutes = 0;
        int hostedCount = 0;
        int attendedCount = 0;
        Set<String> uniqueEventIds = new HashSet<>();
        List<StudentSessionLogEventDTO> sessionEvents = new ArrayList<>();

        for (Event event : events) {
            if (event == null || event.getId() == null) {
                continue;
            }

            boolean hosted = studentId.equals(event.getHost());
            boolean attended = attendedEventIds.contains(event.getId())
                    || (event.getAttendees() != null && event.getAttendees().contains(studentId));

            if (!hosted && !attended) {
                continue;
            }

            if (!isPastEvent(event)) {
                continue;
            }

            if (hosted) {
                hostedCount++;
            }
            if (attended) {
                attendedCount++;
            }

            uniqueEventIds.add(event.getId());
            totalMinutes += Math.max(event.getDuration(), 0);

            String role;
            if (hosted && attended) {
                role = "Hosted & Attended";
            } else if (hosted) {
                role = "Hosted";
            } else {
                role = "Attended";
            }

            sessionEvents.add(new StudentSessionLogEventDTO(
                    event.getId(),
                    event.getTitle(),
                    event.getCourse(),
                    event.getLocation(),
                    event.getDate(),
                    event.getTime(),
                    event.getDuration(),
                    event.getStatus(),
                    role));
        }

        sessionEvents.sort(Comparator.comparing(this::startDateTimeFromDTO, Comparator.nullsLast(Comparator.reverseOrder())));

        StudentSessionLogSummaryDTO summary = new StudentSessionLogSummaryDTO(
                totalMinutes,
                uniqueEventIds.size(),
                hostedCount,
                attendedCount);

        return new StudentSessionLogResponseDTO(summary, sessionEvents);
    }

    /**
     * Adds an event to a student's session log, if they attended the event. 
     * This updates the student's attendedEventIds list.
     */
    public void addEventToSessionLog(Student student, String eventId) {
        student.addAttendedEvent(eventId);
    }

    /**
     * Filters the student's session log by course code 
     */
    public List<Event> filterSessionLogByCourse(List<Event> sessionLog, String courseCode) {
        List<Event> filtered = new ArrayList<>();
        for (Event event : sessionLog) {
            if (event.getCourse() != null && event.getCourse().equals(courseCode)) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    private boolean isPastEvent(Event event) {
        LocalDateTime start = toStartDateTime(event);
        if (start != null) {
            LocalDateTime end = start.plusMinutes(Math.max(event.getDuration(), 0));
            return end.isBefore(LocalDateTime.now());
        }

        return "past".equalsIgnoreCase(event.getStatus());
    }

    private LocalDateTime startDateTimeFromDTO(StudentSessionLogEventDTO event) {
        if (event == null || event.date() == null || event.time() == null) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(event.date(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime time = LocalTime.parse(event.time(), DateTimeFormatter.ofPattern("H:mm"));
            return LocalDateTime.of(date, time);
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime toStartDateTime(Event event) {
        if (event == null || event.getDate() == null || event.getTime() == null) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(event.getDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime time = LocalTime.parse(event.getTime(), DateTimeFormatter.ofPattern("H:mm"));
            return LocalDateTime.of(date, time);
        } catch (Exception ex) {
            return null;
        }
    }
}
