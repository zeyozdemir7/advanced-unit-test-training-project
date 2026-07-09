package lv.bootcamp.shelter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.service.AnimalNotFoundException;
import lv.bootcamp.shelter.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Task: REST controller tests with MockMvc and @WebMvcTest.
 *
 * Stub the service with @MockitoBean. Use mockMvc.perform() to make requests
 * and chain .andExpect() calls to verify status, JSON content, and error responses.
 */
@WebMvcTest(AnimalController.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnimalService animalService;

    @Test
    @WithMockUser
    void findAll_shouldReturnListOfAnimals() throws Exception {
        when(animalService.findAll()).thenReturn(List.of(
                new AnimalResponse(1L, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE),
                new AnimalResponse(2L, "Luna", AnimalType.CAT, "Norwegian", 2, "Calm", AnimalStatus.ADOPTED)
        ));

        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Rex"))
                .andExpect(jsonPath("$[1].name").value("Luna"));
    }

    @WithMockUser
    @Test
    void findById_shouldReturn404WhenNotFound() throws Exception {

        when(animalService.findById(99L)).thenThrow(new AnimalNotFoundException(99L));

        mockMvc.perform(get("/api/animals/99"))
                // Assert status 404
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser
    void create_shouldReturn201WithCreatedAnimal() throws Exception {

        when(animalService.create(any())).thenReturn(new AnimalResponse(1L, "Rex", AnimalType.DOG, "Siamese", 3, "", AnimalStatus.AVAILABLE));

        AnimalCreateRequest animalCreateRequest = new AnimalCreateRequest("Rex", AnimalType.DOG, "Labrador", 3, "Friendly");

        mockMvc.perform(post("/api/animals").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rex"))
                .andExpect(jsonPath("$.status").value("AVAILABLE")); }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenNameIsBlank() throws Exception {
        AnimalCreateRequest animalCreateRequest = new AnimalCreateRequest("", AnimalType.DOG, "Labrodor", 3, "Friendly");
        mockMvc.perform(post("/api/animals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void create_shouldReturn400WhenTypeIsNull() throws Exception {
        AnimalCreateRequest animalCreateRequest = new AnimalCreateRequest("", null, "Labrodor", 3, "Friendly");
        mockMvc.perform(post("/api/animals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalCreateRequest)))
                .andExpect(status().isBadRequest());
    }
}
