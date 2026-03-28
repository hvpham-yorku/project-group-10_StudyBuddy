package ca.yorku.my.StudyBuddy.mappers;

import ca.yorku.my.StudyBuddy.classes.Event;
import ca.yorku.my.StudyBuddy.classes.Student;
import ca.yorku.my.StudyBuddy.dtos.EventResponseDTO;
import ca.yorku.my.StudyBuddy.dtos.HostDTO;
import ca.yorku.my.StudyBuddy.services.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventMapper {

    @Autowired
    private StudentRepository studentService;

    /**
     * Converts an internal Event database entity into a safe Frontend DTO,
     * calculating dynamic fields like status and attendee visibility.
     */
    public EventResponseDTO toResponseDTO(Event event, String requesterId) throws Exception {
        
        // 1. Map the Host
        Student hostStudent = studentService.getStudent(event.getHost());
        HostDTO hostDTO = new HostDTO(
            hostStudent.getUserId(),
            hostStudent.getFullName(),
            hostStudent.getAvatar()
        );

        // 2. Calculate Attendee Visibility
        List<String> rawAttendees = event.getAttendees() != null ? event.getAttendees() : new ArrayList<>();
        int attendeeCount = rawAttendees.size();
        
        boolean isParticipating = requesterId != null && 
            (requesterId.equals(event.getHost()) || rawAttendees.contains(requesterId));

        List<String> visibleAttendees = isParticipating ? rawAttendees : new ArrayList<>();

        // 3. Calculate "Upcoming" vs "Past" status
        String serverToday = java.time.LocalDate.now().toString();
        String calculatedStatus = "upcoming";
        if (event.getDate() != null && event.getDate().compareTo(serverToday) < 0) {
            calculatedStatus = "past";
        }

        // 4. Build and return the final DTO
        return new EventResponseDTO(
            event.getId(),
            event.getTitle(),
            event.getCourse(),
            hostDTO,
            event.getLocation(),
            event.getDate(),
            event.getTime(),
            event.getDuration(),
            event.getDescription(),
            event.getMaxParticipants(),
            attendeeCount,
            visibleAttendees,
            event.getTags() != null ? event.getTags() : new ArrayList<>(),
            calculatedStatus,
            event.getReviews() != null ? event.getReviews() : new ArrayList<>()
        );
    }
}
