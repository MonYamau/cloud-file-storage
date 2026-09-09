package ru.monyamau.cloudfilestorage.annotation;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ru.monyamau.cloudfilestorage.dto.response.ErrorDto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "404", description = "Ресурс не найден",
        content = @Content(schema = @Schema(implementation = ErrorDto.class)))
public @interface NotFoundErrorApiResponse {
}
