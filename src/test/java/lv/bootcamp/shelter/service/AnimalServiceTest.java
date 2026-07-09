package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.dto.AdoptionRequest;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// TODO: add any imports you need as you write the tests

/**
 * Task: Service-layer tests with Mockito.
 *
 * Use @Mock, @InjectMocks, stubbing, verify(), and ArgumentCaptor.
 * Write Arrange-Act-Assert for each method.
 */
@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AnimalService animalService;

    @Captor
    private ArgumentCaptor<List<Long>> idsCaptor;

    @Test
    void create_shouldSaveAnimalWithAvailableStatus() {
        AnimalCreateRequest request = new AnimalCreateRequest("Rex", AnimalType.DOG, "Labrador", 3, "Friendly");

        Animal savedAnimal = new Animal(1L, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE);
        when(animalRepository.save(any(Animal.class))).thenReturn(savedAnimal);

        var response = animalService.create(request);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Rex");
        assertThat(response.status()).isEqualTo(AnimalStatus.AVAILABLE);
        ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository).save(animalCaptor.capture());
        assertThat(animalCaptor.getValue().getStatus()).isEqualTo(AnimalStatus.AVAILABLE);
    }

    @Test
    void findById_shouldThrowWhenAnimalNotFound() {

        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.findById(99L))
                .isInstanceOf(AnimalNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void adopt_shouldChangeStatusAndSendNotification() {

        Animal animal = new Animal(1L, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE);
        AdoptionRequest request = new AdoptionRequest(1L, "John", "john@example.com");

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(animalRepository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        AnimalResponse response = animalService.adopt(request);

        assertThat(response.status()).isEqualTo(AnimalStatus.ADOPTED);
        verify(notificationClient).sendAdoptionNotification(1L, "Rex", "john@example.com");
    }

    @Test
    void adopt_shouldThrowWhenAnimalAlreadyAdopted() {

        Animal animal = new Animal(1L, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.ADOPTED);

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalService.adopt(new AdoptionRequest(1L, "John", "john@example.com")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");

        verifyNoInteractions(notificationClient);

    }

    @Test
    void reserveMultiple_shouldNotifyWithReservedIds() {

        Animal animal1 = new Animal(1L, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE);
        Animal animal2 = new Animal(2L, "Luna", AnimalType.CAT, "Norwegian", 2, "Calm", AnimalStatus.AVAILABLE);

        when(animalRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(animal1, animal2));
        when(animalRepository.save(any(Animal.class))).thenAnswer(inv -> inv.getArgument(0));

        List<AnimalResponse> responses = animalService.reserveMultiple(List.of(1L, 2L));

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).status()).isEqualTo(AnimalStatus.RESERVED);
        assertThat(responses.get(1).status()).isEqualTo(AnimalStatus.RESERVED);


        verify(notificationClient).sendBulkStatusNotification(idsCaptor.capture(), eq("RESERVED"));
        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L);
    }
}
