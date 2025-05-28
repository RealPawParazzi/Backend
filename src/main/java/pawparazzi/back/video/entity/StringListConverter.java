package pawparazzi.back.video.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("리스트를 JSON 문자열로 변환 실패", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }

        // 문자열 앞뒤 공백 제거
        String trimmedData = dbData.trim();

        try {
            // JSON 배열 형식인지 확인 ("[" 로 시작하는지)
            if (trimmedData.startsWith("[")) {
                return objectMapper.readValue(trimmedData,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } else {
                // JSON 형식이 아닌 단순 문자열인 경우
                // 단일 문자열을 배열의 첫 번째 요소로 처리
                return Arrays.asList(trimmedData);
            }
        } catch (JsonProcessingException e) {
            // JSON 파싱 실패시에도 단순 문자열로 처리
            // 예: "https://example.com" -> ["https://example.com"]
            return Arrays.asList(trimmedData);
        }
    }
}