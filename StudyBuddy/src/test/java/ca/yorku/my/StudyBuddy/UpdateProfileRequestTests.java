package ca.yorku.my.StudyBuddy;
import ca.yorku.my.StudyBuddy.dtos.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class UpdateProfileRequestTests {

    @Test
    void constructor_setsFieldsCorrectly() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            List.of("EECS 2311"),                 // courses
            List.of("Quiet"),                     // studyVibes
            Map.of("showBio", true),              // privacySettings
            "Hello world",                        // bio
            "3",                                  // year
            "CS",                                 // program
            "avatarUrl",                          // avatar
            "Toronto",                            // location
            Map.of("latitude", 43.77, "longitude", -79.50), // exactLocation
            true,                                 // twoFAEnabled
            30,                                   // autoTimeout
            true,                                 // isOnline
            Map.of("email", true)                 // notifications
        );

        assertEquals(List.of("EECS 2311"), req.courses());
        assertEquals(List.of("Quiet"), req.studyVibes());
        assertEquals(Map.of("showBio", true), req.privacySettings());
        assertEquals("Hello world", req.bio());
        assertEquals("3", req.year());
        assertEquals("CS", req.program());
        assertEquals("avatarUrl", req.avatar());
        assertEquals("Toronto", req.location());
        assertEquals(Map.of("latitude", 43.77, "longitude", -79.50), req.exactLocation());
        assertEquals(true, req.twoFAEnabled());
        assertEquals(30, req.autoTimeout());
        assertEquals(true, req.isOnline());
        assertEquals(Map.of("email", true), req.notifications());
    }

    @Test
    void emptyRecord_allowsNulls() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            null, null, null, null, null, null,
            null, null, null, null, 0, null, null
        );

        assertNull(req.courses());
        assertNull(req.studyVibes());
        assertNull(req.privacySettings());
        assertNull(req.bio());
        assertNull(req.year());
        assertNull(req.program());
        assertNull(req.avatar());
        assertNull(req.location());
        assertNull(req.exactLocation());
        assertNull(req.twoFAEnabled());
        assertEquals(0, req.autoTimeout());
        assertNull(req.isOnline());
        assertNull(req.notifications());
    }

    @Test
    void constructor_handlesMixedNulls() {
        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            List.of("EECS 2311"),   // courses
            null,                   // studyVibes
            null,                   // privacySettings
            "Bio",                  // bio
            null,                   // year
            "CS",                   // program
            null,                   // avatar
            null,                   // location
            null,                   // exactLocation
            null,                   // twoFAEnabled
            15,                     // autoTimeout
            null,                   // isOnline
            null                    // notifications
        );

        assertEquals(List.of("EECS 2311"), req.courses());
        assertNull(req.studyVibes());
        assertNull(req.privacySettings());
        assertEquals("Bio", req.bio());
        assertNull(req.year());
        assertEquals("CS", req.program());
        assertNull(req.avatar());
        assertNull(req.location());
        assertNull(req.exactLocation());
        assertNull(req.twoFAEnabled());
        assertEquals(15, req.autoTimeout());
        assertNull(req.isOnline());
        assertNull(req.notifications());
    }

    @Test
    void modifyingListAfterConstruction_affectsRecord() {
        List<String> courses = new java.util.ArrayList<>();
        courses.add("EECS 2311");

        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            courses, null, null, null, null, null,
            null, null, null, null, 0, null, null
        );

        courses.add("EECS 3311");

        assertEquals(List.of("EECS 2311", "EECS 3311"), req.courses());
    }

    @Test
    void constructor_handlesExtremeAutoTimeoutValues() {
    	UpdateProfileRequestDTO req1 = new UpdateProfileRequestDTO(
            null, null, null, null, null, null,
            null, null, null, null, Integer.MIN_VALUE, null, null
        );
        assertEquals(Integer.MIN_VALUE, req1.autoTimeout());

        UpdateProfileRequestDTO req2 = new UpdateProfileRequestDTO(
            null, null, null, null, null, null,
            null, null, null, null, Integer.MAX_VALUE, null, null
        );
        assertEquals(Integer.MAX_VALUE, req2.autoTimeout());
    }
    
    @Test
    void constructor_handlesBooleanValues() {
    	UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            null, null, null, null, null, null,
            null, null,
            null,
            true,   // twoFAEnabled
            10,
            false,  // isOnline
            null
        );

        assertTrue(req.twoFAEnabled());
        assertFalse(req.isOnline());
    }

    @Test
    void constructor_handlesMultiplePrivacySettings() {
        Map<String, Boolean> settings = Map.of(
            "showBio", true,
            "showCourses", false
        );

        UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            null, null, settings, null, null, null,
            null, null, null, null, 0, null, null
        );

        assertEquals(settings, req.privacySettings());
    }

    @Test
    void constructor_handlesEmptyCollections() {
    	UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            List.of(), List.of(), Map.of(),
            "", "", "",
            "", "", null, false,
            0, false, Map.of()
        );

        assertTrue(req.courses().isEmpty());
        assertTrue(req.studyVibes().isEmpty());
        assertTrue(req.privacySettings().isEmpty());
        assertEquals("", req.bio());
    }

    @Test
    void constructor_distinguishesNullAndEmptyStrings() {
    	UpdateProfileRequestDTO req = new UpdateProfileRequestDTO(
            null, null, null,
            "",      // empty bio
            null,    // null year
            "",      // empty program
            null, null, null, null, 0, null, null
        );

        assertEquals("", req.bio());
        assertNull(req.year());
        assertEquals("", req.program());
    }
}