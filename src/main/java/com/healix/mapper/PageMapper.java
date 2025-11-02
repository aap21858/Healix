package com.healix.mapper;

import com.healix.model.PatientPageResponse;
import com.healix.model.PatientResponse;
import com.healix.model.Pageable;
import com.healix.model.Sort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

    public PatientPageResponse toPatientPageResponse(Page<PatientResponse> page) {
        PatientPageResponse response = new PatientPageResponse();

        // Content
        response.setContent(page.getContent());

        // Pagination metadata
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setNumberOfElements(page.getNumberOfElements());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setEmpty(page.isEmpty());

        // Pageable
        response.setPageable(toPageable(page.getPageable()));

        return response;
    }

    /**
     * Convert Spring Pageable to OpenAPI Pageable
     */
    private Pageable toPageable(org.springframework.data.domain.Pageable pageable) {
        Pageable openApiPageable = new Pageable();

        openApiPageable.setPageNumber(pageable.getPageNumber());
        openApiPageable.setPageSize(pageable.getPageSize());
        openApiPageable.setOffset(pageable.getOffset());
        openApiPageable.setPaged(pageable.isPaged());
        openApiPageable.setUnpaged(pageable.isUnpaged());
        openApiPageable.setSort(toSort(pageable.getSort()));

        return openApiPageable;
    }

    /**
     * Convert Spring Sort to OpenAPI Sort
     */
    private Sort toSort(org.springframework.data.domain.Sort sort) {
        Sort openApiSort = new Sort();

        openApiSort.setEmpty(sort.isEmpty());
        openApiSort.setSorted(sort.isSorted());
        openApiSort.setUnsorted(sort.isUnsorted());

        return openApiSort;
    }
}