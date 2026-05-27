package com.bloodline.service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GitHubCodeFetchServiceTest {

    @InjectMocks
    private GitHubCodeFetchService codeFetchService;

    @TempDir
    Path tempDir;

    @Test
    void shouldEnumerateJavaAndXmlFiles() throws IOException {
        Path repoRoot = tempDir.resolve("repo");
        Files.createDirectories(repoRoot.resolve("src/main/java/com/example"));
        Files.createDirectories(repoRoot.resolve("src/main/resources/mapper"));
        Files.write(repoRoot.resolve("src/main/java/com/example/OrderService.java"),
                "package com.example; public class OrderService {}".getBytes());
        Files.write(repoRoot.resolve("src/main/resources/mapper/OrderMapper.xml"),
                "<mapper namespace='com.example.OrderMapper'></mapper>".getBytes());
        Files.write(repoRoot.resolve("README.md"), "# README".getBytes());

        List<GitHubCodeFetchService.SourceFile> files = codeFetchService.ensembleSourceFiles(repoRoot.toFile());

        assertThat(files).hasSize(2);
        assertThat(files).extracting(GitHubCodeFetchService.SourceFile::getRelativePath)
                .containsExactlyInAnyOrder("src/main/java/com/example/OrderService.java",
                        "src/main/resources/mapper/OrderMapper.xml");
    }

    @Test
    void shouldReturnEmptyListForEmptyDirectory() {
        File emptyDir = tempDir.resolve("empty").toFile();
        emptyDir.mkdirs();

        List<GitHubCodeFetchService.SourceFile> files = codeFetchService.ensembleSourceFiles(emptyDir);

        assertThat(files).isEmpty();
    }
}
