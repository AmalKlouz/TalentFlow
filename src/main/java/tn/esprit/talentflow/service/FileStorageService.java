package tn.esprit.talentflow.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDirectory =
            Paths.get("uploads/cv");

    public FileStorageService() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    public String saveCv(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Le CV est obligatoire");
        }

        String contentType = file.getContentType();

        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new RuntimeException(
                    "Seuls les fichiers PDF sont autorisés"
            );
        }

        String originalName = file.getOriginalFilename();

        String extension = ".pdf";

        if (originalName != null &&
                originalName.toLowerCase().endsWith(".pdf")) {
            extension = ".pdf";
        }

        String fileName =
                UUID.randomUUID() + extension;

        Path targetPath =
                uploadDirectory.resolve(fileName);

        try {
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return targetPath.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erreur lors de l'enregistrement du CV",
                    e
            );
        }
    }
    public byte[] getCv(String cvPath) {

        try {
            Path path = Paths.get(cvPath);

            if (!Files.exists(path)) {
                throw new RuntimeException("CV introuvable");
            }

            return Files.readAllBytes(path);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Erreur lors de la lecture du CV",
                    e
            );
        }
    }
}