import { WebPlugin } from '@capacitor/core';
import type { PluginListenerHandle } from '@capacitor/core';

import type {
  CapacitorMusicControlsInfo,
  CapacitorMusicControlsPlugin,
  PermissionStatus,
} from './definitions';

export class CapacitorMusicControlsWeb
  extends WebPlugin
  implements CapacitorMusicControlsPlugin
{
  create(options: CapacitorMusicControlsInfo): Promise<any> {
    console.log('create', options);
    return Promise.resolve(undefined);
  }

  destroy(): Promise<any> {
    return Promise.resolve(undefined);
  }

  checkPermissions(): Promise<PermissionStatus> {
    return Promise.resolve({ notifications: 'granted' });
  }

  requestPermissions(): Promise<PermissionStatus> {
    return Promise.resolve({ notifications: 'granted' });
  }

  updateDismissable(dismissable: boolean): void {
    console.log('updateDismissable', dismissable);
  }

  updateState(args: { elapsed: number; isPlaying: boolean }): void {
    console.log('updateState', args);
  }

  updateIsPlaying(opts: { isPlaying: boolean }): Promise<void> {
    console.log('updateIsPlaying', opts);
    return Promise.resolve();
  }

  addListener(
    event: string,
    callback: (info: any) => void,
  ): Promise<PluginListenerHandle> {
    console.log('addListener', event, callback);
    return Promise.resolve({
      remove: async () => {
        console.log('Listener removed');
      },
    });
  }
}
