package com.yumg.starter.modules.resume.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yumg.starter.common.api.ApiResponseAdvice;
import com.yumg.starter.common.web.ClientIpResolver;
import com.yumg.starter.modules.resume.application.ResumeFeedbackContent;
import com.yumg.starter.modules.resume.application.ResumeFeedbackService;
import com.yumg.starter.modules.resume.application.ResumeFeedbackStatus;
import com.yumg.starter.modules.resume.application.ResumeFeedbackType;
import com.yumg.starter.modules.resume.application.ResumeReaction;
import com.yumg.starter.modules.resume.application.ResumeDocumentContent;
import com.yumg.starter.modules.resume.application.ResumeService;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ResumeControllerTest {

    private final ResumeService resume = Mockito.mock(ResumeService.class);
    private final ResumeFeedbackService feedback = Mockito.mock(ResumeFeedbackService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ResumeController(resume, feedback, new ClientIpResolver("")))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setControllerAdvice(new ApiResponseAdvice())
                .build();
    }

    @Test
    void exposesTheCompleteDocumentFromThePublicEndpoint() throws Exception {
        when(resume.publicDocument()).thenReturn(new ResumeDocumentContent("{\"profile\":{}}", 1, 7));

        mvc.perform(get("/api/v1/resume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("{\"profile\":{}}"))
                .andExpect(jsonPath("$.data.schemaVersion").value(1))
                .andExpect(jsonPath("$.data.version").value(7));
    }

    @Test
    void managementEndpointsRequireResumeManagementPermission() throws Exception {
        Method managedGet = ResumeController.class.getMethod("managed");
        Method managedUpdate = ResumeController.class.getMethod("update",
                com.yumg.starter.modules.resume.api.dto.ResumeDocumentRequest.class);

        assertThat(managedGet.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('resume:manage')");
        assertThat(managedUpdate.getAnnotation(PreAuthorize.class).value())
                .isEqualTo("hasAuthority('resume:manage')");
    }

    @Test
    void acceptsAValidatedDocumentUpdate() throws Exception {
        when(resume.update("{\"profile\":{\"name\":\"Alice\",\"contacts\":[]},\"sections\":[{\"id\":\"summary\",\"type\":\"bullet-list\",\"title\":\"简介\",\"items\":[\"Hello\"]}]}", 1, 0))
                .thenReturn(new ResumeDocumentContent("{}", 1, 1));

        mvc.perform(put("/api/v1/resume/manage").contentType("application/json").content("""
                {"content":"{\\"profile\\":{\\"name\\":\\"Alice\\",\\"contacts\\":[]},\\"sections\\":[{\\"id\\":\\"summary\\",\\"type\\":\\"bullet-list\\",\\"title\\":\\"简介\\",\\"items\\":[\\"Hello\\"]}]}","schemaVersion":1,"version":0}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(1));
    }

    @Test
    void acceptsAnAnonymousImmediateReaction() throws Exception {
        when(feedback.submitReaction(ResumeReaction.GREAT, "127.0.0.1", null))
                .thenReturn(new ResumeFeedbackContent("feedback-1", ResumeFeedbackType.REACTION,
                        ResumeReaction.GREAT, null, null, "127.0.0.1", null,
                        ResumeFeedbackStatus.UNREAD, null));
        mvc.perform(post("/api/v1/resume/feedback/reactions")
                .contentType("application/json")
                .content("{\"reaction\":\"GREAT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reaction").value("GREAT"));
    }
}
