import { AfterViewInit, Component, ElementRef, Input, OnChanges, OnInit, SimpleChanges, ViewChild, ViewEncapsulation } from '@angular/core';
import { ApplicationGroupDto, OwnerDto, ProjectDto } from '../api/project-api.model';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MenuItem } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ContextMenuModule } from 'primeng/contextmenu';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ToolbarModule } from 'primeng/toolbar';
import { DataSet, IdType, Network } from 'vis-network/standalone';
import { ApplicationLiteDto, ApplicationType } from '../api/application-api.model';
import { COLLAPSED_NAME, GROUP_MARGIN, MOVE_TO_SCALE, NODE_HEIGHT, NODE_WIDTH, SCALE_FACTORY, topologyOptions } from './project-topology.const';

interface GroupCluster {
  id?: string,
  name?: string,
  clusterOptionsByData?: any,
  collapsedNodeId?: string,
  collapsed?: boolean,
  hidden?: boolean,
  description?: string,
  applicationNames?: string[],
  selected?: boolean,
  hovered?: boolean
}

interface GroupSize {
  x: number,
  y: number,
  width: number,
  height: number
}

export enum TopologyType {
  ALL,
  MICROSERVICES,
  DEPENDECIES
}

@Component({
  selector: 'app-project-topology',
  standalone: true,
  imports: [
    CommonModule,
    ToolbarModule,
    MultiSelectModule,
    DropdownModule,
    FormsModule,
    ButtonModule,
    ContextMenuModule
  ],
  templateUrl: './project-topology.component.html',
  styleUrls: ['./project-topology.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ProjectTopologyComponent implements OnInit, AfterViewInit, OnChanges {

  @Input('data') data!: ProjectDto;

  @Input() type: TopologyType = TopologyType.ALL;

  @ViewChild('visNetworkContainer', { static: false }) visNetworkContainer: ElementRef | any;

  // @ViewChild('op', { static: false }) op: ElementRef | any;
  @ViewChild('contextMenu', { static: false }) contextMenu: ElementRef | any;

  private container: HTMLElement | any;

  private network!: Network;

  private networkData!: any;

  apps: ApplicationLiteDto[] = [];

  ownerOptions: OwnerDto[] = [];
  selectedOwners: OwnerDto[] = [];

  // Types
  typeOptions: ApplicationType[] = [ApplicationType.LIBRARY, ApplicationType.MICROSERVICE];
  selectedTypes: ApplicationType[] = [];

  moveToAppOptions: ApplicationLiteDto[] = [];

  selectedApps: ApplicationLiteDto[] = [];

  selectedApp?: ApplicationLiteDto;

  selectedNodes: string[] = [];

  items: MenuItem[] = [];

  selectedItems: MenuItem[] = [{
    label: 'Show neighbors',
    icon: 'pi pi-fw pi-file',
    command: (event) => {
      console.log(event);
      const selectedNodes = this.network.getSelectedNodes();
      if (selectedNodes.length > 0) {
        this.showNeighbors(selectedNodes as string[]);
      }
    }
  }];

  constructor() { }

  ngOnInit() {

  }

  ngAfterViewInit(): void {
    this.container = this.visNetworkContainer?.nativeElement;
    this.network = new Network(this.container, this.networkData, topologyOptions);

    this.networkData?.groups.forEach((group: GroupCluster) => {
      this.network.cluster(group.clusterOptionsByData);
    });

    this.network.on('click', event => {
      const nodeId = this.network.getNodeAt(event.pointer.DOM);
      const nodeGroup = this.networkData.nodes.get(nodeId);
      if (nodeGroup && nodeGroup!.group === 'group_control') {
        let groupName: string = nodeId as string;
        const group = this.networkData.groups.get(this.convertGroupNodeIdToGroupId(groupName));
        if (group) {
          this.closeGroup(group);
          // this.networkData!.nodes.update([{ id: group.collapsedNodeId, hidden: true }]);
        }
      };
    });

    this.network.on('doubleClick', event => {
      const nodeId = this.network.getNodeAt(event.pointer.DOM);
      if (nodeId) {
        const group: GroupCluster = this.networkData.groups.get(nodeId as string);

        this.openGroup(group);
      }
    });

    this.network.on('stabilized', event => {
      // console.log('stabilized')
    });

    // this.network.on('animationFinished', event => {
    //   console.log('animationFinished')
    // });

    this.network.on('oncontext', event => {
      this.contextMenu.show();
      // this.groupClusters.forEach((value, key) => {
      //   value.collapsed = true;
      //   this.network.cluster(value.clusterOptionsByData);
      // });
    });

    this.network.on('selectNode', params => {
      const nodeIds: string[] = params.nodes;
      this.selectedNodes = nodeIds;

      const selectedGroupNodes: any[] = this.networkData.nodes.get({
        filter: (item: any) => {
          return params.nodes.includes(item.id) && item.group === 'group_control';
        }
      });

      selectedGroupNodes.forEach((groupNode: any) => {
        const groupId = this.convertGroupNodeIdToGroupId(groupNode.id);
        (this.networkData.groups.get(groupId) as GroupCluster).selected = true;
      });

      this.items = this.selectedItems;
    });

    this.network.on('deselectNode', params => {
      this.selectedNodes = params.nodes;
      const selectedGroupNodes: any[] = this.networkData.nodes.get({
        filter: (item: any) => {
          return params.nodes.includes(item.id) && item.group === 'group_control';
        }
      });

      selectedGroupNodes.forEach((groupNode: any) => {
        const groupId = this.convertGroupNodeIdToGroupId(groupNode.id);
        (this.networkData.groups.get(groupId) as GroupCluster).selected = false;
      });

      if (this.selectedNodes.length == 0) {
        this.items = [];
      }

    });

    this.network.on('hoverNode', (params: any) => {
      // this.op.el.nativeElement.setAttribute('style', 'left: 100px;');
      // this.op.show(params.event);
      const groupNode: any = this.networkData.nodes.get(params.node);
      if (groupNode && groupNode.group === 'group_control') {
        const groupId = this.convertGroupNodeIdToGroupId(groupNode.id);
        (this.networkData.groups.get(groupId) as GroupCluster).hovered = true;
        // this.networkData.nodes.update({id: groupNode.id, hover: true})
      }

    });

    this.network.on('blurNode', (params: any) => {
      // this.op.hide();
      const groupNode: any = this.networkData.nodes.get(params.node);
      if (groupNode && groupNode.group === 'group_control') {
        const groupId = this.convertGroupNodeIdToGroupId(groupNode.id);
        (this.networkData.groups.get(groupId) as GroupCluster).hovered = false;
        // this.networkData.nodes.update({id: groupNode.id, hover: false})
      }
    });

    this.network.on('dragStart', params => {
      if (params.nodes.length === 1) {
        const relevantGroups: GroupCluster[] = this.networkData.groups.get({
          filter: (item: GroupCluster) => {
            return item.collapsedNodeId === params.nodes[0];
          }
        });

        if (relevantGroups.length === 1) {
          const nodesInGroup: IdType[] = relevantGroups[0].applicationNames!.filter(app => this.network.findNode(app).length) as IdType[];
          this.network.selectNodes(nodesInGroup);
        }

        // // If groupClusterDrugged
        // const group: GroupCluster = this.networkData.groups.get(params.nodes[0]);
        // if (group) {
        //   this.network.selectNodes([params.nodes[0], group.collapsedNodeId as IdType])
        // }
      }
    });

    this.network.on('dragging', params => {
      params.nodes.forEach((nodeId: string) => {
        const relevantGroups: GroupCluster[] = this.networkData.groups.get({
          filter: (item: GroupCluster) => {
            return item.applicationNames!.includes(nodeId);
          }
        });
        if (relevantGroups.length === 1) {
          const group: GroupCluster = relevantGroups[0];
          const groupSize = this.calculateGroupSize(group);
          this.network.moveNode(group.collapsedNodeId!, groupSize!.x + groupSize!.width, groupSize!.y);
        }
      });
    });

    this.network.on('beforeDrawing', ctx => {
      this.networkData?.groups.forEach((group: GroupCluster) => {

        const groupSize = this.calculateGroupSize(group);
        if (groupSize) {

          ctx.beginPath();

          if (group.selected) {
            ctx.strokeStyle = "#69a5fe";
          } else if (group.hovered) {
            ctx.strokeStyle = "#8dd0ff";
          } else {
            ctx.strokeStyle = "#a5b6d4";
          }

          ctx.setLineDash([10, 15]);
          ctx.lineWidth = 1;
          const x = groupSize.x;
          const y = groupSize.y;
          const width = groupSize.width;
          const height = groupSize.height;
          ctx.strokeRect(x, y, width, height);

          ctx.font = '12px Arial';
          ctx.fillStyle = 'white';
          ctx.fillText(group.name, x + 10, y + height - 10);
          ctx.setLineDash([]);
        }
      });
    });
  }


  private closeGroup(group: any) {
    group.collapsed = true;
    group.hidden = false;
    this.network.cluster(group.clusterOptionsByData);
  }

  private openGroup(group: GroupCluster) {
    if (group && group.collapsed) {
      group.collapsed = false;
      group.hidden = true;
      (this.network as any).clustering.openCluster(group.name);
      // this.networkData!.nodes.update([{ id: group.collapsedNodeId, hidden: false }]);
      this.network.once('stabilized', event => {
        this.networkData.groups.forEach((group: GroupCluster) => {
          const groupSize = this.calculateGroupSize(group);
          if (groupSize) {
            this.network.moveNode(group.collapsedNodeId!, groupSize!.x + groupSize!.width, groupSize!.y);
          }
        });
      });
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    const nodeArray: any[] = this.data.applications
      .filter((app: ApplicationLiteDto) => {
        return app.type === ApplicationType.MICROSERVICE || ((this.type === TopologyType.ALL || this.type === TopologyType.DEPENDECIES)
         && app.type === ApplicationType.LIBRARY)
      })
      .map((app: ApplicationLiteDto) => {
        if (app.type === ApplicationType.MICROSERVICE) {
          return { id: app.name, label: this.getAppLabel(app), group: 'app' }; // TODO: groupId: 'Group Demo'
        } else {
          return { id: app.name, label: this.getAppLabel(app), group: 'lib' };
        }
      });
    this.apps = this.data.applications;
    if (this.data.owners) {
      this.ownerOptions = this.data.owners;
    }

    this.moveToAppOptions = this.data.applications;

    const edges: any[] = [];

    if (this.type === TopologyType.ALL || this.type === TopologyType.MICROSERVICES) {
      const connections = this.data.connections;
      Object.keys(connections).forEach(key => {
        const to = connections[key];
        if (to) {
          to.forEach((t: any) => {
            edges.push({
              from: key,
              to: t
            });
          });
        }
      });
    }

    // Dependencies
    if (this.type === TopologyType.ALL || this.type === TopologyType.DEPENDECIES) {
      const dependencies = this.data.dependencies;
      Object.keys(dependencies).forEach(key => {
        const to = dependencies[key];
        if (to) {
          to.forEach((t: any) => {
            edges.push({
              from: key,
              to: t
            });
          });
        }
      });
    }

    this.networkData = {
      nodes: new DataSet(nodeArray),
      edges: new DataSet(edges),
      groups: new DataSet()
    }

    // Groups - Cluster
    this.data.groups?.forEach(group => {
      const groupCollapsedNodeId = group.name + COLLAPSED_NAME;
      const clusterOptionsByData = {
        joinCondition: (childOptions: any) => {
          return group.applicationNames.includes(childOptions.id) || childOptions.id === groupCollapsedNodeId;
        },
        clusterNodeProperties: {
          id: group.name,
          label: this.getGroupLabel(group),
          group: 'group',
          allowSingleNodeCluster: true
        }
      }

      this.networkData.nodes.add({ id: groupCollapsedNodeId, label: '-', group: 'group_control' });

      // subscribe to any change in the DataSet
      // this.networkData.nodes.on('*', (event: any, properties: any, senderId: any) => {
      //   console.log('event:', event, 'properties:', properties, 'senderId:', senderId);
      // });

      this.networkData.groups.add({
        id: group.name,
        name: group.name,
        clusterOptionsByData: clusterOptionsByData,
        collapsed: true,
        hidden: false,
        collapsedNodeId: groupCollapsedNodeId,
        applicationNames: group.applicationNames,
        description: group.description,
        selected: false,
        hovered: false
      } as GroupCluster);

    });
  }

  getAppLabel(app: ApplicationLiteDto): string {
    return `<b>${app.name}</b>
    <code>[Container: ${Object.keys(app.tags)}]</code>\n
    <b><i>${app.description}</i></b>
    `
  }

  getGroupLabel(group: ApplicationGroupDto): string {
    return `<b>${group.name}</b>
    <code>[Software System]</code>\n
    <b><i>${group.description} description</i></b>
    `
  }

  fit(): void {
    this.network.fit();
  }

  zoomIn(): void {
    this.network.moveTo({
      scale: this.network.getScale() + SCALE_FACTORY
    });
  }

  zoomOut(): void {
    this.network.moveTo({
      scale: this.network.getScale() - SCALE_FACTORY
    });
  }

  /**
   * Dispaly only specific applications
   */
  onFilterApps(value: ApplicationLiteDto[]): void {
    const ids: any[] = this.networkData.nodes.map((node: any) => {
      return { id: node.id, hidden: false };
    });
    this.networkData!.nodes.update(ids);
    if (value.length > 0) {
      this.moveToAppOptions = value;
      const nodeIds: string[] = value.map(app => app.name);
      const neighborIds: string[] = [];
      neighborIds.push(...nodeIds);

      // Find 
      const notIncludedNodeIds: any[] = this.networkData.nodes.get({
        filter: (item: any) => {
          return !neighborIds.includes(item.id) && item.group !== 'group_control';
        }
      });

      const updateHiddenNodes = notIncludedNodeIds.map(node => {
        return { id: node.id, hidden: true };
      });
      this.networkData!.nodes.update(updateHiddenNodes);

    } else {
      this.moveToAppOptions = this.apps;
    }

    // Hide groups
    this.networkData?.groups.forEach((group: GroupCluster) => {
      const groupHidden: boolean | undefined = group.applicationNames?.some(appName => this.networkData?.nodes.get(appName).hidden);
      if (!group.hidden) {
        if (groupHidden) {
          group.collapsed = false;
          (this.network as any).clustering.openCluster(group.name);

          this.networkData!.nodes.update([{ id: group.collapsedNodeId, hidden: true }]);
        } else {
          group.collapsed = true;
          this.network.cluster(group.clusterOptionsByData);
        }
      } else {
        this.networkData!.nodes.update([{ id: group.collapsedNodeId, hidden: groupHidden }]);
      }
    });

    this.fit();
  }

  onFilterOwners(owners: OwnerDto[]): void {
    const ownerApplications = owners.flatMap((owner) => owner.applicationNames);
    const applictionsToShow = this.apps.filter((app) => ownerApplications.includes(app.name));
    this.selectedApps = applictionsToShow;
    this.onFilterApps(applictionsToShow);
  }

  onFilterTypes(types: ApplicationType[]): void {
    const applictionsToShow = this.apps.filter((app) => types.includes(app.type));
    this.selectedApps = applictionsToShow;
    this.onFilterApps(applictionsToShow);
  }

  /**
   * Move to specific node change
   */
  onMoveToAppChanged(value: ApplicationLiteDto) {
    if (value) {
      this.network.moveTo({
        position: this.network.getPosition(value.name),
        animation: true,
        scale: MOVE_TO_SCALE
      });
    } else {
      this.fit();
    }
  }

  convertGroupNodeIdToGroupId(groupNodeId: string): string {
    return groupNodeId.substring(0, groupNodeId.length - COLLAPSED_NAME.length);
  }

  /**
   * Show Naighbors of nodes
   */
  showNeighbors(nodeIds: string[]): void {

    const neighborIds: string[] = [];
    const edges = this.networkData.edges;

    // If Group add inner nodes
    const groupNodes: GroupCluster[] = this.networkData.groups.get(nodeIds);
    groupNodes.forEach(group => {
      nodeIds.push(...group.applicationNames!);
    });

    edges.forEach((edge: any) => {
      if (nodeIds.includes(edge.from)) {
        neighborIds.push(edge.to);
      } else if (nodeIds.includes(edge.to)) {
        neighborIds.push(edge.from);
      }
    });
    neighborIds.push(...nodeIds);

    const showedNodes: ApplicationLiteDto[] = this.apps.filter(app => neighborIds.includes(app.name));
    this.selectedApps = showedNodes;
    this.onFilterApps(showedNodes)
  }

  /**
   * Calculate group size
   */
  calculateGroupSize(group: GroupCluster): GroupSize | undefined {
    if (!group.collapsed) {

      const notHiddenInGroup = group.applicationNames!.filter(appName => !this.networkData?.nodes.get(appName)?.hidden);

      if (notHiddenInGroup.length > 0) {
        const nodePositions = this.network.getPositions(notHiddenInGroup);

        const xArray: number[] = [];
        const yArray: number[] = [];
        Object.keys(nodePositions).forEach(appName => {
          const nodePosition = nodePositions[appName];
          xArray.push(nodePosition.x);
          yArray.push(nodePosition.y);
        });

        const minX = Math.min(...xArray);
        const minY = Math.min(...yArray);
        const maxX = Math.max(...xArray);
        const maxY = Math.max(...yArray);

        return {
          x: minX - NODE_WIDTH / 2 - GROUP_MARGIN,
          y: minY - NODE_HEIGHT / 2 - GROUP_MARGIN,
          width: maxX - minX + NODE_WIDTH + 2 * GROUP_MARGIN,
          height: maxY - minY + NODE_HEIGHT + 2 * GROUP_MARGIN
        }
      }
    }
    return undefined;
  }

  collapseAll(): void {
    this.networkData?.groups.forEach((group: GroupCluster) => {
      this.openGroup(group);
    });
  }

  uncollapseAll(): void {
    this.networkData?.groups.forEach((group: GroupCluster) => {
      this.closeGroup(group);
    });
  }

  /**
   * Is context hidden
   */
  isContextHidden(): boolean {
    return this.items.length == 0;
  }

  clearAppFilter(): void {
    this.selectedApps = [];
    this.onFilterApps(this.selectedApps);
  }

}
