// SPDX-License-Identifier: Apache-2.0
package org.hiero.gradle.spotless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProtoCommentLinter {
    private static final Pattern DOC_COMMENT_START = Pattern.compile("^\\s*/\\*\\*");
    private static final Pattern DOC_COMMENT_END = Pattern.compile("^\\s*\\*/");
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^\\s*message\\s+\\w+\\s*(\\{)?$");
    private static final Pattern FIELD_PATTERN =
            Pattern.compile("^\\s*(optional|required|repeated)?\\s*[.\\w]+\\s+\\w+\\s*=\\s*\\d+;");
    private static final Pattern INLINE_COMMENT_PATTERN = Pattern.compile("^//.*");

    public static String lint(String protoContent) {
        List<String> lines = Arrays.asList(protoContent.split("\n"));
        List<String> issues = new ArrayList<>();
        boolean inDocComment = false;
        boolean isMessage = false;
        int commentStartIndex = -1;
        List<String> commentLines = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (INLINE_COMMENT_PATTERN.matcher(line).matches()) {
                issues.add("⚠️ Warning: Inline comment detected at line " + (i + 1) + ". Use `/** ... */` instead.");
            }

            if (DOC_COMMENT_START.matcher(line).matches()) {
                inDocComment = true;
                commentStartIndex = i;
                commentLines.clear();
            }

            if (inDocComment) {
                commentLines.add(line);
            }

            if (DOC_COMMENT_END.matcher(line).matches()) {
                inDocComment = false;
                issues.addAll(validateComment(commentLines, isMessage, commentStartIndex + 1));
            }

            if (MESSAGE_PATTERN.matcher(line).matches()) {
                isMessage = true;
            } else if (FIELD_PATTERN.matcher(line).matches()) {
                isMessage = false;
            }
        }

        if (!issues.isEmpty()) {
            throw new RuntimeException("Proto file linting failed:\n" + String.join("\n", issues));
        }

        return protoContent;
    }

    private static List<String> validateComment(List<String> lines, boolean isMessage, int lineNumber) {
        List<String> issues = new ArrayList<>();

        if (lines.size() < 3) {
            issues.add("⚠️ Warning: Comment at line " + lineNumber + " is too short.");
            return issues;
        }

        String firstLine = lines.get(1).replaceAll("\\*", "").trim();
        if (!firstLine.endsWith(".")) {
            issues.add("❌ Error at line " + lineNumber + ": First line should end with a period.");
        }

        String secondLine = lines.get(2).replaceAll("\\*", "").trim();
        if (!secondLine.isEmpty() && !secondLine.equals("<br/>")) {
            issues.add("❌ Error at line " + lineNumber + ": Second line should be blank or contain `<br/>`.");
        }

        if (isMessage && lines.size() > 3) {
            String thirdLine = lines.get(3).replaceAll("\\*", "").trim();
            if (thirdLine.isEmpty()) {
                issues.add("⚠️ Warning at line " + lineNumber + ": Message description should be in the second line.");
            }
        }

        issues.addAll(checkRequirements(lines, lineNumber));
        return issues;
    }

    private static List<String> checkRequirements(List<String> lines, int lineNumber) {
        List<String> issues = new ArrayList<>();
        boolean paragraphBreakFound = false;

        for (int i = 2; i < lines.size(); i++) {
            String line = lines.get(i).replaceAll("\\*", "").trim();

            if (line.isEmpty() || line.equals("<p>")) {
                paragraphBreakFound = true;
            }

            if (line.contains("MUST") || line.contains("SHOULD") || line.contains("REQUIRED")) {
                if (!paragraphBreakFound) {
                    issues.add("❌ Error at line " + (lineNumber + i)
                            + ": Requirement must have a paragraph break (`<p>` or blank line).");
                }
            }
        }

        return issues;
    }

    public static String lintAndFix(String protoContent) {
        List<String> lines = Arrays.asList(protoContent.split("\n"));
        StringBuilder fixedContent = new StringBuilder();

        for (String line : lines) {
            Matcher matcher = INLINE_COMMENT_PATTERN.matcher(line);

            if (matcher.matches()) {
                String indentation = matcher.group(1);
                String commentText = matcher.group(2).trim();
                // Auto-fix: Convert inline `//` comments to new line format
                fixedContent
                        .append(indentation)
                        .append("//")
                        .append("\n")
                        .append(indentation)
                        .append("// ")
                        .append(commentText)
                        .append("\n");
            } else {
                fixedContent.append(line).append("\n");
            }
        }

        return fixedContent.toString().trim();
    }
}
