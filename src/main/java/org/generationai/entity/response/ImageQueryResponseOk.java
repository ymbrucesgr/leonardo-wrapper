package org.generationai.entity.response;

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
public class ImageQueryResponseOk {

    private List<ImageDescription> images;
}
