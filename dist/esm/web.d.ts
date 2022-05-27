import { WebPlugin } from '@capacitor/core';
import { CapacitorMusicControlsInfo, CapacitorMusicControlsPlugin } from "./definitions";
export declare class CapacitorMusicControlsWeb extends WebPlugin implements CapacitorMusicControlsPlugin {
    constructor();
    create(options: CapacitorMusicControlsInfo): Promise<any>;
    destroy(): Promise<any>;
    updateDismissable(dismissable: boolean): void;
    updateState(args: {
        elapsed: number;
        isPlaying: boolean;
    }): void;
    updateIsPlaying(opts: {
        isPlaying: boolean;
    }): Promise<void>;
}
