import {Component, OnInit, Renderer} from '@angular/core';
import {SpatiumService} from "./spatium.service";
import {Router} from "@angular/router";
import {Organization} from "./model/organization.model";
import {PostComponent} from "./post/post.component";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Spatium';
  postComponent: PostComponent;
  style;

  constructor(public spatiumService: SpatiumService, private _ren: Renderer, public router: Router) {
  }

  ngOnInit(): void {

  }

  onActivate(componentRef) {
    this.postComponent = componentRef;
    this.loadOrganizations(this.postComponent.route.snapshot.params.p1);
    this.postComponent.ngOnInit();
  }

  public loadOrganizations(currentOrganization) {
    this.spatiumService.getOrganizations(this.spatiumService.host + '/organizations')
      .subscribe(data => {
        this.spatiumService.organizations = data;
        if (currentOrganization == 0) {
          this.spatiumService.currentOrganization = null;
        } else {
          this.spatiumService.organizations.forEach((organization: Organization) => {
            if (organization.name == currentOrganization) {
              this.spatiumService.currentOrganization = organization;
            }
          });
        }
      }, err => {
        console.log(err);
      });
  }

  clickOnOrganization($event: MouseEvent, isPublic: boolean, selectedOrganization: Organization) {
    this.spatiumService.currentOrganization = selectedOrganization;
    if ((isPublic == true) && (selectedOrganization == null)) {
      this.router.navigateByUrl("/posts/0");
    } else {
      this.router.navigateByUrl('/posts/' + selectedOrganization.name);
    }
    return false;
  }
}
