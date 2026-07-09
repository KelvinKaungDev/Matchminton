package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.enums.RotationPlayerStatus;
import com.badminton_manager.badminton.model.RotationPlayer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Java port of the old React app's src/services/matching.js — same
 * least-rounds-played-first, shuffle-within-group selection strategy.
 */
@Component
public class RotationMatcher {

    public record MatchGroup(List<RotationPlayer> teamA, List<RotationPlayer> teamB) {}

    public List<MatchGroup> generateRound(List<RotationPlayer> allPlayers, int numCourts) {
        List<RotationPlayer> bench = allPlayers.stream()
                .filter(p -> p.getStatus() == RotationPlayerStatus.bench)
                .toList();
        List<RotationPlayer> done = allPlayers.stream()
                .filter(p -> p.getStatus() == RotationPlayerStatus.done)
                .toList();

        List<RotationPlayer> pool = new ArrayList<>(sortByRoundsShuffled(bench));
        int needed = numCourts * 4;
        if (pool.size() < needed) {
            pool.addAll(sortByRoundsShuffled(done));
        }

        int activeCourts = Math.min(numCourts, pool.size() / 4);
        List<RotationPlayer> selected = new ArrayList<>(pool.subList(0, activeCourts * 4));
        Collections.shuffle(selected);

        List<MatchGroup> groups = new ArrayList<>();
        for (int i = 0; i < activeCourts; i++) {
            List<RotationPlayer> four = selected.subList(i * 4, i * 4 + 4);
            groups.add(new MatchGroup(new ArrayList<>(four.subList(0, 2)), new ArrayList<>(four.subList(2, 4))));
        }
        return groups;
    }

    public RotationPlayer findBestSub(List<RotationPlayer> candidates) {
        List<RotationPlayer> bench = candidates.stream()
                .filter(p -> p.getStatus() == RotationPlayerStatus.bench)
                .toList();
        if (!bench.isEmpty()) {
            return bench.stream().min(Comparator.comparingInt(RotationPlayer::getRoundsPlayed)).orElseThrow();
        }
        List<RotationPlayer> done = candidates.stream()
                .filter(p -> p.getStatus() == RotationPlayerStatus.done)
                .toList();
        if (!done.isEmpty()) {
            return done.stream().min(Comparator.comparingInt(RotationPlayer::getRoundsPlayed)).orElseThrow();
        }
        return null;
    }

    private List<RotationPlayer> sortByRoundsShuffled(List<RotationPlayer> players) {
        Map<Integer, List<RotationPlayer>> groups = players.stream()
                .collect(Collectors.groupingBy(RotationPlayer::getRoundsPlayed, TreeMap::new, Collectors.toList()));
        List<RotationPlayer> result = new ArrayList<>();
        for (List<RotationPlayer> group : groups.values()) {
            List<RotationPlayer> shuffled = new ArrayList<>(group);
            Collections.shuffle(shuffled);
            result.addAll(shuffled);
        }
        return result;
    }
}
