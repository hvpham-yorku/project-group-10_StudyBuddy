package ca.yorku.my.StudyBuddy.services;

import java.util.List;

import ca.yorku.my.StudyBuddy.services.ConnectionsService.ConnectionDTO;

public interface ConnectionsRepository {
    List<ConnectionsService.ConnectionDTO> getAcceptedConnections(String myUserId) throws Exception;
    List<ConnectionsService.ConnectionDTO> getAvailableStudents(String myUserId) throws Exception;
    List<ConnectionsService.ConnectionDTO> getPendingRequests(String myUserId) throws Exception;
    void sendRequest(String senderId, String receiverId) throws Exception;
    void acceptRequest(String senderId, String myUserId) throws Exception;
    void declineRequest(String senderId, String myUserId) throws Exception;
    void removeConnection(String myUserId, String otherUserId) throws Exception;
	List<ConnectionDTO> getPendingConnections(String myUserId);
	List<ConnectionDTO> getSentRequests(String myUserId);
}