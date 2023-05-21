import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { SelectButtonModule } from 'primeng/selectbutton';
import { DialogModule } from 'primeng/dialog';
import { DynamicDialogModule } from 'primeng/dynamicdialog';
import { SidebarModule } from 'primeng/sidebar';
import { TooltipModule } from 'primeng/tooltip';
import { MultiSelectModule } from 'primeng/multiselect';
import { DropdownModule } from 'primeng/dropdown';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { ContextMenuModule } from 'primeng/contextmenu';
import { InputTextModule } from 'primeng/inputtext';


@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    ToolbarModule,
    ButtonModule,
    TableModule,
    ToggleButtonModule,
    SelectButtonModule,
    DialogModule,
    DynamicDialogModule,
    SidebarModule,
    TooltipModule,
    MultiSelectModule,
    DropdownModule,
    OverlayPanelModule,
    ContextMenuModule,
    InputTextModule
  ],
  exports: [
    ToolbarModule,
    ButtonModule,
    TableModule,
    ToggleButtonModule,
    SelectButtonModule,
    DialogModule,
    DynamicDialogModule,
    SidebarModule,
    TooltipModule,
    MultiSelectModule,
    DropdownModule,
    OverlayPanelModule,
    ContextMenuModule,
    InputTextModule
  ]
})
export class PrimengModule { }
