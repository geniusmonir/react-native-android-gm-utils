import { DeviceEventEmitter } from 'react-native';
import { BGTimerListener } from '../native/AndroidGmUtils';

type CallbackEntry = {
  callback: (() => void) | (() => Promise<void>);
  interval: boolean;
  timeout: number;
};

class BackgroundTimer {
  private callbacks: Record<number, CallbackEntry> = {};

  constructor() {
    // Listen for native timer events
    DeviceEventEmitter.addListener(
      'backgroundTimer.timeout',
      async (id: number) => {
        const entry = this.callbacks[id];
        if (!entry) return;

        const { callback, interval, timeout } = entry;

        if (!interval) {
          // One-shot timers auto-remove
          delete this.callbacks[id];
        } else {
          // Reschedule interval
          BGTimerListener.setTimeout(timeout, id);
        }

        await callback(); // Run JS callback

        // If async interval, mark finished
        if (interval && callback.constructor.name === 'AsyncFunction') {
          BGTimerListener.markIntervalFinished(id);
        }
      }
    );
  }

  /** Start a one-shot timer (returns native ID) */
  async start(delay: number = 0, callback?: () => void): Promise<number> {
    const id = await BGTimerListener.start(delay, 0);
    if (callback) {
      this.callbacks[id] = { callback, interval: false, timeout: delay };
    }
    return id;
  }

  /** Stop all timers */
  stop() {
    return BGTimerListener.stop();
  }

  /** Internal: sync interval */
  async runSyncInterval(
    callback: () => void,
    timeout: number
  ): Promise<number> {
    const id = await BGTimerListener.setInterval(timeout, 0);
    this.callbacks[id] = { callback, interval: true, timeout };
    return id;
  }

  /** Internal: async interval */
  async runAsyncInterval(
    callback: () => Promise<void>,
    timeout: number
  ): Promise<number> {
    const id = await BGTimerListener.setIntervalAsync(timeout, 0);
    this.callbacks[id] = {
      callback: async () => {
        await callback();
        BGTimerListener.markIntervalFinished(id);
      },
      interval: true,
      timeout,
    };
    return id;
  }

  /** One-shot timeout */
  async setTimeout(callback: () => void, timeout: number): Promise<number> {
    const id = await BGTimerListener.setTimeout(timeout, 0);
    this.callbacks[id] = { callback, interval: false, timeout };
    return id;
  }

  /** Clear any timer */
  clearTimer(id: number) {
    delete this.callbacks[id];
    BGTimerListener.clearInterval(id);
  }

  /** Convenience aliases */
  clearInterval(id: number) {
    this.clearTimer(id);
  }

  clearTimeout(id: number) {
    this.clearTimer(id);
  }
}

export const backgroundTimer = new BackgroundTimer();
