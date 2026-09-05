package com.college.voting.dto;

import com.college.voting.entity.Election;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElectionResponse {

    private Long id;

    private String name;

    private String description;

    private String academicYear;

    private String department;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private String rules;

    private String status;

    private boolean showLiveResults;

    private List<PositionResponse> positions;


    public static ElectionResponse fromEntity(Election e) {

        return ElectionResponse.builder()

                .id(e.getId())

                .name(e.getName())

                .description(e.getDescription())

                .academicYear(e.getAcademicYear())

                .department(e.getDepartment())

                .startDateTime(e.getStartDateTime())

                .endDateTime(e.getEndDateTime())

                .rules(e.getRules())

                .status(e.getEffectiveStatus().name())

                .showLiveResults(e.isShowLiveResults())

                .build();
    }


    public static ElectionResponse fromEntityWithPositions(Election e) {

        ElectionResponse response = fromEntity(e);

        response.setPositions(
                e.getPositions()
                        .stream()
                        .map(PositionResponse::fromEntity)
                        .collect(Collectors.toList())
        );

        return response;
    }
}