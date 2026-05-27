package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JavaSourceParser {
    private final DubboParser dubboParser = new DubboParser();
    private final FeignParser feignParser = new FeignParser();
    private final RestControllerParser restParser = new RestControllerParser();
    private final MyBatisParser myBatisParser = new MyBatisParser();
    private final JpaEntityParser jpaEntityParser = new JpaEntityParser();

    public List<ParsedRelation> parseJavaFile(String sourceCode) {
        List<ParsedRelation> all = new ArrayList<>();
        all.addAll(dubboParser.parse(sourceCode));
        all.addAll(feignParser.parse(sourceCode));
        all.addAll(restParser.parse(sourceCode));
        all.addAll(myBatisParser.parseAnnotation(sourceCode));
        all.addAll(jpaEntityParser.parse(sourceCode));
        return all;
    }

    public List<ParsedRelation> parseMyBatisXml(String xmlContent) {
        return myBatisParser.parseXml(xmlContent);
    }
}
