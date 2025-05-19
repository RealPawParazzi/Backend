package pawparazzi.back.diary.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    private int n = 1;

    public ChatRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new ChatMessage("user", prompt));
    }
}
