import {Component, HostListener, OnInit} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Spatium';
  isExpanded: boolean = false;
  style;
  imageWidth;
  imageHeight;

  constructor() {
  }

  ngOnInit(): void {
  }

  clickOnComments() {
    this.isExpanded = !this.isExpanded;
  }
}
