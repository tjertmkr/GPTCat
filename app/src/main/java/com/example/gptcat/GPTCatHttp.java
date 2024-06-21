package com.example.gptcat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class GPTCatHttp {
    private static final String DISCORD_WEBHOOK_URL = "DISCORD_WEBHOOK_URL";
    private static final String OPEN_AI_API_KEY = "OPEN_AI_API_KEY";
    private static final String OPEN_AI_API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    public static String sendImageToDiscord(String filePath) {
        OkHttpClient client = new OkHttpClient();
        File imageFile = new File(filePath);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/png"), imageFile))
                .build();
        Request request = new Request.Builder()
                .url(DISCORD_WEBHOOK_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
            JSONArray attachments = jsonObject.getJSONArray("attachments");
            return attachments.getJSONObject(0).get("url").toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
    public static String sendImageToChatGPT4o(String imageUrl) {
        OkHttpClient client = new OkHttpClient();
        String jsonRequest = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": [\n" +
                "        {\n" +
                "          \"type\": \"text\",\n" +
                "          \"text\": \"Please solve the english problem here and only tell me the answer without explaining the solution.\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\": \"image_url\",\n" +
                "          \"image_url\": {\n" +
                "            \"url\": \"" + imageUrl + "\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequest);
        Request request = new Request.Builder()
                .url(OPEN_AI_API_ENDPOINT)
                .addHeader("Authorization", "Bearer " + OPEN_AI_API_KEY)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
            JSONArray choices = jsonObject.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").get("content").toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
