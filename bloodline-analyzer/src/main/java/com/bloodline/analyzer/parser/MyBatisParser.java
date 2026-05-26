package com.bloodline.analyzer.parser;

import com.bloodline.analyzer.model.ParsedRelation;
import com.github.javaparser.JavaParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MyBatisParser {
    private final JavaParser javaParser = new JavaParser();

    public List<ParsedRelation> parseXml(String xmlContent) {
        List<ParsedRelation> relations = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            String[] tags = {"select", "insert", "update", "delete"};
            for (String tag : tags) {
                NodeList nodes = doc.getElementsByTagName(tag);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element elem = (Element) nodes.item(i);
                    List<String> sqlTexts = extractSqlTexts(doc, elem);
                    for (String sql : sqlTexts) {
                        relations.addAll(extractTablesFromSql(sql, tag.toUpperCase()));
                    }
                }
            }
        } catch (Exception e) {
            // Log warning
        }
        return relations;
    }

    private List<String> extractSqlTexts(Document doc, Element elem) {
        List<String> texts = new ArrayList<>();

        // 1. Try the full element text (with includes resolved)
        String fullText = resolveIncludes(doc, elem).trim();
        if (!fullText.isEmpty()) {
            texts.add(fullText);
        }

        // 2. Also extract text from dynamic SQL sub-elements
        String[] dynamicTags = {"if", "when", "otherwise", "foreach"};
        for (String dynTag : dynamicTags) {
            NodeList dynNodes = elem.getElementsByTagName(dynTag);
            for (int j = 0; j < dynNodes.getLength(); j++) {
                Element dynElem = (Element) dynNodes.item(j);
                String dynText = resolveIncludes(doc, dynElem).trim();
                if (!dynText.isEmpty() && !dynText.equals(fullText)) {
                    texts.add(dynText);
                }
            }
        }

        return texts;
    }

    private String resolveIncludes(Document doc, Element elem) {
        StringBuilder sb = new StringBuilder();
        NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                sb.append(child.getTextContent());
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElem = (Element) child;
                if (childElem.getTagName().equals("include")) {
                    String refid = childElem.getAttribute("refid");
                    if (refid != null && !refid.isEmpty()) {
                        NodeList sqlNodes = doc.getElementsByTagName("sql");
                        for (int j = 0; j < sqlNodes.getLength(); j++) {
                            Element sqlElem = (Element) sqlNodes.item(j);
                            if (refid.equals(sqlElem.getAttribute("id"))) {
                                sb.append(resolveIncludes(doc, sqlElem));
                                break;
                            }
                        }
                    }
                } else {
                    sb.append(resolveIncludes(doc, childElem));
                }
            }
        }
        return sb.toString();
    }

    public List<ParsedRelation> parseAnnotation(String sourceCode) {
        List<ParsedRelation> relations = new ArrayList<>();

        javaParser.parse(sourceCode).ifSuccessful(cu -> {
            cu.findAll(com.github.javaparser.ast.expr.AnnotationExpr.class).forEach(ann -> {
                String name = ann.getNameAsString();
                if (name.equals("Select") || name.equals("Insert") || name.equals("Update") || name.equals("Delete")) {
                    if (ann.isSingleMemberAnnotationExpr()) {
                        String sql = ann.asSingleMemberAnnotationExpr().getMemberValue().toString();
                        sql = sql.replace("\"", "").trim();
                        relations.addAll(extractTablesFromSql(sql, name.toUpperCase()));
                    }
                }
            });
        });

        return relations;
    }

    private List<ParsedRelation> extractTablesFromSql(String sql, String operation) {
        List<ParsedRelation> relations = new ArrayList<>();
        try {
            sql = sql.replaceAll("#\\{[^}]*\\}", "?");
            Statement stmt = CCJSqlParserUtil.parse(sql);
            TablesNamesFinder finder = new TablesNamesFinder();
            List<String> tables = finder.getTableList(stmt);

            for (String table : tables) {
                ParsedRelation rel = new ParsedRelation("QUERIES", "TABLE", table);
                rel.setTargetDetail(operation);
                relations.add(rel);
            }
        } catch (Exception e) {
            // SQL parse failed
        }
        return relations;
    }
}
