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
- UI shows an explicit empty state message (for example, No connections found).
- No stale cards from the previous session are shown.


### ID85-04: Request workflow impacts tabs end-to-end
Preconditions:
- User A and User C are not connected.


Steps:
1. Sign in as User A and open the “Network” page.
2. Go to the “Find People” tab.
3. Send a connection request to User C.
4. Sign out, sign in as User C.
5. Open the “Network” page and go to the “Requests” tab.
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
3. Click Remove on User B card.
4. Confirm removal in prompt.
5. Refresh page.


Expected Results:
- User B is removed immediately from the list.
- After refresh, User B is still removed.


---


## ID-86 (3.8) Online Status


### ID86-01: Presence updates while active on Network page
Preconditions:
- User A and User B are accepted connections.
- User A and User B both have active sessions in separate browsers.


Steps:
1. Keep User B active on the “Network” page.
2. Sign in as User A and open the “Network” page.
3. Wait up to 15 seconds.


Expected Results:
- User B appears with the “Online” indicator in User A list.
- User B is grouped in the “Online” section.


### ID86-02: Profile online toggle persists to profile view
Preconditions:
- User A is logged in.


Steps:
1. Open Profile page as User A.
2. Set Show as Online toggle to OFF.
3. Click Save Online Status.
4. Open User A profile through the viewer page (for example, from Network list as another user).
5. Repeat with toggle ON and save.


Expected Results:
- Toggle changes are saved and reflected in the profile viewer status chip (Online/Offline).
- Reloading does not reset the chosen value.


### ID86-03: Not signed in access behavior
Preconditions:
- No active auth token (signed out).


Steps:
1. Open the app and try to navigate to the “Network” route.


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
- Form fields are reset on the next open.
- Backend accepts requests (HTTP 200).


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
- Open browser DevTools on profile viewer page.


Steps:
1. Clear studyBuddy token from local storage.
2. Try submitting a report from the modal.


Expected Results:
- Submission fails with unauthorized style error.
- User receives error feedback.
- No report record is created.


### ID97-04: Special characters and long details
Preconditions:
- User A logged in and the report window opened on User B profile.


Steps:
1. Choose category Other.
2. Paste a long detailed message including punctuation and special symbols.
3. Submit.


Expected Results:
- The app does not crash.
- Either the report is accepted safely, or a clear validation error appears.
- UI remains usable after submission attempts.


---


## ID-62 (4.6) Secure Database


### ID62-01: Cross-user profile update is not possible
Preconditions:
- User A and User B both exist.


Steps:
1. Sign in as User A.
2. Attempt profile update call intended to change User B data.
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


Expected Results:
- App redirects users to the Login page for each protected route.
- No private profile/network data is shown.


### ID62-03: Logging out revokes access to private pages
Preconditions:
- User A is signed in.


Steps:
1. From inside the app, log out.
2. After logout, manually open /profile in the browser address bar.
3. Manually open /dashboard in the browser address bar.
4. Press browser Back button to revisit a previously opened private page.


Expected Results:
- The user is redirected to the Login page.
- Private content is not accessible after logout.
- Back navigation should not restore full private data view.


### ID62-04: Invalid/tampered session token is rejected by app
Preconditions:
- User A is signed in.


Steps:
1. Open browser DevTools and change localStorage key studyBuddyToken to an invalid value (for example, "abc.invalid.token").
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


Evidence (Screenshots):














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


Evidence:
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


Evidence:
I go to “My Network” and then to “Find People” where I can see possible people to connect to.


I then attempt to search for a specific user by typing “Mr”, where only “Mr Test” should be shown (“y b”) is hidden, but instead both are still being shown. 


---


## Code Review (Assigned Stories): Bugs and Code Smells

### Scope and Method
Reviewed code paths tied to the four assigned stories:
- ID-85: `Network.tsx`, `ConnectionsController`, `ConnectionsService`
- ID-86: `Network.tsx`, `PresenceController`, `PresenceService`, profile online toggle save path
- ID-97: `ProfileViewer.tsx`, `StudentController` report endpoint, `StubStudentRepository`
- ID-62: token verification flow + data exposure routes (`/api/connections`, `/api/presence`)

Code-smell references used:
- SourceMaking refactoring smell catalog
- Refactoring.Guru smell catalog
- PragmaticWays “31 code smells”

### Code Smells Checked
1. Duplicate Code
2. Dead Code / TODO no-op implementations
3. Temporary Debug Code
4. Primitive Obsession (stringly-typed request payloads)
5. Divergent Change / Interface-implementation drift
6. Long Method
7. Data Clumps
8. Inappropriate Intimacy (trusting client-provided identity data)

Detected from the list above:
- Duplicate Code: **Detected**
- Dead Code / TODO no-op: **Detected**
- Temporary Debug Code: **Detected**
- Primitive Obsession: **Detected**
- Divergent Change / Interface drift: **Detected**
- Inappropriate Intimacy (security design smell): **Detected**
- Long Method: Not prioritized for this iteration
- Data Clumps: Not prioritized for this iteration


## GitHub Issue Drafts (Code-Level Findings)

### BUG-CR-ID62-01
Reported by: Sean Lee-Wah  
Date: March 28, 2026  
Title: Connections endpoints trusted client-supplied user identity  
User Story: ID-62 Secure Database / ID-85 View Network List  
Severity: High  
Environment: itr3-mvp  
Report Type: Coding issue  
Can reproduce: Yes

Problem Summary:
`/api/connections` endpoints accepted `userId`/`myUserId` directly from client input without validating against authenticated token identity.

Expected Result:
- Backend derives caller identity from verified token and blocks identity mismatch.

Actual Result:
- Client-provided IDs could be used to request or mutate another user's connection data.

Fix Applied:
- Added token verification to `ConnectionsController` and enforced caller-ID match before serving data or mutating state.


### BUG-CR-ID62-02
Reported by: Sean Lee-Wah  
Date: March 28, 2026  
Title: Presence endpoints exposed data without authenticated access  
User Story: ID-62 Secure Database / ID-86 Online Status  
Severity: High  
Environment: itr3-mvp  
Report Type: Coding issue  
Can reproduce: Yes

Problem Summary:
`/api/presence` heartbeat/read endpoints did not require token verification.

Expected Result:
- Presence APIs require Authorization and reject caller/target mismatches.

Actual Result:
- Unauthenticated callers could access presence information by user IDs.

Fix Applied:
- Added Authorization checks in `PresenceController`; heartbeat now uses verified token identity.


### SMELL-CR-ID97-01
Reported by: Sean Lee-Wah  
Date: March 28, 2026  
Title: Report workflow used TODO no-op persistence in stub repository  
User Story: ID-97 User Reporting System  
Severity: Medium  
Environment: stub profile  
Report Type: Code smell/design smell

Smell Category:
- Dead Code / Incomplete Implementation (TODO no-op)

Problem Summary:
`StubStudentRepository.reportUser(...)` was a TODO no-op, violating expected behavior and creating false confidence in report submission flow.

Fix Applied:
- Implemented in-memory report persistence (`StubDatabase.REPORTS`) with basic validation.


### SMELL-CR-ID85-01
Reported by: Sean Lee-Wah  
Date: March 28, 2026  
Title: Temporary debug logging and repeated request logic in network page  
User Story: ID-85 View Network List / ID-86 Online Status  
Severity: Low  
Environment: frontend  
Report Type: Code smell/design smell

Smell Category:
- Temporary Debug Code
- Primitive Obsession / duplicated request setup

Problem Summary:
`Network.tsx` contained debug `console.log` and repeated direct fetch blocks without centralized authenticated request handling.

Fix Applied:
- Removed debug log.
- Centralized auth-aware `apiGet`/`apiPost` usage for network/presence requests.


### SMELL-CR-ID62-03
Reported by: Sean Lee-Wah  
Date: March 28, 2026  
Title: Duplicate profile-update branch in controller  
User Story: ID-62 Secure Database (profile update path)  
Severity: Low  
Environment: backend  
Report Type: Code smell/design smell

Smell Category:
- Duplicate Code

Problem Summary:
`StudentController.updateProfile(...)` had duplicate `req.courses()` update block.

Fix Applied:
- Removed duplicate block to keep single write path.


### SMELL-CR-ID85-02
Reported by: Sean Lee-Wah  
Date: March 28, 2026  
Title: Interface methods left as TODO return-null in connections service  
User Story: ID-85 View Network List  
Severity: Medium  
Environment: backend firestore profile  
Report Type: Code smell/design smell

Smell Category:
- Dead Code / Interface-implementation drift

Problem Summary:
`ConnectionsService.getPendingConnections(...)` and `getSentRequests(...)` used TODO/null placeholder behavior.

Fix Applied:
- Implemented both methods with safe return behavior and Firestore-backed sent-request retrieval.


## Summary of Implemented Smell Fixes
Implemented in this iteration:
1. Duplicate code removal in `StudentController.updateProfile`.
2. TODO no-op replacement in `StubStudentRepository.reportUser` with in-memory persistence.
3. Temporary debug code removal and request-handling cleanup in `Network.tsx`.
4. TODO/null interface implementation fixes in `ConnectionsService`.

Security bug hardening also implemented:
1. Auth enforcement and identity checks in `ConnectionsController`.
2. Auth enforcement in `PresenceController`.

Note for GitHub submission:
- Use the issue drafts above as the issue body format.
- Paste one issue per finding (or group by story if your instructor requests grouped reporting).

