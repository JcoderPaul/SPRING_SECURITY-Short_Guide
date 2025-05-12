import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { CommonModule } from '@angular/common'; 
import { ApiService } from './api.service';


@NgModule({
    declarations: [AppComponent],
    imports: [BrowserModule, CommonModule, HttpClientModule],
    providers: [ApiService],
    bootstrap: [AppComponent]
})
export class AppModule { }