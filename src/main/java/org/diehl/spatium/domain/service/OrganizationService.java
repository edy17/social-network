package org.diehl.spatium.domain.service;

import org.diehl.spatium.domain.model.Organization;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OrganizationService {

    CompletableFuture<Organization> findById(String id);

    CompletableFuture<List<Organization>> findAll();

    CompletableFuture<Organization> add(Organization organization);
}
