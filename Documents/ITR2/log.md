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


## Rationale of Changes on Plan and Big Design Decisions




---

## Concerns with the Project or Group Members


---

## Task Assignments and Estimated/Actual Time

### Summary Table (User Stories)


### Developer Tasks per User Story


## Next Steps (Plan for Iteration 3)
