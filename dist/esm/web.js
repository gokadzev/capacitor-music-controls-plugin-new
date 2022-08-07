import { WebPlugin } from '@capacitor/core';
export class CapacitorMusicControlsWeb extends WebPlugin {
    constructor() {
        super({
            name: 'CapacitorMusicControls',
            platforms: ['web'],
        });
    }
    create(options) {
        console.log('create', options);
        return Promise.resolve(undefined);
    }
    destroy() {
        return Promise.resolve(undefined);
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
}
//# sourceMappingURL=web.js.map