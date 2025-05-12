import { Component, OnInit } from '@angular/core';
import { ApiService } from './api.service';

@Component({
    selector: 'app-root',
    standalone: true,
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    title = 'frontEnd';
    data: any;
    constructor(private apiService: ApiService) { };
    ngOnInit() {
        this.apiService.getData().subscribe(data => {this.data = data;});
    }
}