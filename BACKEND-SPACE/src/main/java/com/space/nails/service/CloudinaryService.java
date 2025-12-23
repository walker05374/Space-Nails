package com.space.nails.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Faz upload de uma imagem para o Cloudinary
     * 
     * @param file Arquivo MultipartFile
     * @return URL pública da imagem
     * @throws IOException Se houver erro no upload
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Validação de tipo de arquivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas arquivos de imagem são permitidos");
        }

        // Upload para Cloudinary com configurações
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "space-nails/portfolio", // Organiza em pasta
                        "resource_type", "image"));

        // Retorna a URL segura (HTTPS)
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Deleta uma imagem do Cloudinary
     * 
     * @param publicId ID público da imagem (extraído da URL)
     * @throws IOException Se houver erro na exclusão
     */
    public void deleteImage(String publicId) throws IOException {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }

    /**
     * Extrai o publicId de uma URL do Cloudinary
     * Exemplo:
     * https://res.cloudinary.com/den5q9jqu/image/upload/v123/space-nails/portfolio/abc.jpg
     * Retorna: space-nails/portfolio/abc
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }

        try {
            // Formato: .../upload/v{version}/{publicId}.{extension}
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2)
                return null;

            String afterUpload = parts[1];
            // Remove versão (v123456/)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove extensão
            int lastDot = withoutVersion.lastIndexOf('.');
            if (lastDot > 0) {
                return withoutVersion.substring(0, lastDot);
            }
            return withoutVersion;
        } catch (Exception e) {
            return null;
        }
    }
}
