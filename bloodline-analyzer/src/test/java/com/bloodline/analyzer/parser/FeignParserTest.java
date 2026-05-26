package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeignParserTest {

    private final FeignParser parser = new FeignParser();

    @Test
    void shouldParseFeignClientWithName() {
        String source = "package com.example;\n" +
                "import org.springframework.cloud.openfeign.FeignClient;\n" +
                "@FeignClient(name = \"user-service\")\n" +
                "public interface UserClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("HTTP_CALLS");
        assertThat(result.get(0).getTargetType()).isEqualTo("API_ENDPOINT");
        assertThat(result.get(0).getTargetAppId()).isEqualTo("user-service");
    }

    @Test
    void shouldParseFeignClientWithValue() {
        String source = "package com.example;\n" +
                "@FeignClient(value = \"order-service\")\n" +
                "public interface OrderClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetAppId()).isEqualTo("order-service");
    }

    @Test
    void shouldParseFeignClientWithPath() {
        String source = "package com.example;\n" +
                "@FeignClient(name = \"user-service\", path = \"/api/users\")\n" +
                "public interface UserClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetAppId()).isEqualTo("user-service");
        assertThat(result.get(0).getTargetName()).isEqualTo("/api/users");
    }

    @Test
    void shouldParseFeignClientSingleMember() {
        String source = "package com.example;\n" +
                "@FeignClient(\"inventory-service\")\n" +
                "public interface InventoryClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetAppId()).isEqualTo("inventory-service");
    }

    @Test
    void shouldExtractPathFromFullUrl() {
        String source = "package com.example;\n" +
                "@FeignClient(url = \"http://user-service/api/users\")\n" +
                "public interface UserClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetName()).isEqualTo("/api/users");
    }

    @Test
    void shouldAutoPrefixPathWithoutLeadingSlash() {
        String source = "package com.example;\n" +
                "@FeignClient(name = \"user-service\", path = \"api/users\")\n" +
                "public interface UserClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetName()).isEqualTo("/api/users");
    }

    @Test
    void shouldPreferNameOverValue() {
        String source = "package com.example;\n" +
                "@FeignClient(name = \"real-service\", value = \"old-service\")\n" +
                "public interface UserClient {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetAppId()).isEqualTo("real-service");
    }

    @Test
    void shouldReturnEmptyForNonFeignSource() {
        String source = "package com.example;\n" +
                "public class PlainService {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).isEmpty();
    }
}
