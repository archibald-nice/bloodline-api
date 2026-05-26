package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestControllerParserTest {

    private final RestControllerParser parser = new RestControllerParser();

    @Test
    void shouldParseGetMappingWithClassPath() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RequestMapping(\"/api\")\n" +
                "public class UserController {\n" +
                "    @GetMapping(\"/users\")\n" +
                "    public String getUsers() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("EXPOSES");
        assertThat(result.get(0).getTargetType()).isEqualTo("API_ENDPOINT");
        assertThat(result.get(0).getTargetName()).isEqualTo("/api/users");
        assertThat(result.get(0).getTargetDetail()).isEqualTo("GET");
    }

    @Test
    void shouldParseMultipleMappings() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RestController\n" +
                "public class OrderController {\n" +
                "    @GetMapping(\"/orders\")\n" +
                "    public String getOrders() { return \"\"; }\n" +
                "    @PostMapping(\"/orders\")\n" +
                "    public String createOrder() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParsedRelation::getTargetDetail)
                .containsExactlyInAnyOrder("GET", "POST");
        assertThat(result).extracting(ParsedRelation::getTargetName)
                .allMatch(name -> name.equals("/orders"));
    }

    @Test
    void shouldParseMappingWithoutClassPath() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RestController\n" +
                "public class ProductController {\n" +
                "    @GetMapping(\"/products\")\n" +
                "    public String getProducts() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetName()).isEqualTo("/products");
    }

    @Test
    void shouldParsePutAndDeleteMappings() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RestController\n" +
                "public class UserController {\n" +
                "    @PutMapping(\"/users/1\")\n" +
                "    public String updateUser() { return \"\"; }\n" +
                "    @DeleteMapping(\"/users/1\")\n" +
                "    public String deleteUser() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParsedRelation::getTargetDetail)
                .containsExactlyInAnyOrder("PUT", "DELETE");
    }

    @Test
    void shouldAvoidDoubleSlashWhenJoiningPaths() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RequestMapping(\"/api/\")\n" +
                "public class UserController {\n" +
                "    @GetMapping(\"/users\")\n" +
                "    public String getUsers() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result.get(0).getTargetName()).isEqualTo("/api/users");
    }

    @Test
    void shouldPreservePathVariableTemplate() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RestController\n" +
                "public class UserController {\n" +
                "    @GetMapping(\"/users/{id}\")\n" +
                "    public String getUser() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result.get(0).getTargetName()).isEqualTo("/users/{id}");
    }

    @Test
    void shouldParseRequestMappingWithValueAttribute() {
        String source = "package com.example;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "@RequestMapping(value = \"/api\")\n" +
                "public class UserController {\n" +
                "    @GetMapping(\"/users\")\n" +
                "    public String getUsers() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result.get(0).getTargetName()).isEqualTo("/api/users");
    }

    @Test
    void shouldReturnEmptyForNonControllerSource() {
        String source = "package com.example;\n" +
                "public class PlainService {\n" +
                "    public String hello() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).isEmpty();
    }
}
