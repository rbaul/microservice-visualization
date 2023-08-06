import { NodeOptions } from "vis-network";

export interface NodeOptionsExtend extends NodeOptions {
    heightConstraint?: number | boolean | { minimum?: number, maximum?: number };
}