package com.yaren.careerpilot.repository;

import com.yaren.careerpilot.entity.Resume;
import com.yaren.careerpilot.entity.User;
import com.yaren.careerpilot.enums.ResumeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ResumeRepositoryTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {

        resumeRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("yaren@test.com");
        testUser.setPassword("hashedpassword");
        testUser.setFullName("Yaren Keles");
        testUser = userRepository.save(testUser);
    }

    private Resume buildResume(String fileName) {
        Resume resume = new Resume();
        resume.setFileName(fileName);
        resume.setFilePath("uploads/" + fileName);
        resume.setStatus(ResumeStatus.UPLOADED);
        resume.setUser(testUser);
        return resume;
    }

    @Test
    void save_ShouldPersistResume() {
        Resume saved = resumeRepository.save(buildResume("cv.pdf"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("cv.pdf");
        assertThat(saved.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findByUserId_ShouldReturnOnlyThatUsersResumes() {

        User anotherUser = new User();
        anotherUser.setEmail("other@test.com");
        anotherUser.setPassword("hashedpassword");
        anotherUser.setFullName("Other User");
        anotherUser = userRepository.save(anotherUser);

        resumeRepository.save(buildResume("mine.pdf"));

        Resume othersResume = new Resume();
        othersResume.setFileName("theirs.pdf");
        othersResume.setFilePath("uploads/theirs.pdf");
        othersResume.setStatus(ResumeStatus.UPLOADED);
        othersResume.setUser(anotherUser);
        resumeRepository.save(othersResume);

        List<Resume> results = resumeRepository.findByUserId(testUser.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFileName()).isEqualTo("mine.pdf");
    }

    @Test
    void findByUserIdAndFileNameContainingIgnoreCase_ShouldReturnMatch_WhenKeywordIsLowercase() {
        resumeRepository.save(buildResume("java-developer-cv.pdf"));
        resumeRepository.save(buildResume("python-developer-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByUserIdAndFileNameContainingIgnoreCase(testUser.getId(), "java");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFileName()).isEqualTo("java-developer-cv.pdf");
    }

    @Test
    void findByUserIdAndFileNameContainingIgnoreCase_ShouldReturnMatch_WhenKeywordIsUppercase() {
        resumeRepository.save(buildResume("java-developer-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByUserIdAndFileNameContainingIgnoreCase(testUser.getId(), "JAVA");

        assertThat(results).hasSize(1);
    }

    @Test
    void findByUserIdAndFileNameContainingIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        resumeRepository.save(buildResume("java-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByUserIdAndFileNameContainingIgnoreCase(testUser.getId(), "python");

        assertThat(results).isEmpty();
    }

    @Test
    void findByUserIdAndFileNameContainingIgnoreCase_ShouldReturnMultipleMatches() {
        resumeRepository.save(buildResume("java-cv.pdf"));
        resumeRepository.save(buildResume("java-backend-cv.pdf"));
        resumeRepository.save(buildResume("python-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByUserIdAndFileNameContainingIgnoreCase(testUser.getId(), "java");

        assertThat(results).hasSize(2);
    }

    @Test
    void findAll_ShouldReturnAllSavedResumes() {
        resumeRepository.save(buildResume("cv1.pdf"));
        resumeRepository.save(buildResume("cv2.pdf"));

        List<Resume> all = resumeRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        Optional<Resume> result = resumeRepository.findById(9999L);
        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveResume() {
        Resume saved = resumeRepository.save(buildResume("to-delete.pdf"));

        resumeRepository.delete(saved);

        assertThat(resumeRepository.findById(saved.getId())).isEmpty();
    }
}