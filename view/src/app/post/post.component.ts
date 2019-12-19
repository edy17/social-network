import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {SpatiumService} from "../spatium.service";
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {DetailedPost} from "../model/detailedPost.model";
import {MappingService} from "./mapping.service";

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css']
})
export class PostComponent implements OnInit {

  @ViewChild('rendererCanvas', {static: true})
  public rendererCanvas: ElementRef<HTMLCanvasElement>;

  @ViewChild('parentCanvas', {static: true})
  public parentCanvas;

  detailedPosts: Array<DetailedPost>;
  enableEdition: boolean = false;
  currentPost: DetailedPost;
  progression: number = undefined;
  currentFileToUpload: File = undefined;
  uploadFileName: string = "Choose new image";
  timeStamp: number = (new Date()).getTime();
  linkPicture: string = this.spatiumService.host + '/posts/image/';

  constructor(public spatiumService: SpatiumService, private mappingService: MappingService,
              public route: ActivatedRoute, private router: Router) {
  }

  ngOnInit() {
    this.mappingService.init(this.rendererCanvas, this.parentCanvas);
    //render(); // remove when using next line for animation loop (requestAnimationFrame)
    this.mappingService.animate();

    this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        if (this.route.snapshot.params.p1 == 0) {
          this.getPosts(this.spatiumService.host + "/posts");
        } else {
          this.getPosts(this.spatiumService.host + "/posts/organization/" + this.route.snapshot.params.p1);
        }
      }
    });
    if (this.route.snapshot.params.p1 == 0) {
      this.getPosts(this.spatiumService.host + "/posts");
    }
  }

  private getPosts(url) {
    this.spatiumService.getDetailedPosts(url)
      .subscribe(posts => {
        this.detailedPosts = posts;
      }, err => {
        console.log(err);
      });
  }

  clickOnComments(p: DetailedPost) {
    p.isExpanded = !p.isExpanded;
  }

  onResize(event) {
    this.mappingService.camera.aspect = event.target.innerWidth / event.target.innerHeight;
    this.mappingService.camera.updateProjectionMatrix();
    this.mappingService.renderer.setSize(event.target.innerWidth, event.target.innerHeight);
  }
}
