import { Options } from "vis-network/standalone";
import { NodeOptionsExtend } from "./vis-network-extend";

export const SCALE_FACTORY = 0.1;

export const COLLAPSED_NAME = '_collapsed';
export const NODE_WIDTH = 200;
export const NODE_HEIGHT = 100;
export const GROUP_MARGIN = 30;
export const MOVE_TO_SCALE = 1.5;

export const clusterNodeProperties: NodeOptionsExtend = {
    // borderWidth: 3,
    shape: 'box',
    mass: 1,
    widthConstraint: { minimum: 200, maximum: 200 },
    heightConstraint: { minimum: 100, maximum: 100 },
    color: {
        border: 'gray',
        background: '#9ccbb5',
        highlight: {
            border: '#2B7CE9',
            background: '#459e74'
        },
        hover: {
            border: '#2B7CE9',
            background: '#70b595'
        }
    },
    font: {
        multi: "html",
        size: 10,
        color: '#343434',
        bold: { // <b>
            // color: '#343434',
            size: 16, // px
            face: 'arial',
            vadjust: 0,
            mod: 'bold'
        },
        ital: { // <i>
            color: 'grey',
            size: 14, // px
            face: 'arial',
            vadjust: 0,
            mod: 'italic',
        },
        boldital: { // <b><i>
            // color: '#343434',
            size: 12, // px
            face: 'arial',
            vadjust: 0,
            mod: ''
        },
        mono: { // <code>
            // color: '#343434',
            size: 8, // px
            face: 'courier new',
            vadjust: 0,
            mod: ''
        }
    }
}

export const topologyOptions: Options = {
    edges: {
        color: {
            opacity: 0.5,
            color: '#a5b6d4',
            highlight: '#69a5fe',
            hover: '#8dd0ff'
        },
        width: 1,
        hoverWidth: 2,
        selectionWidth: 3,
        // hidden: true,
        dashes: true,
        smooth: {
            enabled: false,
            forceDirection: 'vertical',
            type: 'cubicBezier',
            roundness: 1
        },
        arrows: {
            to: {
                enabled: true
            }
        }
    },
    groups: {
        app: {
            shape: 'box',
            mass: 1,
            widthConstraint: { minimum: 200, maximum: 200 },
            heightConstraint: { minimum: 100, maximum: 100 },
            color: {
                border: 'gray',
                background: '#c5dcff',
                highlight: {
                    border: '#2B7CE9',
                    background: '#69a5fe'
                },
                hover: {
                    border: '#2B7CE9',
                    background: '#8dd0ff'
                }
            },
            font: {
                multi: "html",
                size: 10,
                color: '#343434',
                bold: { // <b>
                    // color: '#343434',
                    size: 16, // px
                    face: 'arial',
                    vadjust: 0,
                    mod: 'bold'
                },
                ital: { // <i>
                    color: 'grey',
                    size: 14, // px
                    face: 'arial',
                    vadjust: 0,
                    mod: 'italic',
                },
                boldital: { // <b><i>
                    // color: '#343434',
                    size: 12, // px
                    face: 'arial',
                    vadjust: 0,
                    mod: ''
                },
                mono: { // <code>
                    // color: '#343434',
                    size: 8, // px
                    face: 'courier new',
                    vadjust: 0,
                    mod: ''
                }
            }
        },
        lib: {
            shape: 'box',
            mass: 1,
            widthConstraint: { minimum: 200, maximum: 200 },
            heightConstraint: { minimum: 100, maximum: 100 },
            color: {
                border: 'gray',
                background: 'red',
                highlight: {
                    border: '#2B7CE9',
                    background: '#69a5fe'
                },
                hover: {
                    border: '#2B7CE9',
                    background: '#8dd0ff'
                }
            },
            font: {
                multi: "html",
                size: 10,
                color: '#343434',
                bold: { // <b>
                    // color: '#343434',
                    size: 16, // px
                    face: 'arial',
                    vadjust: 0,
                    mod: 'bold'
                },
                ital: { // <i>
                    color: 'grey',
                    size: 14, // px
                    face: 'arial',
                    vadjust: 0,
                    mod: 'italic',
                },
                boldital: { // <b><i>
                    // color: '#343434',
                    size: 12, // px
                    face: 'arial',
                    vadjust: 0,
                    mod: ''
                },
                mono: { // <code>
                    // color: '#343434',
                    size: 8, // px
                    face: 'courier new',
                    vadjust: 0,
                    mod: ''
                }
            }
        },
        group: clusterNodeProperties,
        group_control: {
            shape: 'square',
            size: 5
        }
    },
    nodes: {
    },
    interaction: {
        keyboard: false,
        multiselect: true,
        hideEdgesOnDrag: false,
        navigationButtons: false,
        hover: true,
        zoomView: true,
        dragNodes: true,
        dragView: true
    },
    physics: {
        barnesHut: {
            theta: 0.5,
            gravitationalConstant: -30000,
            centralGravity: 0,
            springLength: 100,
            springConstant: 0,
            damping: 1,
            avoidOverlap: 1
        },
        hierarchicalRepulsion: {
            centralGravity: 1,
            springLength: 1,
            springConstant: 0.611,
            nodeDistance: 200,
            damping: 0.9,
            avoidOverlap: 1
        },
        forceAtlas2Based: {
            theta: 0.5,
            gravitationalConstant: -5,
            centralGravity: 0.0001,
            springConstant: 0,
            springLength: 1000,
            damping: 1,
            avoidOverlap: 1
        },
        solver: 'forceAtlas2Based',
        // enabled: false,
        stabilization: {
            iterations: 1000
        }
    },
    manipulation: {
        enabled: false
    },
    layout: {
        improvedLayout: false
    }
};

