package com.assetvault.service;

import com.assetvault.dto.SoftwareLicenseRequest;
import com.assetvault.dto.SoftwareLicenseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for software license operations.
 */
public interface SoftwareLicenseService {
    /**
     * Creates a new software license.
     *
     * @param request the request payload
     * @return the resulting software license
     */
    SoftwareLicenseResponse create(SoftwareLicenseRequest request);

    /**
     * Returns the requested page of software licenses.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicenseResponse> getAll(Pageable pageable);

    /**
     * Returns the software license identified by the given id.
     *
     * @param id the database identifier
     * @return the resulting software license
     */
    SoftwareLicenseResponse getById(Long id);

    /**
     * Executes the get expiring soon operation.
     *
     * @param days the number of days to look ahead
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicenseResponse> getExpiringSoon(int days, Pageable pageable);

    /**
     * Executes the get expired operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicenseResponse> getExpired(Pageable pageable);

    /**
     * Executes the get low seats operation.
     *
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicenseResponse> getLowSeats(Pageable pageable);

    /**
     * Searches software licenses using the supplied keyword.
     *
     * @param name the name or label value
     * @param pageable the pagination and sorting information
     * @return the requested page of results
     */
    Page<SoftwareLicenseResponse> search(String name, Pageable pageable);

    /**
     * Updates an existing software license.
     *
     * @param id the database identifier
     * @param request the request payload
     * @return the resulting software license
     */
    SoftwareLicenseResponse update(Long id, SoftwareLicenseRequest request);

    /**
     * Deactivates the software license.
     *
     * @param id the database identifier
     * @return the resulting software license
     */
    SoftwareLicenseResponse deactivate(Long id);

    /**
     * Deletes the software license.
     *
     * @param id the database identifier
     */
    void delete(Long id);
}
