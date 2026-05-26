package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.ArrayList;
import java.util.List;

public class RestControllerParser {
    private final JavaParser javaParser = new JavaParser();

    public List<ParsedRelation> parse(String sourceCode) {
        List<ParsedRelation> relations = new ArrayList<>();

        javaParser.parse(sourceCode).ifSuccessful(cu -> {
            String basePath = "";

            for (AnnotationExpr ann : cu.findAll(AnnotationExpr.class)) {
                if (ann.getNameAsString().equals("RequestMapping")) {
                    if (ann.isNormalAnnotationExpr()) {
                        for (com.github.javaparser.ast.expr.MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                            if (pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path")) {
                                basePath = pair.getValue().toString().replace("\"", "");
                            }
                        }
                    } else if (ann.isSingleMemberAnnotationExpr()) {
                        basePath = ann.asSingleMemberAnnotationExpr().getMemberValue().toString().replace("\"", "");
                    }
                }
            }

            for (AnnotationExpr ann : cu.findAll(AnnotationExpr.class)) {
                String name = ann.getNameAsString();
                if (name.endsWith("Mapping") && !name.equals("RequestMapping")) {
                    String method = name.replace("Mapping", "").toUpperCase();
                    String path = basePath;

                    if (ann.isSingleMemberAnnotationExpr()) {
                        String memberValue = ann.asSingleMemberAnnotationExpr().getMemberValue().toString().replace("\"", "");
                        path = joinPaths(basePath, memberValue);
                    } else if (ann.isNormalAnnotationExpr()) {
                        for (com.github.javaparser.ast.expr.MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                            if (pair.getNameAsString().equals("value") || pair.getNameAsString().equals("path")) {
                                String memberValue = pair.getValue().toString().replace("\"", "");
                                path = joinPaths(basePath, memberValue);
                            }
                        }
                    }

                    ParsedRelation rel = new ParsedRelation("EXPOSES", "API_ENDPOINT", path);
                    rel.setTargetDetail(method);
                    relations.add(rel);
                }
            }
        });

        return relations;
    }

    private String joinPaths(String base, String sub) {
        if (base == null || base.isEmpty()) {
            return sub.startsWith("/") ? sub : "/" + sub;
        }
        if (sub == null || sub.isEmpty()) {
            return base;
        }
        String normalizedBase = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String normalizedSub = sub.startsWith("/") ? sub : "/" + sub;
        return normalizedBase + normalizedSub;
    }
}
