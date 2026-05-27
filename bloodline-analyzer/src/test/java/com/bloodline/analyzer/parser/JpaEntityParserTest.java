package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JpaEntityParserTest {

    private final JpaEntityParser parser = new JpaEntityParser();

    @Test
    void shouldParseEntityWithTableAnnotation() {
        String source = "package com.example;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "@Entity\n" +
                "@Table(name = \"t_user\")\n" +
                "public class User {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("OWNS");
        assertThat(result.get(0).getTargetType()).isEqualTo("TABLE");
        assertThat(result.get(0).getTargetName()).isEqualTo("t_user");
    }

    @Test
    void shouldParseEntityWithDefaultTableName() {
        String source = "package com.example;\n" +
                "import javax.persistence.Entity;\n" +
                "@Entity\n" +
                "public class Order {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("OWNS");
        assertThat(result.get(0).getTargetType()).isEqualTo("TABLE");
        assertThat(result.get(0).getTargetName()).isEqualTo("Order");
    }

    @Test
    void shouldParseEntityWithColumns() {
        String source = "package com.example;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Table;\n" +
                "import javax.persistence.Column;\n" +
                "@Entity\n" +
                "@Table(name = \"t_user\")\n" +
                "public class User {\n" +
                "    @Column(name = \"user_name\")\n" +
                "    private String username;\n" +
                "    @Column(name = \"email_addr\")\n" +
                "    private String email;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(ParsedRelation::getTargetType)
                .containsExactlyInAnyOrder("TABLE", "COLUMN", "COLUMN");

        ParsedRelation tableRel = result.stream()
                .filter(r -> r.getTargetType().equals("TABLE"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected TABLE relation"));
        assertThat(tableRel.getTargetName()).isEqualTo("t_user");

        List<ParsedRelation> columnRels = result.stream()
                .filter(r -> r.getTargetType().equals("COLUMN"))
                .collect(java.util.stream.Collectors.toList());
        assertThat(columnRels).hasSize(2);
        assertThat(columnRels).extracting(ParsedRelation::getTargetName)
                .containsExactlyInAnyOrder("user_name", "email_addr");
        assertThat(columnRels).extracting(ParsedRelation::getTargetDetail)
                .containsOnly("t_user");
    }

    @Test
    void shouldReturnEmptyForNonJpaSource() {
        String source = "package com.example;\n" +
                "public class PlainService {\n" +
                "    private String name;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSupportJakartaNamespace() {
        String source = "package com.example;\n" +
                "import jakarta.persistence.Entity;\n" +
                "import jakarta.persistence.Table;\n" +
                "@Entity\n" +
                "@Table(name = \"jakarta_user\")\n" +
                "public class User {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetType()).isEqualTo("TABLE");
        assertThat(result.get(0).getTargetName()).isEqualTo("jakarta_user");
    }

    @Test
    void shouldDefaultColumnNameToFieldName() {
        String source = "package com.example;\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Column;\n" +
                "@Entity\n" +
                "public class User {\n" +
                "    @Column\n" +
                "    private String email;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        ParsedRelation colRel = result.stream()
                .filter(r -> r.getTargetType().equals("COLUMN"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected COLUMN relation"));
        assertThat(colRel.getTargetName()).isEqualTo("email");
    }

    @Test
    void shouldReturnEmptyForInvalidSource() {
        String source = "this is not valid Java";
        List<ParsedRelation> result = parser.parse(source);
        assertThat(result).isEmpty();
    }
}
