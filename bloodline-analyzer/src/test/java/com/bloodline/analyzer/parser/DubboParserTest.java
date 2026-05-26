package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DubboParserTest {

    private final DubboParser parser = new DubboParser();

    @Test
    void shouldParseDubboReferenceField() {
        String source = "package com.example;\n" +
                "import org.apache.dubbo.config.annotation.DubboReference;\n" +
                "public class UserServiceConsumer {\n" +
                "    @DubboReference\n" +
                "    private UserService userService;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("CALLS");
        assertThat(result.get(0).getTargetType()).isEqualTo("SERVICE");
        assertThat(result.get(0).getTargetName()).isEqualTo("UserService");
    }

    @Test
    void shouldParseAliDubboReferenceField() {
        String source = "package com.example;\n" +
                "import com.alibaba.dubbo.config.annotation.Reference;\n" +
                "public class OrderConsumer {\n" +
                "    @Reference\n" +
                "    private OrderService orderService;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("CALLS");
        assertThat(result.get(0).getTargetType()).isEqualTo("SERVICE");
        assertThat(result.get(0).getTargetName()).isEqualTo("OrderService");
    }

    @Test
    void shouldParseDubboServiceClass() {
        String source = "package com.example;\n" +
                "import org.apache.dubbo.config.annotation.DubboService;\n" +
                "@DubboService\n" +
                "public class UserServiceImpl implements UserService {\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("EXPOSES");
        assertThat(result.get(0).getTargetType()).isEqualTo("SERVICE");
        assertThat(result.get(0).getTargetName()).isEqualTo("UserService");
    }

    @Test
    void shouldParseMultipleDubboReferences() {
        String source = "package com.example;\n" +
                "public class MultiConsumer {\n" +
                "    @DubboReference\n" +
                "    private UserService userService;\n" +
                "    @DubboReference\n" +
                "    private OrderService orderService;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParsedRelation::getTargetName)
                .containsExactlyInAnyOrder("UserService", "OrderService");
    }

    @Test
    void shouldReturnEmptyForNonDubboSource() {
        String source = "package com.example;\n" +
                "public class PlainService {\n" +
                "    private String name;\n" +
                "}";

        List<ParsedRelation> result = parser.parse(source);

        assertThat(result).isEmpty();
    }
}
