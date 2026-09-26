package ru.monyamau.cloudfilestorage.mapper;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.monyamau.cloudfilestorage.dto.request.RequestUploadDto;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadCommand;
import ru.monyamau.cloudfilestorage.dto.request.service.UploadedFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UploadCommandMapper {
    public UploadCommand toCommand(RequestUploadDto uploadDto) {
        List<UploadedFile> files = new ArrayList<>();
        for (MultipartFile multipartFile : uploadDto.object()) {
            try {
                files.add(new UploadedFile(multipartFile.getOriginalFilename(),
                        multipartFile.getContentType(),
                        multipartFile.getSize(),
                        multipartFile.getInputStream()));
            } catch (IOException e) {
                throw new IllegalStateException("Ошибка сохранения: не удалось преобразовать ресурс из DTO: "
                        + multipartFile.getOriginalFilename());
            }
        }
        return new UploadCommand(uploadDto.path(), files);
    }
}