package com.time.tdd.rest;

import java.util.Map;
import java.util.Optional;

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

