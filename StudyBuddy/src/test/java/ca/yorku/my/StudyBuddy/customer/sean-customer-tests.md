# Customer Tests and Bug Reports (Assigned Stories)
Assigned stories:
1. ID-85 (1.6) View Network List
2. ID-86 (3.8) Online Status
3. ID-97 (4.3) User Reporting System
4. ID-62 (4.6) Secure Database


## Test Environment
- Frontend: App running in browser
- Backend: Spring Boot app on localhost:8080
- Profile: stub (current default in application.properties)
- Browser: Chrome (latest)
- Accounts used:
  - User A: valid YorkU account
  - User B: valid YorkU account
  - User C: valid YorkU account


## Test Data Setup (Preconditions for All Stories)
1. Start backend and frontend.
2. Ensure at least three users exist: User A, User B, User C.
3. Ensure each user has profile data (name, program, at least one course).


---


## ID-85 (1.6) View Network List


### ID85-01 Main flow: view accepted connections list
Preconditions:
- User A is connected with User B (accepted connection exists).
- User A is logged in.


Steps:
1. Open the app and sign in as User A.
2. Navigate to the “Network” page.
3. Stay on the “My Connections” tab.
4. Observe the list of connections.


Expected Results:
- Network page loads successfully.
- User B appears in the “My Connections” list.
- User B card shows name and profile details available in profile data.
- Connection count near page header reflects list size.




### ID85-02: Search filter by name/program/course
Preconditions:
- User A has at least two accepted connections with different names/programs/courses.


Steps:
1. Sign in as User A.
2. Open Network page.
3. In the search box, enter part of User B’s full name.
4. Clear search, enter part of User B program.
5. Clear search, enter a course code that only User B has.


Expected Results:
- Each query narrows the list correctly.
- Matching connection remains visible.
- Non-matching connections are hidden.


### ID85-03: Empty state behavior
Preconditions:
- User A has zero accepted connections.


Steps:
1. Sign in as User A.
2. Open Network page.
3. Stay on My “Connections” tab.


Expected Results:
- UI shows an explicit empty state message.


### ID85-04: Request impacts tabs end-to-end
Preconditions:
- User A and User C are not connected.


Steps:
1. Sign in as User A and open the “Network” page.
2. Go to the “Find People” tab.
3. Send a connection request to User C.
4. Sign out, sign in as User C.
5. Open the “My Network” page and go to the “Requests” tab.
6. Accept User A request.
7. Sign out, sign in again as User A.
8. Open the “My Connections” tab.


Expected Results:
- Request is visible to User C in the “Requests” tab.
- After accepting, User A and User C appear in each other's “My Connections” tab.
- Request no longer appears as pending.


### ID85-05: Remove connection flow
Preconditions:
- User A and User B are accepted connections.


Steps:
1. Sign in as User A.
2. Open “Network” page -> “My Connections”.
3. Click "Remove Connection on User B.
4. Confirm removal in prompt.
5. Refresh page.


Expected Results:
- User B is removed immediately from the list.
- After refresh, User B is still removed.


---


## ID-86 (3.8) Online Status


### ID86-01: Profile online toggle persists to profile view
Preconditions:
- User A is logged in.


Steps:
1. Open Profile page as User A.
2. Set Show as Online toggle to OFF.
3. Click Save Online Status.
4. Open User A profile through the viewer page of User B
5. Repeat with toggle ON and save.


Expected Results:
- Toggle changes are saved and reflected in the profile viewer status chip (Online/Offline).
- If User A sets status at “Online”, should show “Online” for User B, and vice versa for “Offline” 
- Reloading does not reset the chosen value.


### ID86-03: Not signed in access behavior
Preconditions:
- No active auth token (signed out).


Steps:
1. Open the app and try to navigate to the “My Network” route.


Expected Results:
- User is redirected to login.
- Network data is not displayed without authentication.


---


## ID-97 (4.3) User Reporting System


### ID97-01: Submit report with category and details
Preconditions:
- User A is logged in.
- User B profile is accessible at profile viewer page.


Steps:
1. Open User B profile viewer.
2. Click Report User.
3. Select category (for example, Harassment).
4. Enter details.
5. Submit report.


Expected Results:
- Success message is shown.
- Modal closes.
- The report is stored in firestore  



### ID97-02: Validation: category required
Preconditions:
- User A logged in on User B profile viewer.


Steps:
1. Click Report User.
2. Leave category unselected.
3. Enter some details.
4. Click Submit Report.


Expected Results:
- The user sees a validation message asking for a category.
- Request is not submitted.


### ID97-03: Unauthorized report submission blocked
Preconditions:
- Open browser DevTools (Inspect) on profile viewer page.


Steps:
1. Clear token from local storage.
2. Try submitting a report from the window.


Expected Results:
- Submission fails with unauthorized error.
- User receives error feedback.
- No report record is created in firestore.


### ID97-04: Special characters and long details
Preconditions:
- User A logged in and the report window opened on User B profile.


Steps:
1. Choose category "Other".
2. Paste a very long detailed message. 
3. Submit.


Expected Results:
- The app does not crash.
- Either the report is accepted safely, or a clear validation error appears.
- UI remains usable/stable after submission attempts.


---


## ID-62 (4.6) Secure Database


### ID62-01: Cross-user profile update is not possible
Preconditions:
- User A and User B both exist.


Steps:
1. Sign in as User A.
2. Attempt profile update call intended to change User B data (through User B profile).
3. Verify User B profile afterwards.


Expected Results:
- Backend only updates data for authenticated token owner (User A).
- User B data remains unchanged.


### ID62-02: Protected pages are blocked when not signed in
Preconditions:
- User is signed out (no active session).


Steps:
1. Open the app in a new browser tab.
2. Try to navigate directly to /dashboard.
3. Try to navigate directly to /profile.
4. Try to navigate directly to /network.
5. Example is: /profile/{userId}


Expected Results:
- App redirects users to the Login page for each protected route.
- No private profile/network data is shown (e.g. JSON).


### ID62-03: Logging out revokes access to private pages
Preconditions:
- User A is signed in.


Steps:
1. From inside the app, log out.
2. After logout, manually open /profile in the browser address bar.
3. Press browser Back button to revisit a previously opened private page.


Expected Results:
- The user is redirected to the Login page.
- Private content is not accessible after logout.
- Back navigation should not restore full private data view (stays on Login page).


### ID62-04: Invalid/tampered session token is rejected by app
Preconditions:
- User A is signed in.


Steps:
1. Open browser DevTools (Inspect) and change localStorage key studyBuddyToken to an invalid value (for example, "abc.invalid.token").
2. Refresh the app.
3. Try opening /dashboard or /profile.


Expected Results:
- App treats session as invalid and redirects to Login.
- Users cannot continue with protected features.


### ID62-05: Public endpoint should not expose another user's profile without login
Preconditions:
- User A and User B exist.
- User B userId is known (from profile URL, network card, or test data).
- Tester is signed out.


Steps:
1. Open browser while signed out.
2. In the address bar, request /api/studentcontroller/{UserB-userId} directly.
3. Observe response.


Expected Results:
- Requests should be blocked (Unauthorized/Forbidden) because the user is not signed in.
- Personal profile details should not be returned.


### ID62-06: Public endpoint should not expose network data without login
Preconditions:
- User A exists.
- User A userId is known.
- Tester is signed out.


Steps:
1. Open browser while signed out.
2. In the address bar, request /api/connections?userId={UserA-userId}.
3. Observe response body.


Expected Results:
- Requests should be blocked (Unauthorized/Forbidden).
- The connection list should not be visible to unauthenticated users.




---


## Bug Reports


### BUG-ID62-01 (Found from ID62-06)
Reported by: Sean Lee-Wah
Date: March 28, 2026
User Story: ID-62 Secure Database
Severity: Large
Environment: itr3-mvp
Report Type: Coding issue
Can reproduce: Yes


Problem Summary:
Connections and presence endpoints accept userId/uids directly and do not require Authorization token, allowing data access without authenticated identity.


Steps to Reproduce:
1. Start app.
2. Use browser DevTools or Postman.
3. Send GET /api/connections?userId=<existing-user-id> without Authorization.
4. Send GET /api/presence?uids=<existing-user-id> without Authorization.


Expected Result:
- Requests rejected as unauthorized.


Actual Result:
- Endpoints return data based on provided IDs.


Evidence (Screenshots, found in "Issues" on repo page):

On the sign-up/sign-in page and attempt to access connections through my userId by typing into URL.

Result shows the JSON file with private data of my connections





### BUG-ID62-02 (Found from ID62-05)
Reported by: Sean Lee-Wah
Date: March 28, 2026
Title: Student profile data can be fetched without login via direct API URL
User Story: ID-62 Secure Database
Severity: Large
Environment: itr3-mvp
Report Type: Coding issue
Can reproduce: Yes


Problem Summary:
Direct student profile endpoint returns profile data without requiring authentication, allowing signed-out users to read another student's data if userId is known.


Steps to Reproduce:
1. Sign out of the app (e.g. be on the sign up/sign in page).
2. Open browser and request /api/studentcontroller/{existing-user-id}.
3. Observe returned JSON.


Expected Result:
- Endpoint should reject the request as unauthorized.


Actual Result:
- Endpoint returns student profile payload.


Evidence (Screenshots, found in "Issues" on repo page):
On the sign-up/sign-in page and attempt to access student information through my userId by typing into the URL based on the student controller endpoint.



It shows JSON with private information of the user, including things like privacy settings and location.








### BUG-ID85-03 (Found from ID85-02)
Reported by: Sean Lee-Wah
Date: March 28, 2026 
Title: Searching Ineffective when in “Find People” Tab
User Story: ID-85: View Network List
Severity: Medium 
Environment: itr3-mvp
Report Type: Coding issue
Can reproduce: Yes


Problem Summary:
When trying to search for a specific user in the “Find People” tab in “My Network”, the search does not actually filter based on what is typed.


Steps to Reproduce:
1. Run the app.
2. Go to “My Network” tab on the left 
3. Go to the “Find People” tab (right most tab).
4. Attempt to filter by searching based on person’s name 


Expected Result:
- When the user types specific characters, should only show people that correspond/match with what is typed 


Actual Result:
- The people shown do not change and no filtering is actually being applied


Evidence (Screenshots, found in "Issues" on repo page):
I go to “My Network” and then to “Find People” where I can see possible people to connect to.


I then attempt to search for a specific user by typing “Mr”, where only “Mr Test” should be shown (“y b”) is hidden, but instead both are still being shown. 


---


1. Security Bug: Unauthenticated Connection Endpoints (ID-62)

Location: ConnectionsController.java
The getConnections() endpoint accepted userId directly from the client without verifying the caller's identity. Any user could request connections for arbitrary users.


Fix Made:
Added authorization checks to all connection endpoints:
- Extract Authorization header
- Verify token with AuthRepository.verifyFrontendToken()
- Compare caller ID from token with requested userId
- Return 401 UNAUTHORIZED if no header, 403 FORBIDDEN if IDs don't match


Endpoints Secured:
- getConnections() - GET connections list
- getPending() - GET pending requests
- getSent() - GET sent requests
- sendRequest() - POST new connection request
- acceptRequest() - POST accept connection
- declineRequest() - POST decline connection
- removeConnection() - POST remove connection


Explanation:
Connection data is sensitive personal information. Without proper authentication, any user could access another user's connections. The fix ensures only authenticated users requesting their own data can access it. Identity verification happens server-side using JWT tokens, never trusting client-provided userIds.


Commit Message (comment): "Security fix (ID-62): Add authorization checks to all connection endpoints"




---


2. Security Bug: Unauthenticated Presence (ID-62)

Location: PresenceController.java
The getPresence() and heartbeat() endpoints lacked authentication. A person could query who is online/offline and build activity profiles without authorization.


Fix Made:
Added authorization checks to presence endpoints:
- getPresence(): Requires valid auth header before returning presence data
- heartbeat(): Requires auth header and validates caller ID matches userId


Explanation:
User presence data (online/idle/offline) reveals real-time activity patterns. Without authentication, people could discover when users are active and build behavioral profiles. The fix ensures only authenticated users can check presence, and users can only update their own presence status.


Commit Message (comment): "Security fix (ID-62): Add authorization checks to presence endpoints"


---


3. Code Smell: TODO Implementation In Reportuser() (ID-97)

Location: StubStudentRepository.java
The reportUser() method was a TODO stub returning null, making it impossible to test the user reporting system (story ID-97).


Fix Made:
Implemented full reportUser() validation and persistence:
- Validates reporterUserId, reportedUserId, and category are non-empty
- Checks both users exist in the system
- Creates ReportRecord and persists to StubDatabase.REPORTS
- Returns boolean (true for success, false for invalid report)


Also added ReportRecord class and REPORTS collection to StubDatabase.java:
- ReportRecord: Immutable container with reporterUserId, reportedUserId, category, details, createdAt fields
- REPORTS: CopyOnWriteArrayList for thread-safe report storage


Explanation:
TODO stubs meant that it was incomplete work so I filled it in. Without a working implementation, the reporting feature cannot be tested. The fix provides a complete, validated implementation that persists reports for testing. CopyOnWriteArrayList ensures thread-safe concurrent access.


Commit Message (comment): "Feature: Implement reportUser() for user reporting system (ID-97)"


---


4. Code Smell: Null Returns In Connection Service (ID-85)

Location: ConnectionsService.java
getPendingRequests() and getSentRequests() returned null instead of empty lists, forcing callers to do null checks.


Fix Made:
Implemented both methods with proper Firestore queries:
- getPendingRequests(): Queries connections where receiverId==userId and status=="pending"
- getSentRequests(): Queries connections where senderId==userId and status=="pending"
- Both return empty list on error (instead of null)
- Proper exception handling: InterruptedException re-interrupts thread, ExecutionException returns empty list

Explanation:
Returning null is worse than just returning an empty object. An empty list is safer and requires no null checking. The queries fetch the appropriate connection records from Firestore, map to StudentDTO objects, and filter out invalid results.


Commit Message (comment): "Feature: Implement pending/sent request queries (ID-85)"


---


5. Code Smell: Array Initialization Syntax (ID-86)

Location: PresenceService.java
Array initialization used verbose syntax causing compiler warnings: new DocumentReference[docRefs.size()]


Fix Made:
Simplified to modern Java constructor reference syntax: DocumentReference[]::new


Explanation:
The constructor reference is cleaner, more concise, and the JVM automatically sizes the array. It's modern Java style (Java 8+) and removes compiler warnings that occur.


Commit Message (comment): "Refactor: Simplify array initialization in PresenceService"


---


6. Code Smell: Duplicate Code In Studentcontroller (ID-62)

Location: StudentController.java, updateProfile() method
The courses update block was duplicated, executing the same update twice unnecessarily.


Fix Made:
Removed duplicate block, keeping single update to courses.


Explanation:
Duplicate code violates DRY (Don't Repeat Yourself) principle. It is repetitive, unnecessary, and creates risk if one copy gets updated but the other doesn't. For the fix, removed one of the redundant blocks.


Commit Message (comment): "Code smell: Remove duplicate course update in StudentController"



---


7. Code Smell: Debug Logging In Frontend (ID-85/ID-86)


Location: Network.tsx
Temporary debug console.log left in production code: console.log("authReady:", authReady, "uid:", uid)


Fix Made:
Removed the debug logging line.


Explanation:
There were debugs found in console.log which could possibly leak sensitive information (user IDs). They were removed from being shown in console.log.


Commit Message (comment): "Code smell: Remove temporary debug logging from Network.tsx"


---


8. Code Smell: Repeated Fetch Request Code In Frontend (ID-62)


Location: Network.tsx
Multiple fetch calls repeated auth header logic (token retrieval, validation) across functions.


Fix Made:
Created centralized auth and API helpers:
- getAuthHeader(): Retrieves token from localStorage, throws if not authenticated
- apiGet<T>(path): Generic GET wrapper that injects auth headers
- apiPost<T>(path, data): Generic POST wrapper that injects auth headers


All domain functions now use these helpers (e.g., fetchConnections() calls apiGet("/connections")).


Explanation:
Repeated code is hard to maintain. If auth changes (new header format, new token location), must update all copies. Centralized helpers were added to eliminate duplication, ensure consistency, and improve security across all the functions being used.


Commit Message (comment): "Refactor: Centralize API auth and fetch logic (ID-62)"



---


9. Code Bug: Typescript Error: Missing onRemove Prop Type (Network.tsx)


Location: Network.tsx, ConnectionCard component
The component accepted onRemove prop but didn't define it in the TypeScript interface. The IDE couldn't autocomplete and the compiler failed.


Fix Made:
Added proper TypeScript type to ConnectionCardProps interface:
onRemove: (targetId: string) => void | Promise<void>;


This defines that onRemove is a function taking a string (user ID).


Explanation:
Without the prop type, the IDE could not autocomplete or validate usage. The union type (void | Promise<void>) allows both synchronous and asynchronous removal handlers.


Commit Message (comment): "TypeScript: Add missing onRemove prop type to ConnectionCard"


---

Total Commits Made:


1. Code bug: Security fix (ID-62): Add authorization checks to all connection endpoints
2. Code bug: Feature: Implement reportUser() for user reporting system (ID-97)
3. Code smell: Feature: Implement pending/sent request queries (ID-85)
4. Code smell: Remove duplicate course update in StudentController
5. Code smell: Refactor: Centralize API auth and fetch logic (ID-62)
6. Code smell: Refactor: Simplify array initialization in PresenceService
7. Code smell: Remove temporary debug logging from Network.tsx
8. Code smell: Repeated fetch request code in frontend (ID-62)
9. Code bug: TypeScript: Add missing onRemove prop type to ConnectionCard

