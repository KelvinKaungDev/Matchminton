package com.badminton_manager.badminton.service.rotation;

import com.badminton_manager.badminton.dto.rotation.RotationCourtResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationHistoryEntryResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationHistoryPlayerDTO;
import com.badminton_manager.badminton.dto.rotation.RotationPlayerResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationSessionResponseDTO;
import com.badminton_manager.badminton.dto.rotation.RotationStateResponseDTO;
import com.badminton_manager.badminton.enums.SkillLevel;
import com.badminton_manager.badminton.enums.Team;
import com.badminton_manager.badminton.model.RotationCourt;
import com.badminton_manager.badminton.model.RotationPlayer;
import com.badminton_manager.badminton.model.RotationRoundHistoryEntry;
import com.badminton_manager.badminton.model.RotationSession;
import com.badminton_manager.badminton.repository.RotationCourtRepository;
import com.badminton_manager.badminton.repository.RotationPlayerRepository;
import com.badminton_manager.badminton.repository.RotationRoundHistoryEntryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RotationStateAssembler {

    // Names may contain commas, so a control character is used as the join delimiter
    // instead of a comma when encoding round-history snapshots.
    static final String DELIM = "";

    private final RotationPlayerRepository playerRepository;
    private final RotationCourtRepository courtRepository;
    private final RotationRoundHistoryEntryRepository historyRepository;

    public RotationStateAssembler(RotationPlayerRepository playerRepository,
                                   RotationCourtRepository courtRepository,
                                   RotationRoundHistoryEntryRepository historyRepository) {
        this.playerRepository = playerRepository;
        this.courtRepository = courtRepository;
        this.historyRepository = historyRepository;
    }

    public RotationStateResponseDTO build(RotationSession session) {
        UUID sessionId = session.getId();
        List<RotationPlayer> allPlayers = playerRepository.findByRotationSessionId(sessionId);
        List<RotationCourt> allCourts = courtRepository.findByRotationSessionIdOrderByCourtNumberAsc(sessionId);
        List<RotationRoundHistoryEntry> historyEntries =
                historyRepository.findByRotationSessionIdOrderByRoundNumberAsc(sessionId);

        Map<UUID, List<RotationPlayer>> byCourt = allPlayers.stream()
                .filter(p -> p.getCourt() != null)
                .collect(Collectors.groupingBy(p -> p.getCourt().getId()));

        List<RotationCourtResponseDTO> courtDtos = allCourts.stream().map(court -> {
            List<RotationPlayer> courtPlayers = byCourt.getOrDefault(court.getId(), List.of());
            RotationCourtResponseDTO dto = new RotationCourtResponseDTO();
            dto.setId(court.getId());
            dto.setCourtNumber(court.getCourtNumber());
            dto.setName(court.getName() != null ? court.getName() : String.valueOf(court.getCourtNumber()));
            dto.setTeamA(courtPlayers.stream().filter(p -> p.getTeam() == Team.teamA).map(this::toPlayerResponse).toList());
            dto.setTeamB(courtPlayers.stream().filter(p -> p.getTeam() == Team.teamB).map(this::toPlayerResponse).toList());
            return dto;
        }).toList();

        RotationStateResponseDTO state = new RotationStateResponseDTO();
        state.setSession(toSessionResponse(session));
        state.setPlayers(allPlayers.stream().map(this::toPlayerResponse).toList());
        state.setCourts(courtDtos);
        state.setHistory(historyEntries.stream().map(this::toHistoryResponse).toList());
        return state;
    }

    public RotationSessionResponseDTO toSessionResponse(RotationSession session) {
        RotationSessionResponseDTO dto = new RotationSessionResponseDTO();
        dto.setId(session.getId());
        dto.setOrganizerId(session.getOrganizer().getId());
        dto.setOrganizerName(session.getOrganizer().getName());
        dto.setCourts(session.getCourts());
        dto.setMaxRoundsPerPlayer(session.getMaxRoundsPerPlayer());
        dto.setMaxPlayers(session.getMaxPlayers());
        dto.setFullRoundPrice(session.getFullRoundPrice());
        dto.setCourtNames(decodeStringList(session.getCourtNamesCsv()));
        dto.setScreen(session.getScreen());
        dto.setCreatedAt(session.getCreatedAt());
        return dto;
    }

    public String encodeStringList(List<String> values) {
        return values == null ? "" : String.join(DELIM, values);
    }

    public List<String> decodeStringList(String encoded) {
        if (encoded == null || encoded.isEmpty()) return List.of();
        return List.of(encoded.split(DELIM, -1));
    }

    /** Court's custom name if configured, falling back to the plain court number. */
    public String resolveCourtName(RotationSession session, int courtNumber) {
        List<String> names = decodeStringList(session.getCourtNamesCsv());
        if (courtNumber - 1 < names.size()) {
            String candidate = names.get(courtNumber - 1);
            if (candidate != null && !candidate.isBlank()) return candidate;
        }
        return String.valueOf(courtNumber);
    }

    public RotationPlayerResponseDTO toPlayerResponse(RotationPlayer player) {
        RotationPlayerResponseDTO dto = new RotationPlayerResponseDTO();
        dto.setId(player.getId());
        dto.setRotationSessionId(player.getRotationSession().getId());
        dto.setName(player.getName());
        dto.setSkill(player.getSkill());
        dto.setStatus(player.getStatus());
        dto.setRoundsPlayed(player.getRoundsPlayed());
        dto.setCourtId(player.getCourt() != null ? player.getCourt().getId() : null);
        dto.setTeam(player.getTeam());
        dto.setCreatedAt(player.getCreatedAt());
        return dto;
    }

    public String encodeNames(List<RotationPlayer> players) {
        return players.stream().map(RotationPlayer::getName).collect(Collectors.joining(DELIM));
    }

    public String encodeSkills(List<RotationPlayer> players) {
        return players.stream().map(p -> p.getSkill().name()).collect(Collectors.joining(DELIM));
    }

    private RotationHistoryEntryResponseDTO toHistoryResponse(RotationRoundHistoryEntry entry) {
        RotationHistoryEntryResponseDTO dto = new RotationHistoryEntryResponseDTO();
        dto.setRoundNumber(entry.getRoundNumber());
        dto.setCourtNumber(entry.getCourtNumber());
        dto.setCourtName(entry.getCourtName());
        dto.setTeamA(decodeTeam(entry.getTeamANamesCsv(), entry.getTeamASkillsCsv()));
        dto.setTeamB(decodeTeam(entry.getTeamBNamesCsv(), entry.getTeamBSkillsCsv()));
        return dto;
    }

    private List<RotationHistoryPlayerDTO> decodeTeam(String namesEncoded, String skillsEncoded) {
        String[] names = namesEncoded.isEmpty() ? new String[0] : namesEncoded.split(DELIM, -1);
        String[] skills = skillsEncoded.isEmpty() ? new String[0] : skillsEncoded.split(DELIM, -1);
        return java.util.stream.IntStream.range(0, names.length)
                .mapToObj(i -> {
                    RotationHistoryPlayerDTO p = new RotationHistoryPlayerDTO();
                    p.setName(names[i]);
                    p.setSkill(SkillLevel.valueOf(skills[i]));
                    return p;
                })
                .toList();
    }
}
