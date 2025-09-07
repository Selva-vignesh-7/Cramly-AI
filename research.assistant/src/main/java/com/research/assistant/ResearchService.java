package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ResearchService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = WebClient.builder().build();
        this.objectMapper = objectMapper;
    }

    public String processContent(ResearchRequest request) {
        // Build the prompt
        String prompt = buildPrompt(request);

        // query the AI model API
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // parse the response
        // return response

        return extractTextResponse(response);
    }

    private String extractTextResponse(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent() != null &&
                                firstCandidate.getContent().getParts() != null &&
                                !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No content found in response";
        }catch (Exception e){
            return "Error Parsing: " + e.getMessage();
        }
    }


    private String buildPrompt(ResearchRequest request) {
        StringBuilder prompt = new StringBuilder();
        switch (request.getOperation()){
            case "summarize":
                prompt.append("You are my Research Assistant, my teacher , professor , philosopher, scientist, researcher, everything .\n" +
                        "Your purpose is to make studying easier.\n" +
                        "I will paste a passage of text. Rewrite it into two sections:\n" +
                        "\n" +
                        "1. Easy Explanation\n" +
                        "- Explain the full passage in simple, clear language.\n" +
                        "- Cover all main ideas so even a beginner can understand.\n" +
                        "- Keep it short, about 1–3 paragraphs.\n" +
                        "\n" +
                        "2. Detailed Explanation\n" +
                        "- Give a deeper, structured explanation in short paragraphs.\n" +
                        "- Use \"-\" dashes for lists of applications, insights, or related concepts.\n" +
                        "- End with 2–3 reference links.\n" +
                        "\n" +
                        "Rules:\n" +
                        "- Plain text only. No markdown, no HTML, no symbols like ** or ##.\n" +
                        "- Put a blank line before each section heading.\n" +
                        "- No introductions or filler like \"Here is your answer\".\n" +
                        "- Be concise but complete.\n" +
                        "\n" +
                        "\n "+
                        "\n" +
                        "\n:\n\n");
                break;
            case "suggest":
                prompt.append("Based on the following content: suggest related topic and further reading. Format the response with clear heading and bullet points:\n\n");
                break;
            default:
                throw  new IllegalArgumentException("Unknown operation: " + request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}
