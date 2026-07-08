package com.badminton_manager.badminton.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BinarySearchUtilTest {

    private final List<Integer> sorted = List.of(1, 3, 3, 3, 5, 8);

    @Test
    void lowerBound_findsFirstIndexNotLessThanTarget() {
        assertThat(BinarySearchUtil.lowerBound(sorted, x -> x - 3)).isEqualTo(1);
        assertThat(BinarySearchUtil.lowerBound(sorted, x -> x - 0)).isEqualTo(0);
        assertThat(BinarySearchUtil.lowerBound(sorted, x -> x - 9)).isEqualTo(6);
    }

    @Test
    void upperBound_findsFirstIndexGreaterThanTarget() {
        assertThat(BinarySearchUtil.upperBound(sorted, x -> x - 3)).isEqualTo(4);
        assertThat(BinarySearchUtil.upperBound(sorted, x -> x - 0)).isEqualTo(0);
        assertThat(BinarySearchUtil.upperBound(sorted, x -> x - 9)).isEqualTo(6);
    }

    @Test
    void equalRange_isolatesAllOccurrencesOfTarget() {
        int from = BinarySearchUtil.lowerBound(sorted, x -> x - 3);
        int to = BinarySearchUtil.upperBound(sorted, x -> x - 3);

        assertThat(sorted.subList(from, to)).containsExactly(3, 3, 3);
    }

    @Test
    void emptyList_returnsZeroForBothBounds() {
        List<Integer> empty = List.of();

        assertThat(BinarySearchUtil.lowerBound(empty, x -> x - 5)).isZero();
        assertThat(BinarySearchUtil.upperBound(empty, x -> x - 5)).isZero();
    }

    @Test
    void noMatch_lowerAndUpperBoundAreEqual() {
        List<Integer> sortedNoFour = List.of(1, 3, 5, 8);

        int from = BinarySearchUtil.lowerBound(sortedNoFour, x -> x - 4);
        int to = BinarySearchUtil.upperBound(sortedNoFour, x -> x - 4);

        assertThat(from).isEqualTo(to);
        assertThat(sortedNoFour.subList(from, to)).isEmpty();
    }
}
