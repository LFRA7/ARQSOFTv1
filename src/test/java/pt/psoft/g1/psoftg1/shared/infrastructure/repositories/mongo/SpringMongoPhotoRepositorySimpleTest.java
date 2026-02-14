package pt.psoft.g1.psoftg1.shared.infrastructure.repositories.mongo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.test.context.ActiveProfiles;
import pt.psoft.g1.psoftg1.configuration.MongoTestConfig;
import pt.psoft.g1.psoftg1.shared.model.Photo;
import pt.psoft.g1.psoftg1.shared.services.IdGenerationService;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("mongotest")
@Import({MongoTestConfig.class, IdGenerationService.class})
class SpringMongoPhotoRepositorySimpleTest {

    @Autowired
    private SpringMongoPhotoRepository repository;

    @Autowired
    private IdGenerationService idGenerationService;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    private Photo saveToMongo(Photo photo) {
        try {
            Field pkField = Photo.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            long currentId = pkField.getLong(photo);
            if (currentId == 0) {
                pkField.setLong(photo, idGenerationService.generateId());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set ID for Photo", e);
        }
        return ((MongoRepository<Photo, Long>) repository).save(photo);
    }

    @Test
    void testSavePhoto() {
        Path photoPath = Paths.get("/uploads/test-photo.jpg");
        Photo photo = new Photo(photoPath);

        Photo saved = saveToMongo(photo);

        assertNotNull(saved);
        assertEquals("/uploads/test-photo.jpg", saved.getPhotoFile());
    }

    @Test
    void testSaveMultiplePhotos() {
        Photo photo1 = new Photo(Paths.get("/uploads/photo1.jpg"));
        Photo photo2 = new Photo(Paths.get("/uploads/photo2.png"));
        Photo photo3 = new Photo(Paths.get("/uploads/photo3.gif"));

        saveToMongo(photo1);
        saveToMongo(photo2);
        saveToMongo(photo3);

        assertEquals(3, repository.count());
    }

    @Test
    void testDeleteByPhotoFile() {
        Path photoPath = Paths.get("/uploads/delete-me.jpg");
        Photo photo = new Photo(photoPath);
        saveToMongo(photo);

        repository.deleteByPhotoFile("/uploads/delete-me.jpg");

        assertEquals(0, repository.count());
    }

    @Test
    void testDeleteById() {
        Photo photo = new Photo(Paths.get("/uploads/delete-by-id.jpg"));
        Photo saved = saveToMongo(photo);

        try {
            Field pkField = Photo.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            long id = pkField.getLong(saved);

            ((MongoRepository<Photo, Long>) repository).deleteById(id);

            Optional<Photo> found = ((MongoRepository<Photo, Long>) repository).findById(id);
            assertFalse(found.isPresent());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testDeleteAll() {
        Photo photo1 = new Photo(Paths.get("/uploads/delete-all-1.jpg"));
        Photo photo2 = new Photo(Paths.get("/uploads/delete-all-2.jpg"));

        saveToMongo(photo1);
        saveToMongo(photo2);

        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    void testFindById() {
        Photo photo = new Photo(Paths.get("/uploads/find-me.jpg"));
        Photo saved = saveToMongo(photo);

        try {
            Field pkField = Photo.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            long id = pkField.getLong(saved);

            Optional<Photo> found = ((MongoRepository<Photo, Long>) repository).findById(id);

            assertTrue(found.isPresent());
            assertEquals("/uploads/find-me.jpg", found.get().getPhotoFile());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testCountPhotos() {
        Photo photo1 = new Photo(Paths.get("/uploads/count1.jpg"));
        Photo photo2 = new Photo(Paths.get("/uploads/count2.jpg"));
        Photo photo3 = new Photo(Paths.get("/uploads/count3.jpg"));
        Photo photo4 = new Photo(Paths.get("/uploads/count4.jpg"));

        saveToMongo(photo1);
        saveToMongo(photo2);
        saveToMongo(photo3);
        saveToMongo(photo4);

        assertEquals(4, repository.count());
    }

    @Test
    void testExistsById() {
        Photo photo = new Photo(Paths.get("/uploads/exists.jpg"));
        Photo saved = saveToMongo(photo);

        try {
            Field pkField = Photo.class.getDeclaredField("pk");
            pkField.setAccessible(true);
            long id = pkField.getLong(saved);

            boolean exists = ((MongoRepository<Photo, Long>) repository).existsById(id);
            assertTrue(exists);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access pk field", e);
        }
    }

    @Test
    void testFindAll() {
        Photo photo1 = new Photo(Paths.get("/uploads/all1.jpg"));
        Photo photo2 = new Photo(Paths.get("/uploads/all2.jpg"));

        saveToMongo(photo1);
        saveToMongo(photo2);

        List<Photo> all = ((MongoRepository<Photo, Long>) repository).findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testPhotoDifferentExtensions() {
        Photo jpg = new Photo(Paths.get("/uploads/image.jpg"));
        Photo png = new Photo(Paths.get("/uploads/image.png"));
        Photo gif = new Photo(Paths.get("/uploads/image.gif"));
        Photo webp = new Photo(Paths.get("/uploads/image.webp"));

        Photo savedJpg = saveToMongo(jpg);
        Photo savedPng = saveToMongo(png);
        Photo savedGif = saveToMongo(gif);
        Photo savedWebp = saveToMongo(webp);

        assertEquals("/uploads/image.jpg", savedJpg.getPhotoFile());
        assertEquals("/uploads/image.png", savedPng.getPhotoFile());
        assertEquals("/uploads/image.gif", savedGif.getPhotoFile());
        assertEquals("/uploads/image.webp", savedWebp.getPhotoFile());
    }

    @Test
    void testPhotoWithLongPath() {
        Path longPath = Paths.get("/uploads/very/long/nested/directory/structure/with/many/levels/photo.jpg");
        Photo photo = new Photo(longPath);

        Photo saved = saveToMongo(photo);

        assertTrue(saved.getPhotoFile().contains("very/long/nested"));
    }

    @Test
    void testPhotoFileCanBeUpdated() {
        Photo photo = new Photo(Paths.get("/uploads/original.jpg"));
        Photo saved = saveToMongo(photo);

        saved.setPhotoFile("/uploads/updated.jpg");
        Photo updated = ((MongoRepository<Photo, Long>) repository).save(saved);

        assertEquals("/uploads/updated.jpg", updated.getPhotoFile());
    }
}
