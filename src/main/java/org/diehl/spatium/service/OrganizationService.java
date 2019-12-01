package org.diehl.spatium.service;

import org.diehl.spatium.model.Organization;
import org.diehl.spatium.repository.OrganizationRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    OrganizationRepository organizationRepository;

    public CompletableFuture<List<Organization>> findAll() {
        return dynamoDB.scan(organizationRepository.scanRequest())
                .thenApply(res -> res.items().stream().map(organizationRepository::getObject).collect(Collectors.toList()));
    }

    public CompletableFuture<List<Organization>> add(Organization organization) {
        organization.setId(UUID.randomUUID().toString());
        return dynamoDB.putItem(organizationRepository.putRequest(organization)).thenCompose(ret -> findAll());
    }

    public CompletableFuture<List<Organization>> update(Organization organization) {
        return dynamoDB.updateItem(organizationRepository.update(organization)).thenCompose(ret -> findAll());
    }

    public CompletableFuture<Organization> getById(String id) {
        return dynamoDB.getItem(organizationRepository.getByIdRequest(id)).thenApply(response -> organizationRepository.getObject(response.item()));
    }
}
