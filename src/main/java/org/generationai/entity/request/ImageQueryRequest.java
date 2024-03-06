package org.generationai.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.generationai.entity.ImageDescription;
import org.generationai.entity.Trait;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageQueryRequest {

    private String requestId;
}
