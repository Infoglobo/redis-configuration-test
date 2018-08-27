package br.com.globo.support;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Matchers {

    private Matchers() {

    }

    public static Optional<List<Optional<String>>> matchGroupsAsList(String input, String regex) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {

            return Optional.of(IntStream.rangeClosed(0, matcher.groupCount())
                .skip(1).mapToObj(i ->  matcher.group(i))
                .map(s -> Optional.ofNullable(s))
                .collect(toList()));
        }

        return Optional.empty();
    }
}