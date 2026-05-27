package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;

import java.util.ArrayList;
import java.util.List;

public class JpaEntityParser {
    private final JavaParser javaParser = new JavaParser();

    public List<ParsedRelation> parse(String sourceCode) {
        List<ParsedRelation> relations = new ArrayList<>();

        javaParser.parse(sourceCode).ifSuccessful(cu -> {
            for (TypeDeclaration<?> type : cu.findAll(TypeDeclaration.class)) {
                if (!hasEntityAnnotation(type, cu)) {
                    continue;
                }

                String tableName = extractTableName(type);
                if (tableName == null || tableName.isEmpty()) {
                    tableName = type.getNameAsString();
                }

                ParsedRelation tableRel = new ParsedRelation("OWNS", "TABLE", tableName);
                relations.add(tableRel);

                for (FieldDeclaration field : type.findAll(FieldDeclaration.class)) {
                    String columnName = extractColumnName(field);
                    if (columnName != null && !columnName.isEmpty()) {
                        ParsedRelation colRel = new ParsedRelation("OWNS", "COLUMN", columnName);
                        colRel.setTargetDetail(tableName);
                        relations.add(colRel);
                    }
                }
            }
        });

        return relations;
    }

    private boolean hasEntityAnnotation(TypeDeclaration<?> type, CompilationUnit cu) {
        for (AnnotationExpr ann : type.getAnnotations()) {
            String name = ann.getNameAsString();
            if (name.equals("Entity")) {
                return true;
            }
        }
        return false;
    }

    private String extractTableName(TypeDeclaration<?> type) {
        for (AnnotationExpr ann : type.getAnnotations()) {
            if (ann.getNameAsString().equals("Table")) {
                return extractNameAttribute(ann);
            }
        }
        return null;
    }

    private String extractColumnName(FieldDeclaration field) {
        for (AnnotationExpr ann : field.getAnnotations()) {
            if (ann.getNameAsString().equals("Column")) {
                String name = extractNameAttribute(ann);
                if (name != null && !name.isEmpty()) {
                    return name;
                }
                return field.getVariables().get(0).getNameAsString();
            }
        }
        return null;
    }

    private String extractNameAttribute(AnnotationExpr ann) {
        if (ann.isNormalAnnotationExpr()) {
            NormalAnnotationExpr normalAnn = ann.asNormalAnnotationExpr();
            for (MemberValuePair pair : normalAnn.getPairs()) {
                if (pair.getNameAsString().equals("name")) {
                    return pair.getValue().toString().replace("\"", "");
                }
            }
        }
        return null;
    }
}
