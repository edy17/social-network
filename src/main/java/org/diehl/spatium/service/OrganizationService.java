package org.diehl.spatium.service;

import org.diehl.spatium.model.Organization;
import org.diehl.spatium.repository.OrganizationRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class OrganizationService {

    @Inject
    DynamoDbAsyncClient dynamoDB;
    @Inject
    OrganizationRepository organizationRepository;

    public CompletableFuture<Organization> getByKeySchema(String keySchema) {
        return dynamoDB.getItem(organizationRepository.getByKeySchemaRequest(keySchema)).thenApply(response -> organizationRepository.getObject(response.item()));
    }

    public CompletableFuture<List<Organization>> findAll() {
        return dynamoDB.scan(organizationRepository.scanRequest(null))
                .thenApply(res -> {
                    Map<String, AttributeValue> lastKeyEvaluated = res.lastEvaluatedKey();
                    Stream<Organization> organizationStream = res.items().stream().map(organizationRepository::getObject);
                    while (!lastKeyEvaluated.isEmpty()) {
                        AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Organization>> nextPage
                                = this.getPage(organizationRepository.scanRequest(lastKeyEvaluated)).join();
                        lastKeyEvaluated = nextPage.getKey();
                        organizationStream = Stream.concat(organizationStream, nextPage.getValue());
                    }
                    return organizationStream.collect(Collectors.toList());
                });
    }

    private CompletableFuture<AbstractMap.SimpleImmutableEntry<Map<String, AttributeValue>, Stream<Organization>>> getPage(ScanRequest scanRequest) {
        return dynamoDB.scan(scanRequest)
                .thenApply(response -> {
                    Stream<Organization> organizationStream = response.items().stream().map(organizationRepository::getObject);
                    return new AbstractMap.SimpleImmutableEntry<>(response.lastEvaluatedKey(), organizationStream);
                });
    }

    public CompletableFuture<Organization> add(Organization organization) {
        return dynamoDB.putItem(organizationRepository.putRequest(organization)).thenApply(response -> organizationRepository.getObject(response.attributes()));
    }

    public CompletableFuture<Organization> addPost(String organizationName, String postId) {
        Organization organization = getByKeySchema(organizationName).join();
        ArrayList<String> postIds = new ArrayList<>(organization.getPostIds());
        postIds.add(postId);
        organization.setPostIds(postIds);
        return dynamoDB.updateItem(organizationRepository.addPostRequest(organization)).thenApply(response -> organizationRepository.getObject(response.attributes()));
    }
}
