package ca.yorku.my.StudyBuddy;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a study event that users can create, join, and attend.
 */

public class Event{

    private String eventId;
    private String hostId;
    private String title;
    private String course;
    private String location;
    private String description;
    // Time values are stored as strings to keep Firestore serialization simple and uniform.
    private String startTime;
    private String endTime;
    private int maxCapacity;
    // Participant ids represent enrolled users for this event instance.
    private List<String> participantIds;
    

    // Default constructor required by serialization frameworks.
    public Event(){
        
        participantIds = new ArrayList<>();

    }

    // Convenience constructor for creating a fully populated event object.
 public Event(String hostId, String title, String course, String location, String description,
             String startTime, String endTime, int maxCapacity) {
    this.hostId = hostId;
    this.title = title;
    this.course = course;
    this.location = location;
    this.description = description;
    this.startTime = startTime;
    this.endTime = endTime;
    this.maxCapacity = maxCapacity;
    this.participantIds = new ArrayList<>();
}

    public String getEventId(){
        return this.eventId;   
    }

    public String getHostId(){
        return this.hostId;   
    }

    public String getTitle(){
        return this.title;   
    }

    public String getCourse(){
        return this.course;   
    }

    public String getLocation(){
        return this.location;   
    }

    public String getDescription(){
        return this.description;   
    }

    public String getStartTime(){
        return this.startTime;   
    }

    public String getEndTime(){
        return this.endTime;   
    }

    public int getMaxCapacity(){
        return this.maxCapacity;   
    }

    public List<String> getParticipantIds(){
        return this.participantIds;   
    }

    // Setter Methods for the Event class.

    public void setEventId(String eventId){
        this.eventId = eventId;
    }

    public void setHostId(String hostId){
        this.hostId = hostId;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setCourse(String course){
        this.course = course;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setStartTime(String startTime){
        this.startTime = startTime;
    }

    public void setEndTime(String endTime){
        this.endTime = endTime;
    }

    public void setMaxCapacity(int maxCapacity){
        this.maxCapacity = maxCapacity;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public void removeParticipant(String participantId ){ 

        if(this.participantIds.contains(participantId)){
            this.participantIds.remove(participantId);
        }else{

            // Keep behavior non-throwing when participant is already absent.
            System.out.println("Cannot remove participant. Participant is not in the event.");

        }
    }


    } // End of event class.
