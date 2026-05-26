package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisParserTest {

    private final MyBatisParser parser = new MyBatisParser();

    @Test
    void shouldParseSelectFromXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\n" +
                "    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"com.example.UserMapper\">\n" +
                "    <select id=\"findById\" resultType=\"User\">\n" +
                "        SELECT * FROM user WHERE id = #{id}\n" +
                "    </select>\n" +
                "</mapper>";

        List<ParsedRelation> result = parser.parseXml(xml);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationType()).isEqualTo("QUERIES");
        assertThat(result.get(0).getTargetType()).isEqualTo("TABLE");
        assertThat(result.get(0).getTargetName()).isEqualTo("user");
        assertThat(result.get(0).getTargetDetail()).isEqualTo("SELECT");
    }

    @Test
    void shouldParseMultipleOperationsFromXml() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<mapper namespace=\"com.example.UserMapper\">\n" +
                "    <select id=\"find\">SELECT * FROM user</select>\n" +
                "    <insert id=\"insert\">INSERT INTO user VALUES (1)</insert>\n" +
                "    <update id=\"update\">UPDATE user SET name = 'x'</update>\n" +
                "    <delete id=\"delete\">DELETE FROM user WHERE id = 1</delete>\n" +
                "</mapper>";

        List<ParsedRelation> result = parser.parseXml(xml);

        assertThat(result).hasSize(4);
        assertThat(result).extracting(ParsedRelation::getTargetDetail)
                .containsExactlyInAnyOrder("SELECT", "INSERT", "UPDATE", "DELETE");
        assertThat(result).extracting(ParsedRelation::getTargetName)
                .allMatch(name -> name.equals("user"));
    }

    @Test
    void shouldParseSelectAnnotation() {
        String source = "package com.example;\n" +
                "import org.apache.ibatis.annotations.Select;\n" +
                "public interface UserMapper {\n" +
                "    @Select(\"SELECT * FROM user WHERE id = #{id}\")\n" +
                "    User findById(Long id);\n" +
                "}";

        List<ParsedRelation> result = parser.parseAnnotation(source);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetName()).isEqualTo("user");
        assertThat(result.get(0).getTargetDetail()).isEqualTo("SELECT");
    }

    @Test
    void shouldParseJoinTablesFromXml() {
        String xml = "<mapper namespace=\"com.example.OrderMapper\">\n" +
                "    <select id=\"findWithUser\">\n" +
                "        SELECT o.*, u.name FROM orders o JOIN user u ON o.user_id = u.id\n" +
                "    </select>\n" +
                "</mapper>";

        List<ParsedRelation> result = parser.parseXml(xml);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ParsedRelation::getTargetName)
                .containsExactlyInAnyOrder("orders", "user");
    }

    @Test
    void shouldResolveIncludeRef() {
        String xml = "<mapper namespace=\"com.example.UserMapper\">\n" +
                "    <sql id=\"columns\">id, name</sql>\n" +
                "    <select id=\"find\">\n" +
                "        SELECT <include refid=\"columns\"/> FROM user\n" +
                "    </select>\n" +
                "</mapper>";

        List<ParsedRelation> result = parser.parseXml(xml);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetName()).isEqualTo("user");
    }

    @Test
    void shouldExtractTablesFromDynamicIfTag() {
        String xml = "<mapper namespace=\"com.example.UserMapper\">\n" +
                "    <select id=\"find\">\n" +
                "        SELECT * FROM user\n" +
                "        <if test=\"name != null\">\n" +
                "            WHERE name = #{name}\n" +
                "        </if>\n" +
                "    </select>\n" +
                "</mapper>";

        List<ParsedRelation> result = parser.parseXml(xml);

        assertThat(result).isNotEmpty();
        assertThat(result).extracting(ParsedRelation::getTargetName)
                .contains("user");
    }

    @Test
    void shouldHandleInvalidSqlGracefully() {
        String xml = "<mapper namespace=\"com.example.BadMapper\">\n" +
                "    <select id=\"bad\">THIS IS NOT SQL</select>\n" +
                "</mapper>";

        List<ParsedRelation> result = parser.parseXml(xml);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyForNonMyBatisSource() {
        String source = "package com.example;\n" +
                "public class PlainService {\n" +
                "}";

        List<ParsedRelation> result = parser.parseAnnotation(source);

        assertThat(result).isEmpty();
    }
}
