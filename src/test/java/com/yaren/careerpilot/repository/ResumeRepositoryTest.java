package com.yaren.careerpilot.repository;

import com.yaren.careerpilot.entity.Resume;
import com.yaren.careerpilot.enums.ResumeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ResumeRepository için JPA slice testi.
 *
 * @DataJpaTest:
 *   - Sadece JPA katmanını yükler (Controller, Service yüklenmez → hızlı)
 *   - H2 in-memory DB otomatik olarak kullanılır (Docker gerekmez)
 *   - Her test metodundan sonra transaction rollback yapar (testler birbirini etkilemez)
 *
 * H2 neden yeterli?
 *   findByFileNameContainingIgnoreCase → JPQL: LOWER(fileName) LIKE LOWER('%keyword%')
 *   Bu sorgu H2 ve PostgreSQL'de aynı davranır — Docker uyumsuzluğu geçici workaround.
 */
@DataJpaTest
class ResumeRepositoryTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @BeforeEach
    void setUp() {
        resumeRepository.deleteAll();
    }

    private Resume buildResume(String fileName) {
        Resume resume = new Resume();
        resume.setFileName(fileName);
        resume.setFilePath("uploads/" + fileName);
        resume.setStatus(ResumeStatus.UPLOADED);
        return resume;
    }

    @Test
    void save_ShouldPersistResume() {
        Resume saved = resumeRepository.save(buildResume("cv.pdf"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("cv.pdf");
    }

    @Test
    void findByFileNameContainingIgnoreCase_ShouldReturnMatch_WhenKeywordIsLowercase() {
        resumeRepository.save(buildResume("java-developer-cv.pdf"));
        resumeRepository.save(buildResume("python-developer-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByFileNameContainingIgnoreCase("java");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFileName()).isEqualTo("java-developer-cv.pdf");
    }

    @Test
    void findByFileNameContainingIgnoreCase_ShouldReturnMatch_WhenKeywordIsUppercase() {
        resumeRepository.save(buildResume("java-developer-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByFileNameContainingIgnoreCase("JAVA");

        assertThat(results).hasSize(1);
    }

    @Test
    void findByFileNameContainingIgnoreCase_ShouldReturnMatch_WhenKeywordIsMixed() {
        resumeRepository.save(buildResume("java-developer-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByFileNameContainingIgnoreCase("JaVa");

        assertThat(results).hasSize(1);
    }

    @Test
    void findByFileNameContainingIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        resumeRepository.save(buildResume("java-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByFileNameContainingIgnoreCase("python");

        assertThat(results).isEmpty();
    }

    @Test
    void findByFileNameContainingIgnoreCase_ShouldReturnMultipleMatches() {
        resumeRepository.save(buildResume("java-cv.pdf"));
        resumeRepository.save(buildResume("java-backend-cv.pdf"));
        resumeRepository.save(buildResume("python-cv.pdf"));

        List<Resume> results =
                resumeRepository.findByFileNameContainingIgnoreCase("java");

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
