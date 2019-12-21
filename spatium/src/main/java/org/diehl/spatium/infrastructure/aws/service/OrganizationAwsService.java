package org.diehl.spatium.infrastructure.aws.service;

import org.diehl.spatium.domain.model.Organization;
import org.diehl.spatium.domain.service.OrganizationService;
import org.diehl.spatium.infrastructure.aws.mapper.OrganizationDynamoDbMapper;
import org.diehl.spatium.infrastructure.aws.dynamodb.OrganizationDynamoDbRepository;
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
public class OrganizationAwsService implements OrganizationService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    OrganizationDynamoDbRepository organizationDynamoDbRepository;
    @Inject
    OrganizationDynamoDbMapper organizationDynamoDbMapper;

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Organization>>>
    getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Organization> organizationStream = response.items().stream()
                            .map(organizationDynamoDbMapper::toOrganization);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), organizationStream);
                });
    }

    @Override
    public CompletableFuture<Organization> findById(String id) {
        return dynamoDB.getItem(organizationDynamoDbRepository.getByKeySchemaRequest(id))
                .thenApply(response -> organizationDynamoDbMapper.toOrganization(response.item()));
    }

    @Override
    public CompletableFuture<List<Organization>> findAll() {
        return dynamoDB.scan(organizationDynamoDbRepository.scanRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<Organization> organizationStream = res.items().stream()
                            .map(organizationDynamoDbMapper::toOrganization);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Organization>> nextPage
                                = this.getPage(organizationDynamoDbRepository.scanRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        organizationStream = Stream.concat(organizationStream, nextPage.getValue());
                    }
                    return organizationStream.collect(Collectors.toList());
                });
    }

    @Override
    public CompletableFuture<Organization> add(Organization organization) {
        return dynamoDB.putItem(organizationDynamoDbRepository.putRequest(organizationDynamoDbMapper.toDynamoDbItem(organization)))
                .thenApply(response -> organizationDynamoDbMapper.toOrganization(response.attributes()));
    }
}
