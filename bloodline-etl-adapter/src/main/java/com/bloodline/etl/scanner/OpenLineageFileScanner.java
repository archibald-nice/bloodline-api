package com.bloodline.etl.scanner;

import com.bloodline.etl.parser.OpenLineageEventParser;
import com.bloodline.etl.service.LineageIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Component
public class OpenLineageFileScanner {

    private static final Logger log = LoggerFactory.getLogger(OpenLineageFileScanner.class);

    private final OpenLineageEventParser parser;
    private final LineageIngestionService ingestionService;
    private final String scanPath;

    public OpenLineageFileScanner(
            OpenLineageEventParser parser,
            LineageIngestionService ingestionService,
            @Value("${openlineage.scan.path:/tmp/openlineage}") String scanPath) {
        this.parser = parser;
        this.ingestionService = ingestionService;
        this.scanPath = scanPath;
    }

    @Scheduled(fixedDelay = 30000)
    public synchronized void scan() {
        File dir = new File(scanPath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles((d, name) -> {
            File f = new File(d, name);
            return f.isFile() && name.toLowerCase().endsWith(".json");
        });
        if (files == null) return;

        for (File file : files) {
            try {
                ingestionService.ingest(parser.parse(file));
                Path archiveDir = Paths.get(scanPath, "archive");
                if (!Files.exists(archiveDir)) {
                    Files.createDirectories(archiveDir);
                }
                Files.move(file.toPath(), archiveDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Failed to process {}", file.getName(), e);
            }
        }
    }
}
