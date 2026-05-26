package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.ArrayList;
import java.util.List;

public class FeignParser {
    private final JavaParser javaParser = new JavaParser();

    public List<ParsedRelation> parse(String sourceCode) {
        List<ParsedRelation> relations = new ArrayList<>();

        javaParser.parse(sourceCode).ifSuccessful(cu -> {
            cu.findAll(AnnotationExpr.class).forEach(ann -> {
                if (ann.getNameAsString().equals("FeignClient")) {
                    ParsedRelation rel = new ParsedRelation("HTTP_CALLS", "API_ENDPOINT", null);

                    if (ann.isNormalAnnotationExpr()) {
                        ann.asNormalAnnotationExpr().getPairs().forEach(pair -> {
                            String pairName = pair.getNameAsString();
                            String pairValue = pair.getValue().toString().replace("\"", "");

                            if (pairName.equals("name")) {
                                rel.setTargetAppId(pairValue);
                            }
                            if (pairName.equals("value") && rel.getTargetAppId() == null) {
                                rel.setTargetAppId(pairValue);
                            }
                            if (pairName.equals("path")) {
                                rel.setTargetName(normalizePath(pairValue));
                            }
                            if (pairName.equals("url")) {
                                rel.setTargetName(extractPathFromUrl(pairValue));
                            }
                        });
                    } else if (ann.isSingleMemberAnnotationExpr()) {
                        rel.setTargetAppId(ann.asSingleMemberAnnotationExpr().getMemberValue().toString().replace("\"", ""));
                    }

                    relations.add(rel);
                }
            });
        });

        return relations;
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String extractPathFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            int hostEnd = url.indexOf('/', url.indexOf("://") + 3);
            if (hostEnd != -1) {
                return url.substring(hostEnd);
            }
            return "/";
        }
        return normalizePath(url);
    }
}
