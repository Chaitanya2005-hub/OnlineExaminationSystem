package com.stark.exam.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GeminiService {

    // YOUR API KEY
    private static final String API_KEY = "AIzaSyBLeC8-6-OVkYtE54h1L1jAdASHyxPc7zE";

    // FIX: Use 'gemini-1.5-flash' which is the current standard free model
        private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    public static List<GeneratedQuestion> generateQuestions(String subject, String difficulty, int count) {
        List<GeneratedQuestion> questions = new ArrayList<>();

        OkHttpClient client = getUnsafeOkHttpClient();
        Gson gson = new Gson();

        String prompt = String.format(
                "Generate %d multiple-choice questions on '%s' (Difficulty: %s). " +
                        "Return ONLY a JSON Array. Format: " +
                        "[{\"question_text\":\"...\",\"option_a\":\"...\",\"option_b\":\"...\",\"option_c\":\"...\",\"option_d\":\"...\",\"correct_answer\":\"A\"}]",
                count, subject, difficulty
        );

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(textPart);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject payload = new JsonObject();
        payload.add("contents", contents);

        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder().url(URL).post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String respStr = response.body().string();
                System.out.println("AI Success Response: " + respStr);

                JsonObject jsonResp = gson.fromJson(respStr, JsonObject.class);

                try {
                    String rawText = jsonResp.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

                    rawText = rawText.replace("``````", "").trim();

                    GeneratedQuestion[] qArray = gson.fromJson(rawText, GeneratedQuestion[].class);
                    if (qArray != null) {
                        for (GeneratedQuestion q : qArray) questions.add(q);
                    }
                } catch (Exception e) {
                    System.err.println("JSON Parsing Failed: " + e.getMessage());
                }
            } else {
                System.err.println("HTTP Error: " + response.code() + " " + response.message());
                if (response.body() != null) System.err.println("Body: " + response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class GeneratedQuestion {
        public String question_text, option_a, option_b, option_c, option_d, correct_answer;
    }
}
