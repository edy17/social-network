import * as THREE from 'three';
import {MapControls} from 'three/examples/jsm/controls/OrbitControls.js';
import {GUI} from 'three/examples/jsm/libs/dat.gui.module.js';
import {Injectable, ElementRef, OnDestroy, NgZone} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MappingService implements OnDestroy {

  private canvas: HTMLCanvasElement;
  private renderer: THREE.WebGLRenderer;
  private camera: THREE.PerspectiveCamera;
  private scene: THREE.Scene;
  private light: THREE.AmbientLight;
  private controls;
  canvasWidth: number = 680;
  canvasHeight: number = 400;

  private frameId: number = null;

  public constructor(private ngZone: NgZone) {
  }

  public ngOnDestroy() {
    if (this.frameId != null) {
      cancelAnimationFrame(this.frameId);
    }
  }

  public init(canvas: ElementRef<HTMLCanvasElement>): void {
    this.canvas = canvas.nativeElement;
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0xcccccc);
    this.scene.fog = new THREE.FogExp2(0xcccccc, 0.002);

    this.renderer = new THREE.WebGLRenderer({
      canvas: this.canvas,
      alpha: true,    // transparent background
      antialias: true // smooth edges
    });
    this.renderer.setPixelRatio(window.devicePixelRatio);
    this.renderer.setSize(this.canvasWidth, this.canvasHeight);

    this.camera = new THREE.PerspectiveCamera(60, this.canvasWidth / this.canvasHeight, 1, 1000);
    this.camera.position.set(400, 200, 0);
    // controls
    this.controls = new MapControls(this.camera, this.renderer.domElement);
    //controls.addEventListener( 'change', render ); // call this only in static scenes (i.e., if there is no animation loop)
    this.controls.enableDamping = true; // an animation loop is required when either damping or auto-rotation are enabled
    this.controls.dampingFactor = 0.05;
    this.controls.screenSpacePanning = false;
    this.controls.minDistance = 100;
    this.controls.maxDistance = 500;
    this.controls.maxPolarAngle = Math.PI / 2;
    // world
    let geometry = new THREE.BoxBufferGeometry(1, 1, 1);
    geometry.translate(0, 0.5, 0);
    let material = new THREE.MeshPhongMaterial({color: 0xffffff, flatShading: true});
    for (let i = 0; i < 500; i++) {
      let mesh = new THREE.Mesh(geometry, material);
      mesh.position.x = Math.random() * 1600 - 800;
      mesh.position.y = 0;
      mesh.position.z = Math.random() * 1600 - 800;
      mesh.scale.x = 20;
      mesh.scale.y = Math.random() * 80 + 10;
      mesh.scale.z = 20;
      mesh.updateMatrix();
      mesh.matrixAutoUpdate = false;
      this.scene.add(mesh);
    }
    // lights
    let light = new THREE.DirectionalLight(0xffffff);
    light.position.set(1, 1, 1);
    this.scene.add(light);
    light = new THREE.DirectionalLight(0x002288);
    light.position.set(-1, -1, -1);
    this.scene.add(light);
    let light2 = new THREE.AmbientLight(0x222222);
    this.scene.add(light2);
    //
    window.addEventListener('resize', () => this.onWindowResize(), false);
    let gui = new GUI();
    gui.add(this.controls, 'screenSpacePanning');
  }

  onWindowResize() {
    this.camera.aspect = this.canvasWidth / this.canvasHeight;
    this.camera.updateProjectionMatrix();
    this.renderer.setSize(this.canvasWidth, this.canvasHeight);
  }

  public animate() {
    requestAnimationFrame(() => this.animate());
    this.controls.update(); // only required if controls.enableDamping = true, or if controls.autoRotate = true
    this.render();
  }

  public render() {
    this.renderer.render(this.scene, this.camera);
  }
}
