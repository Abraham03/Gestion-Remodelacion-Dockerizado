package com.gestionremodelacion.gestion.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.gestionremodelacion.gestion.exception.ErrorCatalog;
import com.gestionremodelacion.gestion.exception.FileUploadException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "spring.cloud.gcp.storage.enabled", matchIfMissing = true)
public class FileUploadService {

    // Inyecta el cliente de Storage que da el starter de Spring
    private final Storage storage;

    // Inyecta la variable de entorno que contiene el nombre del bucket
    @Value("${google.storage.bucket.name}")
    private String bucketName;
    // Lista de extensiones de imagen permitidas
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "gif", "webp");
    // Tamaño máximo permitido en bytes (ej. 5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public FileUploadService(Storage storage) {
        this.storage = storage;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // 1. Validaciones del archivo
        validateFile(file);

        try {
            // 2. Genera un nombre de archivo único para evitar colisiones
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "." + extension;

            BlobId blobId = BlobId.of(bucketName, fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            // 3. Sube el archivo al bucket
            storage.create(blobInfo, file.getBytes());

            // 4. Devuelve la URL pública del archivo
            return "https://storage.googleapis.com/" + bucketName + "/" + fileName;

        } catch (StorageException | IOException e) {
            // 5. Capturamos cualquier error y lanzamos nuestra excepción personalizada
            throw new FileUploadException(ErrorCatalog.FILE_UPLOAD_ERROR.getKey(), e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException(ErrorCatalog.FILE_EMPTY.getKey());
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException(ErrorCatalog.FILE_SIZE_EXCEEDED.getKey());
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileUploadException(ErrorCatalog.FILE_INVALID_TYPE.getKey());
        }
    }
}
