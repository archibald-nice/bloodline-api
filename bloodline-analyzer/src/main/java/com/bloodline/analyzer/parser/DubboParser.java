package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import java.util.ArrayList;
import java.util.List;

public class DubboParser {
    private final JavaParser javaParser = new JavaParser();

    public List<ParsedRelation> parse(String sourceCode) {
        List<ParsedRelation> relations = new ArrayList<>();

        javaParser.parse(sourceCode).ifSuccessful(cu -> {
            cu.findAll(FieldDeclaration.class).forEach(field -> {
                field.getAnnotations().forEach(ann -> {
                    String name = ann.getNameAsString();
                    if (name.equals("DubboReference") || name.equals("Reference")) {
                        field.getVariables().forEach(var -> {
                            relations.add(new ParsedRelation("CALLS", "SERVICE", var.getType().asString()));
                        });
                    }
                });
            });

            for (TypeDeclaration<?> type : cu.findAll(TypeDeclaration.class)) {
                for (AnnotationExpr ann : type.getAnnotations()) {
                    String name = ann.getNameAsString();
                    if (name.equals("DubboService") || name.equals("Service")) {
                        if (type.isClassOrInterfaceDeclaration()) {
                            type.asClassOrInterfaceDeclaration().getImplementedTypes().forEach(impl -> {
                                relations.add(new ParsedRelation("EXPOSES", "SERVICE", impl.getNameAsString()));
                            });
                        }
                    }
                }
            }
        });

        return relations;
    }
}
