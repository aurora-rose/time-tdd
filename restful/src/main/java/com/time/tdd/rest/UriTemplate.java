package com.time.tdd.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author XuJian
 * @date 2023-04-19 22:04
 **/
interface UriTemplate {
    Optional<MatchResult> match(String path);

    interface MatchResult extends Comparable<MatchResult> {
        String getMatched();

        String getRemaining();

        Map<String, String> getMatchedPathParameters();
    }
}

class PathTemplate implements UriTemplate {


    public final int variableGroupStartFrom;
    private final Pattern pattern;
    private final PathVariables pathVariables = new PathVariables();


    public PathTemplate(String template) {
        pattern = Pattern.compile(group(pathVariables.template(template)) + "(/.*)?");
        variableGroupStartFrom = 2;
    }

    private static String group(String pattern) {
        return "(" + pattern + ")";
    }

    @Override
    public Optional<MatchResult> match(String path) {
        Matcher matcher = pattern.matcher(path);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.of(new PathMatchResult(matcher, pathVariables));
    }

    class PathVariables implements Comparable<PathVariables> {
        public static final String DEFAULT_VARIABLE_PATTERN = "([^/]+?)";
        private static final String LEFT_BRACKET = "\\{";
        private static final String RIGHT_BRACKET = "}";
        private static final String VARIABLE_NAME = "\\w[\\w\\.-]*";
        private static final String NON_BRACKET = "[^\\{}]+";
        private static final Pattern VARIABLE = Pattern.compile(LEFT_BRACKET
            + group(VARIABLE_NAME)
            + group(":" + group(NON_BRACKET)) + "?"
            + RIGHT_BRACKET);
        private static final int VARIABLE_NAME_GROUP = 1;
        private static final int VARIABLE_PATTERN_GROUP = 3;
        private final List<String> variables = new ArrayList<>();
        private int specificPatternCount = 0;


        private String template(String template) {
            return VARIABLE.matcher(template).replaceAll(pathVariables::replace);
        }


        private String replace(java.util.regex.MatchResult result) {
            String name = result.group(VARIABLE_NAME_GROUP);
            if (variables.contains(name)) {
                throw new IllegalArgumentException(" duplicate variable " + name);
            }
            String patternGroup = result.group(VARIABLE_PATTERN_GROUP);
            variables.add(name);
            if (patternGroup != null) {
                specificPatternCount++;
                return group(patternGroup);
            }
            return DEFAULT_VARIABLE_PATTERN;
        }

        public Map<String, String> extract(Matcher matcher) {
            Map<String, String> parameters = new HashMap<>();
            for (int i = 0; i < variables.size(); i++) {
                parameters.put(variables.get(i), matcher.group(variableGroupStartFrom + i));
            }
            return parameters;
        }


        @Override
        public int compareTo(PathVariables o) {
            if (variables.size() > o.variables.size()) {
                return -1;
            }
            if (variables.size() < o.variables.size()) {
                return 1;
            }
            return Integer.compare(o.specificPatternCount, specificPatternCount);
        }
    }

    class PathMatchResult implements MatchResult {
        private final Matcher matcher;
        private final Map<String, String> parameters;
        private final PathVariables variables;
        private final int matchLiteralCount;

        public PathMatchResult(Matcher matcher, PathVariables variables) {
            this.matcher = matcher;
            this.variables = variables;
            this.parameters = variables.extract(matcher);
            this.matchLiteralCount =
                matcher.group(1).length() - parameters.values().stream().map(String::length).reduce(0, Integer::sum);
        }

        @Override
        public String getMatched() {
            return matcher.group(1);
        }

        @Override
        public String getRemaining() {
            return matcher.group(matcher.groupCount());
        }

        @Override
        public Map<String, String> getMatchedPathParameters() {
            return parameters;
        }

        @Override
        public int compareTo(MatchResult o) {
            PathMatchResult result = (PathMatchResult) o;
            if (matchLiteralCount > result.matchLiteralCount) {
                return -1;
            }
            if (matchLiteralCount < result.matchLiteralCount) {
                return 1;
            }
            return variables.compareTo(result.variables);
        }
    }
}

