package org.generationai.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.generationai.config.LeonardoAiConfig;
import org.generationai.entity.GeneratedImageEntity;
import org.generationai.entity.ImageDescription;
import org.generationai.entity.Trait;
import org.generationai.entity.request.ImageGenerationRequest;
import org.generationai.entity.request.ImageQueryRequest;
import org.generationai.entity.response.ImageGenerationResponseOk;
import org.generationai.entity.response.ImageQueryResponseOk;
import org.generationai.util.Constants;
import org.generationai.util.ImageBase64Converter;
import org.generationai.util.MultipartUtility;
import org.generationai.util.TraitsVariationMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("image")
public class GenerationController {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private LeonardoAiConfig leonardoAiConfig;

    @Operation(summary = "Generate an image using the supplied base image and new trait")
    @PostMapping("/generate")
    public ImageGenerationResponseOk imageGenerate(@RequestBody ImageGenerationRequest imageGenerationRequest) throws Exception {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType,
                buildGeneratedParameters(imageGenerationRequest).toJSONString());
        Request request = new Request.Builder()
                .url(Constants.URL_GENERATION)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("authorization", "Bearer " + leonardoAiConfig.getApiKey())
                .build();
        Response response = client.newCall(request).execute();
        String generationId = getGenerationId(response);
        saveGeneratedImageEntity(generationId, imageGenerationRequest.getBaseImage().getTraits());
        return ImageGenerationResponseOk.builder().requestId(generationId).build();
    }

    private String traits2Prompts(List<Trait> traits) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Trait trait : traits) {
            String name = trait.getName().toLowerCase();
            if (TraitsVariationMap.TRAITS_VARIATION_MAP.containsKey(name)) {
                String variation = TraitsVariationMap.TRAITS_VARIATION_MAP.getOrDefault(name, "").toString();
                stringBuilder.append(String.format(variation, trait.getValue()));
            }
        }
        return String.valueOf(stringBuilder);
    }

    private String buildBaseImage(ImageGenerationRequest imageGenerationRequest) throws Exception {
        String imageBase64 = imageGenerationRequest.getBaseImage().getImage();
        if (StringUtils.isEmpty(imageBase64)) {
            return null;
        }
        try {
            String fileName = UUID.randomUUID() + ".png";
            ImageBase64Converter.getImgBase64ToImgFile(imageBase64, fileName);
            JSONObject uploadInitImageInfo = getInitImageInfo();
            String fields = uploadInitImageInfo.getString("fields");
            String url = uploadInitImageInfo.getString("url");
            Map<String, String> fieldParameters = JSONObject.parseObject(fields, Map.class);
            uploadInitImage(url, fieldParameters, fileName);
            return uploadInitImageInfo.getString("id");
        } catch (Exception e) {
            throw e;
        }
    }

    private JSONObject getInitImageInfo() throws Exception {
        try {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            okhttp3.RequestBody body = okhttp3.RequestBody.create(mediaType, "{\"extension\":\"png\"}");
            Request request = new Request.Builder()
                    .url("https://cloud.leonardo.ai/api/rest/v1/init-image")
                    .post(body)
                    .addHeader("accept", "application/json")
                    .addHeader("content-type", "application/json")
                    .addHeader("authorization", "Bearer " + leonardoAiConfig.getApiKey())
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject generationResult = JSONObject.parseObject(response.body().string());
            return generationResult.getJSONObject("uploadInitImage");
        } catch (Exception e) {
            throw e;
        }
    }

    private void uploadInitImage(String url, Map<String, String> fields, String fileName) throws Exception {
        File uploadFile = new File(fileName);
        try {
            MultipartUtility multipart = new MultipartUtility(url, "UTF-8");
            for (String key : fields.keySet()) {
                multipart.addFormField(key, fields.get(key));
            }
            multipart.addFilePart("file", uploadFile);
            List<String> response = multipart.finish();
        } catch (Exception e) {
            throw e;
        } finally {
            if (uploadFile.exists()) {
                uploadFile.delete();
            }
        }
    }

    private JSONObject buildGeneratedParameters(ImageGenerationRequest imageGenerationRequest) throws Exception {
        List<Trait> traits = imageGenerationRequest.getBaseImage().getTraits();
        traits.addAll(imageGenerationRequest.getNewTraits());
        String prompts = traits2Prompts(traits);

        String image = buildBaseImage(imageGenerationRequest);

        JSONObject generationParameters = new JSONObject();
        generationParameters.put("width", 512);
        generationParameters.put("height", 512);
        generationParameters.put("modelId", Constants.MODEL_DREAM_SHAPER_V6);
        generationParameters.put("prompt", prompts);
        generationParameters.put("seed", imageGenerationRequest.getSeed());
        generationParameters.put("init_strength", Constants.INIT_STRENGTH);
        generationParameters.put("guidance_scale", 7);
        generationParameters.put("public", true);
        generationParameters.put("promptMagic", false);
        generationParameters.put("photoReal", false);
        generationParameters.put("alchemy", false);
        generationParameters.put("presetStyle", "LEONARDO");
        generationParameters.put("num_images", Constants.NUM_IMAGES);
        if (StringUtils.isNotEmpty(image)) {
            generationParameters.put("init_image_id", image);
        }
        return generationParameters;
    }

    private String getGenerationId(Response response) throws Exception {
        try {
            JSONObject generationResult = JSONObject.parseObject(response.body().string());
            JSONObject generationsByPk = generationResult.getJSONObject("sdGenerationJob");
            return generationsByPk.getString("generationId");
        } catch (Exception e) {
            log.error("getGenerationId error:", e);
            throw e;
        }
    }

    private void saveGeneratedImageEntity(String generationId, List<Trait> traits) {
        GeneratedImageEntity generatedImageEntity = GeneratedImageEntity.builder()
                .requestId(generationId)
                .traits(traits)
                .build();
        mongoTemplate.insert(generatedImageEntity);
    }

    @Operation(summary = "Query the generated image")
    @PostMapping("/query")
    public ImageQueryResponseOk imageQuery(@RequestBody ImageQueryRequest imageQueryRequest) throws Exception {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.URL_GENERATION + "/" + imageQueryRequest.getRequestId())
                .get()
                .addHeader("accept", "application/json")
                .addHeader("authorization", "Bearer " + leonardoAiConfig.getApiKey())
                .build();
        Response response = client.newCall(request).execute();
        GeneratedImageEntity generatedImageEntity = findGeneratedImageEntityByGenerationId(imageQueryRequest.getRequestId());
        List<ImageDescription> images = wrapQueryImagesAndUpdateDB(imageQueryRequest.getRequestId(), response, generatedImageEntity);
        return ImageQueryResponseOk.builder().images(images).build();
    }

    private List<ImageDescription> wrapQueryImagesAndUpdateDB(String generationId, Response response, GeneratedImageEntity generatedImageEntity) {
        List<ImageDescription> images = new ArrayList<>();
        List<String> imagesDB = new ArrayList<>();
        try {
            JSONObject generationResult = JSONObject.parseObject(response.body().string());
            JSONObject generationsByPk = generationResult.getJSONObject("generations_by_pk");
            JSONArray generatedImages = generationsByPk.getJSONArray("generated_images");
            for (int i = 0; i < generatedImages.size(); i++) {
                String image = "data:image/jpeg;base64," + convert2Base64(generatedImages.getJSONObject(i).get("url").toString());
                images.add(ImageDescription.builder()
                        .image(image)
                        .traits(generatedImageEntity.getTraits())
                        .extraData("")
                        .build());
                imagesDB.add(image);
            }
        } catch (Exception e) {
            log.error("wrapQueryImages error:", e);
        }
        updateGeneratedImageEntityByGenerationId(generationId, generatedImageEntity, imagesDB);
        return images;
    }

    private void updateGeneratedImageEntityByGenerationId(String generationId, GeneratedImageEntity generatedImageEntity, List<String> images) {
        Criteria criteria = Criteria.where("requestId").is(generationId);
        Query query = new Query(criteria);
        if (Objects.isNull(generatedImageEntity)
                || (Objects.isNull(generatedImageEntity) && Objects.nonNull(generatedImageEntity.getImages()))) {
            return;
        }
        Update update = new Update();
        update.set("images", images);
        mongoTemplate.updateMulti(query, update, GeneratedImageEntity.class);
    }

    private GeneratedImageEntity findGeneratedImageEntityByGenerationId(String generationId) {
        Criteria criteria = Criteria.where("requestId").is(generationId);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, GeneratedImageEntity.class);
    }

    private String convert2Base64(String url) throws Exception {
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setRequestMethod("GET");
            InputStream inputStream = conn.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            inputStream.close();
            byteArrayOutputStream.close();
            return base64Image;
        } catch (IOException e) {
            log.error("convert2Base64 error:", e);
            throw e;
        }
    }

}
