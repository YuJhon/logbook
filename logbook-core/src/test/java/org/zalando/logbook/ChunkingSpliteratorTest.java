package org.zalando.logbook;

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Spliterator.SIZED;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ChunkingSpliteratorTest {

    @Test
    public void shouldEstimateSizeWithoutTrailingPart() {
        assertThat(new ChunkingSpliterator("Hello", 5, 5).estimateSize(), is(1L));
    }

    @Test
    public void shouldEstimateSizeWithTrailingPart() {
        assertThat(new ChunkingSpliterator("Hello World", 5, 5).estimateSize(), is(3L));
    }

    @Test
    public void shouldNotSupportPartitions() {
        assertThat(new ChunkingSpliterator("", 1, 1).trySplit(), is(nullValue()));
    }

    @Test
    public void shouldBeSizedWhenMinEqualToMax() {
        assertTrue((new ChunkingSpliterator("Hello", 5, 5).characteristics() & SIZED) != 0);
    }

    @Test
    public void shouldNotBeSizedWhenMinIsNotEqualToMax() {
        assertTrue((new ChunkingSpliterator("Hello", 4, 5).characteristics() & SIZED) == 0);
    }

    @Test
    public void shouldFailWhenMinIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new ChunkingSpliterator("whatever", 0, 10));
    }

    @Test
    public void shouldFailWhenMaxIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
            new ChunkingSpliterator("whatever", 10, 0));
    }

    @Test
    public void shouldFailWhenMinGreaterThanMax() {
        assertThrows(IllegalArgumentException.class, () ->
            new ChunkingSpliterator("whatever", 11, 10));
    }

    @Test
    public void shouldSplitAfterSplitCharacter() {
        assertThat(split("12345 67890", 5, 6), is(asList("12345 ", "67890")));
        assertThat(split("12345:67890", 5, 6), is(asList("12345:", "67890")));
        assertThat(split("12345,67890", 5, 6), is(asList("12345,", "67890")));
    }

    @Test
    public void shouldSplitAfterSplitCharacterWhenChunkLenghtIsMinimal() {
        assertThat(split("12345 67890", 6, 7), is(asList("12345 ", "67890")));
    }

    @Test
    public void shouldSplitOnMaxWhenNoSplitCharacterPresent() {
        assertThat(split("123456 789012", 5, 6), is(asList("123456", " 78901", "2")));
    }

    @Test
    public void shouldNotSplitWhenMaxIsEqualToLength() {
        assertThat(split("123 45", 1, 6), is(singletonList("123 45")));
    }

    @Test
    public void shouldNotSplitWhenMaxIsGreaterThanLength() {
        assertThat(split("123 45", 1, 10), is(singletonList("123 45")));
    }

    @Test
    public void shouldSplitWithMinimalChunkLenOfOne() {
        assertThat(split(" space", 1, 5), is(asList(" ", "space")));
    }

    private static List<String> split(final String string, final int min, final int max) {
        return stream(new ChunkingSpliterator(string, min, max), false).collect(toList());
    }
}
