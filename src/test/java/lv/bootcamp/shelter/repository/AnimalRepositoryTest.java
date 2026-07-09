package lv.bootcamp.shelter.repository;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Task: Repository tests with @DataJpaTest.
 *
 * Use entityManager.persist() + entityManager.flush() to set up test data.
 * Each test rolls back automatically — no cleanup needed.
 */
@DataJpaTest
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldPersistAnimalAndGenerateId() {
        Animal animal = new Animal(null, "Millo", AnimalType.DOG, "Golden", 2, "Excited", AnimalStatus.AVAILABLE);
        Animal saved = animalRepository.save(animal);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Millo");
    }

    @Test
    void findByStatus_shouldReturnOnlyMatchingAnimals() {

        entityManager.persist(new Animal(null, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE));
        entityManager.persist(new Animal(null, "Karam", AnimalType.CAT, "Street Cat", 2, "Spicy", AnimalStatus.AVAILABLE));
        entityManager.persist(new Animal(null, "T-rex", AnimalType.DOG, "Terrier", 6, "Calm", AnimalStatus.ADOPTED));
        entityManager.flush();

        List<Animal> available = animalRepository.findByStatus(AnimalStatus.AVAILABLE);

        assertThat(available).hasSize(2);
        assertThat(available).extracting("name").containsExactlyInAnyOrder("Rex", "Karam");
    }

    @Test
    void findByType_shouldReturnAnimalsOfGivenType() {

        entityManager.persist(new Animal(null, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE));
        entityManager.persist(new Animal(null, "Luna", AnimalType.CAT, "Norweigan", 2, "Calm", AnimalStatus.AVAILABLE));
        entityManager.flush();

        List<Animal> dogs = animalRepository.findByType(AnimalType.DOG);

        assertThat(dogs).hasSize(1);
        assertThat(dogs.getFirst().getName()).isEqualTo("Rex");
    }

    @Test
    void findByNameContainingIgnoreCase_shouldMatchPartialName() {
        entityManager.persist(new Animal(null, "Rex", AnimalType.DOG, "Labrador", 3, "Friendly", AnimalStatus.AVAILABLE));
        entityManager.persist(new Animal(null, "Rexy Jr", AnimalType.DOG, "Terrier", 1, "Slow", AnimalStatus.AVAILABLE));
        entityManager.persist(new Animal(null, "Mia", AnimalType.CAT, "Siamese", 2, "Calm", AnimalStatus.AVAILABLE));
        entityManager.flush();

        List<Animal> results = animalRepository.findByNameContainingIgnoreCase("rex");

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Rex", "Rexy Jr");
    }
}
