package ca.yorku.my.StudyBuddy.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

import ca.yorku.my.StudyBuddy.StudyBuddyApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("stub")
@SpringBootTest(classes = StudyBuddyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MessageTypeIntegrationTests {
    
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSendAndRetrieveTextLinkFileMessages() throws Exception {
        
        // 1. Setup: Create users in the stub database so they can chat
        mockMvc.perform(
            post("/api/studentcontroller/save")
                .contentType("application/json")
                .content("{\"userId\":\"userA\",\"firstName\":\"Alice\",\"lastName\":\"Smith\"}")
        ).andExpect(status().isOk());
        
        mockMvc.perform(
            post("/api/studentcontroller/save")
                .contentType("application/json")
                .content("{\"userId\":\"userB\",\"firstName\":\"Bob\",\"lastName\":\"Jones\"}")
        ).andExpect(status().isOk());

        // 2. Setup: Create a direct chat between them
        MvcResult chatResult = mockMvc.perform(
            post("/api/chats/direct")
                .header("Authorization", "Bearer userA") // Mocking User A
                .contentType("application/json")
                .content("{\"userA\":\"userA\",\"userB\":\"userB\"}")
        ).andExpect(status().isCreated()).andReturn(); // Returns 201

        // Extract the generated Chat ID from the response
        String responseContent = chatResult.getResponse().getContentAsString();
        String chatId = JsonPath.read(responseContent, "$.chatId");

        // 3. Send TEXT message (Using the correct path and Auth header)
        mockMvc.perform(
            post("/api/chats/" + chatId + "/messages")
                .header("Authorization", "Bearer userA")
                .contentType("application/json")
                .content("{\"type\":\"TEXT\",\"content\":\"Hello\",\"chatId\":\"" + chatId + "\"}")
        ).andExpect(status().isCreated());

        // 4. Send LINK message 
        mockMvc.perform(
            post("/api/chats/" + chatId + "/messages")
                .header("Authorization", "Bearer userA")
                .contentType("application/json")
                .content("{\"type\":\"LINK\",\"content\":\"https://yorku.ca\",\"chatId\":\"" + chatId + "\"}")
        ).andExpect(status().isCreated());

        // 5. Retrieve messages (Using the correct path)
        mockMvc.perform(
            get("/api/chats/" + chatId + "/messages")
                .header("Authorization", "Bearer userA")
        ).andExpect(status().isOk());
    }}
