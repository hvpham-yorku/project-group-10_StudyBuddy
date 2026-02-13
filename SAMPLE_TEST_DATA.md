# Sample Event Data for Testing

Use these realistic test events to populate your demo.

## Event 1: EECS 2311 Study Session

```json
{
  "hostId": "vaughn_test_1",
  "title": "EECS 2311 Final Project Collaboration",
  "course": "EECS 2311",
  "location": "Scott Library, Room 301",
  "description": "Working on Sprint 2 of our StudyBuddy project. Bring your laptops and ideas! We'll focus on integrating frontend with backend.",
  "startTime": "2025-02-15T14:00:00",
  "endTime": "2025-02-15T17:00:00",
  "maxCapacity": 8,
  "participantIds": []
}
```

## Event 2: Database Study Group

```json
{
  "hostId": "vaughn_test_1",
  "title": "Database Design Review Session",
  "course": "EECS 3421",
  "location": "Bergeron Centre, 2nd Floor",
  "description": "Reviewing normalization, ER diagrams, and SQL queries before the midterm. Come with questions!",
  "startTime": "2025-02-16T10:00:00",
  "endTime": "2025-02-16T12:00:00",
  "maxCapacity": 6,
  "participantIds": []
}
```

## Event 3: Web Development Workshop

```json
{
  "hostId": "different_user_123",
  "title": "React & REST APIs Workshop",
  "course": "EECS 4413",
  "location": "Lassonde Building, Lab C",
  "description": "Hands-on workshop covering React components, hooks, and API integration. Perfect for beginners!",
  "startTime": "2025-02-17T18:00:00",
  "endTime": "2025-02-17T20:00:00",
  "maxCapacity": 12,
  "participantIds": []
}
```

## Event 4: Algorithms Prep

```json
{
  "hostId": "different_user_123",
  "title": "Algorithm Practice Problems",
  "course": "EECS 2101",
  "location": "Virtual (Zoom link in description)",
  "description": "Zoom Link: https://yorku.zoom.us/j/123456789 - We'll work through sorting algorithms, dynamic programming, and graph traversals.",
  "startTime": "2025-02-18T15:00:00",
  "endTime": "2025-02-18T17:00:00",
  "maxCapacity": 10,
  "participantIds": []
}
```

## Event 5: Physics Problem Set

```json
{
  "hostId": "another_user_456",
  "title": "Physics 2020 Problem Set Review",
  "course": "PHYS 2020",
  "location": "Petrie Science Building, Room 204",
  "description": "Going through this week's problem set together. Focus on electromagnetism and circuits.",
  "startTime": "2025-02-19T13:00:00",
  "endTime": "2025-02-19T15:00:00",
  "maxCapacity": 5,
  "participantIds": []
}
```

---

## Curl Scripts for Quick Testing

### Create All Five Events

```bash
# Event 1
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"hostId":"vaughn_test_1","title":"EECS 2311 Final Project Collaboration","course":"EECS 2311","location":"Scott Library, Room 301","description":"Working on Sprint 2 of our StudyBuddy project. Bring your laptops and ideas!","startTime":"2025-02-15T14:00:00","endTime":"2025-02-15T17:00:00","maxCapacity":8,"participantIds":[]}'

# Event 2
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"hostId":"vaughn_test_1","title":"Database Design Review Session","course":"EECS 3421","location":"Bergeron Centre, 2nd Floor","description":"Reviewing normalization, ER diagrams, and SQL queries before the midterm.","startTime":"2025-02-16T10:00:00","endTime":"2025-02-16T12:00:00","maxCapacity":6,"participantIds":[]}'

# Event 3
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"hostId":"different_user_123","title":"React & REST APIs Workshop","course":"EECS 4413","location":"Lassonde Building, Lab C","description":"Hands-on workshop covering React components, hooks, and API integration.","startTime":"2025-02-17T18:00:00","endTime":"2025-02-17T20:00:00","maxCapacity":12,"participantIds":[]}'

# Event 4
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"hostId":"different_user_123","title":"Algorithm Practice Problems","course":"EECS 2101","location":"Virtual (Zoom link in description)","description":"Working through sorting algorithms and dynamic programming.","startTime":"2025-02-18T15:00:00","endTime":"2025-02-18T17:00:00","maxCapacity":10,"participantIds":[]}'

# Event 5
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -d '{"hostId":"another_user_456","title":"Physics 2020 Problem Set Review","course":"PHYS 2020","location":"Petrie Science Building, Room 204","description":"Going through this weeks problem set together.","startTime":"2025-02-19T13:00:00","endTime":"2025-02-19T15:00:00","maxCapacity":5,"participantIds":[]}'
```

---

## Testing Delete Authorization

**Important:** Notice how events have different `hostId` values. This lets you test the delete authorization:

- Events 1 & 2 have `hostId: "vaughn_test_1"` - You can delete these
- Events 3 & 4 have `hostId: "different_user_123"` - You cannot delete these
- Event 5 has `hostId: "another_user_456"` - You cannot delete this

### Try to Delete Your Own Event (Should Work)
```bash
# Get the eventId from the GET response, then:
curl -X DELETE "http://localhost:8080/api/events/YOUR_EVENT_ID?userId=vaughn_test_1"
# Expected: 204 No Content
```

### Try to Delete Someone Else's Event (Should Fail)
```bash
curl -X DELETE "http://localhost:8080/api/events/THEIR_EVENT_ID?userId=vaughn_test_1"
# Expected: 403 Forbidden
```

This demonstrates your MVP security feature!

---

## Events with Participants (for Future Testing)

Once you implement the "Join Event" feature, you can test with:

```json
{
  "hostId": "vaughn_test_1",
  "title": "Popular Study Session",
  "course": "EECS 2311",
  "location": "Library",
  "description": "This event already has some participants!",
  "startTime": "2025-02-20T14:00:00",
  "endTime": "2025-02-20T16:00:00",
  "maxCapacity": 10,
  "participantIds": ["user_001", "user_002", "user_003"]
}
```

This will show: **Capacity: 3/10** in the frontend!

---

## Date/Time Format Notes

The format `2025-02-15T14:00:00` means:
- `2025-02-15` = February 15, 2025
- `T` = Time separator
- `14:00:00` = 2:00 PM (24-hour format)

### HTML Input Conversion

If Vaughn's form uses `<input type="datetime-local">`, it returns this exact format automatically!

```javascript
// No conversion needed!
const startTime = document.getElementById("startTime").value;
// Already in format: "2025-02-15T14:00:00"
```

---

## Pro Tips for Demo

1. **Create events in advance** - Use the curl commands before demo starts
2. **Mix of courses** - Shows versatility (EECS 2311, 3421, 4413, 2101, PHYS 2020)
3. **Different locations** - Library, Bergeron, Lassonde, Virtual
4. **Varying capacities** - 5, 6, 8, 10, 12 - shows flexibility
5. **Different hosts** - Demonstrates the delete authorization feature

---

## Color Coding for Frontend (Suggestion for Vaughn)

Suggest to Vaughn to color-code cards by course prefix:
- EECS courses → Blue
- PHYS courses → Green  
- MATH courses → Purple
- Other → Gray

This makes the Pinterest grid more visually appealing!
