import {Inject, Injectable} from '@angular/core';
import {Organization} from "./model/organization.model";
import {HttpClient} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {catchError, map} from "rxjs/operators";
import {DetailedPost} from "./model/detailedPost.model";

@Injectable({
  providedIn: 'root'
})
export class SpatiumService {

  currentOrganization: Organization;
  organizations: Array<Organization>;

  constructor(private httpClient: HttpClient, @Inject('BACKEND_API_URL') public host: string) {
  }

  public getOrganizations(url): Observable<Array<Organization>> {
    return this.httpClient.get<Array<Organization>>(url).pipe(
      map(data => data.map(organization => new Organization().deserialize(organization))),
      catchError(() => throwError('Organizations not found'))
    );
  }

  getDetailedPosts(url): Observable<Array<DetailedPost>> {
    return this.httpClient.get<Array<DetailedPost>>(url).pipe(
      map(data => data.map(detailedPost => new DetailedPost().deserialize(detailedPost))),
      catchError(() => throwError('DetailedPosts not found'))
    );
  }
}
