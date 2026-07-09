package lv.bootcamp.shelter;

import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.dto.AdoptionRequest;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

// TODO: add imports as you write the test (e.g. assertThat, verify)

/**
 * Task: Integration test with @SpringBootTest.
 *
 * The full application context loads — use @MockitoBean only for the external
 * NotificationClient. Everything else (service, repository, JPA) is real.
 * @Transactional rolls back after each test.
 */
@SpringBootTest
@Transactional
class AdoptionIntegrationTest {

    @Autowired
    private AnimalService animalService;

    @MockitoBean
    private NotificationClient notificationClient;

    @Test
    void adoptionFlow_shouldPersistStatusAndNotifyExternalSystem() {
        AnimalCreateRequest createRequest = new AnimalCreateRequest("Prince", AnimalType.DOG, "Pug", 1, "Sleepy");
        AnimalResponse createdAnimal = animalService.create(createRequest);
        assertThat(createdAnimal.status()).isEqualTo(AnimalStatus.AVAILABLE);

        Long animalId = createdAnimal.id();
        String name = "Jekabs";
        String email = "jekabs@examle.com";

        AdoptionRequest adoptionRequest = new AdoptionRequest(createdAnimal.id(), name, email);

        AnimalResponse adoptedAnimal = animalService.adopt(adoptionRequest);

        assertThat(adoptedAnimal.status()).isEqualTo(AnimalStatus.ADOPTED);

        verify(notificationClient).sendAdoptionNotification(animalId, "Prince", email);

        AnimalResponse fetchedAnimal = animalService.findById(animalId);

        assertThat(fetchedAnimal.status()).isEqualTo(AnimalStatus.ADOPTED);


    }
}
