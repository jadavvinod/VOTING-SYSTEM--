package com.college.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvImportSummaryResponse {
    private int totalRecords;
    private int successful;
    private int failed;
    private int duplicates;
    private List<String> errors;
    private String defaultPasswordNote;
}
