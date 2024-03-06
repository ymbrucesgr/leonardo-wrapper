package org.generationai.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.generationai.entity.ImageDescription;
import org.generationai.entity.Trait;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageGenerationRequest {

    private String seed;
    private List<Trait> newTraits;
    private ImageDescription baseImage;
}
