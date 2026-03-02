package ca.yorku.my.StudyBuddy.unit;

import ca.yorku.my.StudyBuddy.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadController.class)
class UploadControllerUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private UploadService uploadService;

    @Test
    void uploadAttachmentReturns201() throws Exception {
        when(chatService.extractActorId("Bearer u1")).thenReturn("u1");

        FileAttachmentDTO response = new FileAttachmentDTO();
        response.setFileName("notes.pdf");
        response.setFileSizeBytes(1234L);
        response.setMimeType("application/pdf");
        response.setStoragePath("/api/uploads/files/u1/123_notes.pdf");

        when(uploadService.uploadAttachment(any(), eq("u1"))).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "sample".getBytes());

        mockMvc.perform(multipart("/api/uploads")
                        .file(file)
                        .header("Authorization", "Bearer u1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("notes.pdf"));
    }

    @Test
    void uploadAttachmentWithInvalidFileReturns422() throws Exception {
        when(chatService.extractActorId("Bearer u1")).thenReturn("u1");
        when(uploadService.uploadAttachment(any(), eq("u1")))
                .thenThrow(new ValidationException("file is required"));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[0]);

        mockMvc.perform(multipart("/api/uploads")
                        .file(file)
                        .header("Authorization", "Bearer u1"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void downloadAttachmentReturns200() throws Exception {
        UploadService.DownloadedFile downloadedFile = new UploadService.DownloadedFile(
                new ByteArrayResource("abc".getBytes()),
                MediaType.APPLICATION_PDF_VALUE,
                "notes.pdf");

        when(uploadService.loadAttachment("u1", "123_notes.pdf", "notes.pdf")).thenReturn(downloadedFile);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/uploads/files/u1/123_notes.pdf")
                        .param("downloadName", "notes.pdf"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Content-Disposition", org.hamcrest.Matchers.containsString("notes.pdf")));
    }

    @Test
    void downloadAttachmentMissingReturns404() throws Exception {
        when(uploadService.loadAttachment("u1", "missing.pdf", null))
                .thenThrow(new NotFoundException("File not found"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/uploads/files/u1/missing.pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("File not found"));
    }
}
