# StudyBuddy - Group 10 - Iteration 2 Log

**Members:**
* Sean Lee-Wah - 221344163 - sean1010@my.yorku.ca
* Bhavya Trivedi - 219743590 - Bhavya23@my.yorku.ca
* Aqsa Malik - 221100987 - xsa@my.yorku.ca
* Vaughn Chan - 219846914 - vc8@my.yorku.ca
* Omar Fakousa - 220609426 - omar821@my.yorku.ca
* Yash Bhupta - 219743285 - bhupta01@my.yorku.ca

**External Links:**
* **Jira Board:** https://le-eecs-2311-project.atlassian.net/jira/software/projects/ID/boards/1

* **Deliverable 1 Presentation Slides:** [Insert Link to PDF/Canva]
* **Architecture Diagram (Updated):** [Insert Link]

---

## Meeting Minutes

**Date:** Feb 19, 2026 (81 min)  
**Members:** Bhavya Trivedi, Vaughn Chan, Yash Bhupta, Omar, Sean, Aqsa  

### Summary
We held a planning session for the current iteration of StudyBuddy, focusing on defining our revised architecture, establishing our Firebase database collections, and resetting our workflow for a fresh start. We clarified the Firebase authentication process, emphasizing token verification on the backend, and agreed on a strict branching strategy. To maintain momentum and quickly address blockers, we are transitioning from long weekly meetings to 15-minute check-ins every two days. 

### Key Decisions
* **Workflow:** Implementing a six-branch strategy named by owner to clearly track progress, and shifting to 15-minute stand-ups every two days instead of longer weekly meetings.
* **Database:** Structuring Firebase collections by data type (user tokens, auth, events) and establishing a "stop database" alongside the persistent one. Shared JSON access will be provided to the whole team to reduce individual setup friction.
* **Authentication:** Frontend will manage email/password logins and ID tokens; the backend will verify both the email and token strictly during sign-up to create the user object. JWT tokens will authenticate subsequent API requests.
* **Development Scope:** Prioritizing MVP features (account creation, event management, user interactions) with a strong emphasis on unit testing for this iteration. 

### Task Assignment
* **Yash:**
  * [ ] Implement Firebase authentication, including `@yorku.ca`/`@my.yorku.ca` domain-restricted sign-up, email verification, and base profile creation.
* **Vaughn:**
  * [ ] Develop event management features, allowing users to create, host, join, and cancel study events or their own attendance.
* **Bhavya:**
  * [ ] Build chat and networking features, including post-session connection requests, group messaging with attachments, and typing indicators. *(Due: 2026-02-21)*
  * [ ] Define collections and structure for the Firebase database.
  * [ ] Ask the professor about tagging commits.
* **Sean:**
  * [ ] Create profile management settings, enabling users to update enrolled courses, customize their "Study Vibe", and manage privacy visibility.
* **Omar:**
  * [ ] Build connection tracking, including a dedicated accepted connections page and real-time online/idle/offline status indicators for group members.
* **Aqsa:**
  * [ ] Implement campus map functionalities to display the user's current location, nearby study sessions with details, and directional navigation.
* **Team / Administrative:**
  * [ ] Confirm access to Firebase for all team members and share the setup JSON file (Assignees: Bhavya, Vaughn).
  * [ ] Assign User Flows and share the updated timeline document with the team (Assignee: Team).
  * [ ] Define the "stop database" structure and conduct unit testing (Assignee: Team).
  * [ ] Integrate and prepare the MVP demo for the end of the iteration (Assignee: Team).


**Date:** Feb 28, 2026 (40 min)  
**Members:** Bhavya Trivedi, Omar, Sean, Aqsa Malik  

### Summary
We held a check-in meeting to review our progress on Iteration 2 features, troubleshoot technical blockers, and prepare for our upcoming submission. This was to complement our regular chat progress updates which happen every other day. We confirmed that the Firebase database is online and accessible to the team. Development updates focused on map integration challenges, chat database connections, and user profile data retrieval. We also emphasized the requirement for unit tests and began planning our project presentation, scheduling a dedicated working session for Sunday.

### Key Decisions
* **Database & Architecture:** Firebase is online; user profile data will be stored in Firestore and queried via user IDs. Architecture documentation has been updated to reflect these data flows.
* **Feature Logic:** Direct messaging functionality is actively being debugged. Group chats will strictly enforce the rule requiring users to have completed a study session before gaining access. 
* **Development Standards:** The team agreed to prioritize regular, incremental commits to avoid integration issues and mandated that unit tests must be written for the current iteration.
* **Presentation Planning:** We will pivot some focus toward the e-class submission, with a dedicated presentation prep meeting scheduled for tomorrow (Sunday). 

### Task Assignment
* **Bhavya:**
  * [ ] Follow up on Firebase database access to ensure the whole team is unblocked.
  * [ ] Finalize chat functionality, including fixing the DM bug and enforcing the completed-session rule for group chats.
* **Omar & Sean:**
  * [ ] Coordinate with Yash on integrating frontend authentication features and profile page data retrieval.
* **Aqsa:**
  * [ ] Continue map implementation, specifically tackling Google Maps integration for moving and pin-dropping. 
* **Yash:**
  * [ ] Update the authentication code to align with the new Firestore structure.
* **Vaughn:**
  * [ ] Continue assigned detailed user stories regarding events functionality.  
* **Team:**
  * [ ] Write required unit tests for all assigned features.
  * [ ] Practice and prepare for the final Deliverable 1 Submission.

## Rationale of Changes on Plan and Big Design Decisions




---

## Concerns with the Project or Group Members


---

## Task Assignments and Estimated/Actual Time

### Summary Table (User Stories)


### Developer Tasks per User Story


## Next Steps (Plan for Iteration 3)
