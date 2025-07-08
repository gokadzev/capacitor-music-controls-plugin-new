import { WebPlugin } from '@capacitor/core';
export class CapacitorMusicControlsWeb extends WebPlugin {
    create(options) {
        console.log('create', options);
        return Promise.resolve(undefined);
    }
    destroy() {
        return Promise.resolve(undefined);
    }
    checkPermissions() {
        return Promise.resolve({ notifications: 'granted' });
    }
    requestPermissions() {
        return Promise.resolve({ notifications: 'granted' });
    }
    updateDismissable(dismissable) {
        console.log('updateDismissable', dismissable);
    }
    updateState(args) {
        console.log('updateState', args);
    }
    updateIsPlaying(opts) {
        console.log('updateIsPlaying', opts);
        return Promise.resolve();
    }
    addListener(event, callback) {
        console.log('addListener', event, callback);
        return Promise.resolve({
            remove: async () => {
                console.log('Listener removed');
            },
        });
    }
}
//# sourceMappingURL=web.js.map