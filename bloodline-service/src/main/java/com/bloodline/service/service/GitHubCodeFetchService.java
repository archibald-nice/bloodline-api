package com.bloodline.service.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubCodeFetchService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubCodeFetchService.class);
    private static final String CACHE_BASE = System.getProperty("java.io.tmpdir") + "/bloodline-cache";

    public File cloneRepository(String gitUrl, String branch, String commitSha) throws GitAPIException {
        String cacheKey = buildCacheKey(gitUrl, branch, commitSha);
        File targetDir = new File(CACHE_BASE, cacheKey);

        if (targetDir.exists()) {
            logger.info("Cache hit for {}", cacheKey);
            return targetDir;
        }

        targetDir.mkdirs();
        logger.info("Cloning {} to {}", gitUrl, targetDir);

        Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(targetDir)
                .setBranch(branch)
                .call();

        return targetDir;
    }

    public List<SourceFile> ensembleSourceFiles(File repoRoot) {
        List<SourceFile> files = new ArrayList<>();
        collectFiles(repoRoot, repoRoot, files);
        return files;
    }

    private void collectFiles(File root, File current, List<SourceFile> accumulator) {
        File[] children = current.listFiles();
        if (children == null) return;

        for (File child : children) {
            if (child.isDirectory()) {
                if (child.getName().equals(".git")) continue;
                collectFiles(root, child, accumulator);
            } else if (child.getName().endsWith(".java") || child.getName().endsWith(".xml")) {
                try {
                    String relativePath = root.toURI().relativize(child.toURI()).getPath();
                    String content = new String(Files.readAllBytes(child.toPath()));
                    accumulator.add(new SourceFile(relativePath, content));
                } catch (IOException e) {
                    logger.warn("Failed to read file: {}", child.getAbsolutePath(), e);
                }
            }
        }
    }

    public void cleanup(File repoDir) {
        if (repoDir != null && repoDir.exists()) {
            deleteDirectory(repoDir);
            logger.info("Cleaned up {}", repoDir);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    private String buildCacheKey(String gitUrl, String branch, String commitSha) {
        String urlHash = Integer.toHexString(gitUrl.hashCode());
        return urlHash + "/" + branch + "/" + (commitSha != null ? commitSha : "latest");
    }

    public static class SourceFile {
        private final String relativePath;
        private final String content;

        public SourceFile(String relativePath, String content) {
            this.relativePath = relativePath;
            this.content = content;
        }

        public String getRelativePath() { return relativePath; }
        public String getContent() { return content; }
    }
}