package paparazzi.back.posting.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import paparazzi.back.posting.entity.Visibility;

public record PostRequestDTO(
        // @NotNull Long userId,
        String caption,
        @NotBlank String imageUrl,
        @NotNull Visibility visibility
) {}