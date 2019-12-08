package org.diehl.spatium.infrastructure.dynamodb.service;

import org.diehl.spatium.domain.model.Organization;
import org.diehl.spatium.domain.service.OrganizationService;
import org.diehl.spatium.infrastructure.dynamodb.mapper.OrganizationDynamoMapper;
import org.diehl.spatium.infrastructure.dynamodb.repository.OrganizationRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrganizationDynamoService implements OrganizationService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    OrganizationRepository organizationRepository;
    @Inject
    OrganizationDynamoMapper organizationDynamoMapper;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Organization>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Organization> organizationStream = response.items().stream()
                            .map(organizationDynamoMapper::toOrganization);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), organizationStream);
                });
    }

    @Override
    public CompletableFuture<Organization> findById(String id) {
        return dynamoDB.getItem(organizationRepository.getByKeySchemaRequest(id))
                .thenApply(response -> organizationDynamoMapper.toOrganization(response.item()));
    }

    @Override
    public CompletableFuture<List<Organization>> findAll() {
        return dynamoDB.scan(organizationRepository.scanRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<Organization> organizationStream = res.items().stream()
                            .map(organizationDynamoMapper::toOrganization);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Organization>> nextPage
                                = this.getPage(organizationRepository.scanRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        organizationStream = Stream.concat(organizationStream, nextPage.getValue());
                    }
                    return organizationStream.collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<Organization> add(Organization organization) {
        return dynamoDB.putItem(organizationRepository.putRequest(organizationDynamoMapper.toDynamoDbItem(organization)))
                .thenApply(response -> organizationDynamoMapper.toOrganization(response.attributes()));
    }
}
