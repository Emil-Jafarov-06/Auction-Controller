package com.project.msbidding.bid.model.dto;

import java.util.List;

public record PageResponse<T>(List<T> content, int pageNumber, int pageSize, long totalCount, int totalPages) {}