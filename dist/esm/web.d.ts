import { WebPlugin } from '@capacitor/core';
import type { PluginListenerHandle } from '@capacitor/core';
import type { CapacitorMusicControlsInfo, CapacitorMusicControlsPlugin, PermissionStatus } from './definitions';
export declare class CapacitorMusicControlsWeb extends WebPlugin implements CapacitorMusicControlsPlugin {
    create(options: CapacitorMusicControlsInfo): Promise<any>;
    destroy(): Promise<any>;
    checkPermissions(): Promise<PermissionStatus>;
    requestPermissions(): Promise<PermissionStatus>;
    updateDismissable(dismissable: boolean): void;
    updateState(args: {
        elapsed: number;
        isPlaying: boolean;
    }): void;
    updateIsPlaying(opts: {
        isPlaying: boolean;
    }): Promise<void>;
    addListener(event: string, callback: (info: any) => void): Promise<PluginListenerHandle>;
}
