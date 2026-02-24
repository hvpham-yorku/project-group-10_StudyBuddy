package ca.yorku.my.StudyBuddy;
import java.util.ArrayList;
import java.util.List;

// This class represents an event that students can create and join.

public class Event{

    // Variable Declaration for the event class
    private String eventId;
    private String hostId;
    private String title;
    private String course;
    private String location;
    private String description;
    // Keep time as String for easier comparison, sorting, and serialization with Firestore. We can use a specific format like "yyyy-MM-dd HH:mm" for consistency.
    private String startTime;
    private String endTime;
    private int maxCapacity;
    // Used List<String> to store participantIds for easier management of participants for each event.
    private List<String> participantIds;
    

    // Default constructor for the Event class. Used for Firebase SDK.
    public Event(){
        
        participantIds = new ArrayList<>();

    }

    // Constructor for the Event class
 public Event(String hostId, String title, String course, String location, String description, String startTime, String endTime, int maxCapacity) {
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

            // Handle the case where the participant is not in the event. We can just print a message for now.
            System.out.println("Cannot remove participant. Participant is not in the event.");

        }
    }


    } // End of event class.
