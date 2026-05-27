package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaSourceParserTest {

    private final JavaSourceParser parser = new JavaSourceParser();

    @Test
    void shouldIncludeJpaParserInChain() {
        String source = "package com.example;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "@Entity\n" +
                "@Table(name = \"t_user\")\n" +
                "public class User {\n" +
                "}";

        List<ParsedRelation> result = parser.parseJavaFile(source);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(ParsedRelation::getTargetType)
                .contains("TABLE");
        assertThat(result).extracting(ParsedRelation::getTargetName)
                .contains("t_user");
    }

    @Test
    void shouldCombineMultipleParsers() {
        String source = "package com.example;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "import org.springframework.web.bind.annotation.RestController;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "@Entity\n" +
                "@Table(name = \"t_user\")\n" +
                "@RestController\n" +
                "public class UserController {\n" +
                "    @GetMapping(\"/users\")\n" +
                "    public String getUsers() { return \"\"; }\n" +
                "}";

        List<ParsedRelation> result = parser.parseJavaFile(source);

        assertThat(result).extracting(ParsedRelation::getTargetType)
                .contains("TABLE", "API_ENDPOINT");
    }
}
