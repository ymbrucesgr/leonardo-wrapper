package org.generationai.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationai.entity.InitTrait;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraitsVariationMap {

    public static final Map<String, Object> TRAITS_VARIATION_MAP = new HashMap<>();

    static {
//        TRAITS_VARIATION_MAP.put("hat", "The cat is wearing %s.");
//        TRAITS_VARIATION_MAP.put("eye", "The cat %s.");
//        TRAITS_VARIATION_MAP.put("mouth", "The cat has %s mouth.");
//        TRAITS_VARIATION_MAP.put("clothes", "The cat is wearing %s.");
//        TRAITS_VARIATION_MAP.put("shoes", "The cat is wearing %s.");
//        TRAITS_VARIATION_MAP.put("weapon", "The cat is holding a %s.");
//        TRAITS_VARIATION_MAP.put("accessory", "The cat is wearing %s.");
//        TRAITS_VARIATION_MAP.put("pet", "The cat has a pet %s.");
//        TRAITS_VARIATION_MAP.put("style", "The cat is %s style.");
//        TRAITS_VARIATION_MAP.put("necklace", "The cat is wearing %s.");
        try {
            ClassPathResource resource = new ClassPathResource("traits/sample_traits.json");
            InputStream inputStream = resource.getInputStream();
            File jsonFile = File.createTempFile("sample_traits_copy", "json");
            FileCopyUtils.copy(inputStream.readAllBytes(), jsonFile);

            ObjectMapper mapper = new ObjectMapper();
            // File jsonFile = ResourceUtils.getFile("classpath:traits/sample_traits.json");
            List<InitTrait> map = mapper.readValue(jsonFile, new TypeReference<List<InitTrait>>(){});
            for (InitTrait initTrait : map) {
                TRAITS_VARIATION_MAP.put(initTrait.getName(), initTrait.getVariation());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}