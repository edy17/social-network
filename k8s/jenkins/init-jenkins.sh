#!/bin/bash
kubectl create -f service-reader.yaml
kubectl create clusterrolebinding service-reader-pod --clusterrole=service-reader --serviceaccount=default:kissing-giraffe-jenkins
kubectl create -f deployment-reader.yaml
kubectl create clusterrolebinding deployment-reader-pod --clusterrole=deployment-reader --serviceaccount=default:kissing-giraffe-jenkins
kubectl create -f event-reader.yaml
kubectl create clusterrolebinding event-reader-pod --clusterrole=event-reader --serviceaccount=default:kissing-giraffe-jenkins
kubectl create -f dr-reader.yaml
kubectl create clusterrolebinding dr-reader-pod --clusterrole=dr-reader --serviceaccount=default:kissing-giraffe-jenkins
