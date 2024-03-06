package org.generationai.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.generationai.exception.ErrorCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationResponseOk {

    private String requestId;
}
