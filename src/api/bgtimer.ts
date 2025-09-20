import { DeviceEventEmitter, NativeEventEmitter } from 'react-native';
import { BGTimerListener } from '../native/AndroidGmUtils';

const Emitter = new NativeEventEmitter(BGTimerListener);

type CallbackEntry = {
  callback: (() => void) | (() => Promise<void>);
  interval: boolean;
  timeout: number;
};

class BackgroundTimer {
  private uniqueId = 0;
  private callbacks: Record<number, CallbackEntry> = {};
  private backgroundListener: any;
  private backgroundTimer: number | null = null;

  constructor() {
    Emitter.addListener('backgroundTimer.timeout', (id: number) => {
      const entry = this.callbacks[id];
      if (!entry) return;

      const { callback, interval, timeout } = entry;
      if (!interval) delete this.callbacks[id];
      else BGTimerListener.setTimeout(id, timeout);
      callback();
    });
  }

  start(delay = 0) {
    return BGTimerListener.start(delay);
  }

  stop() {
    return BGTimerListener.stop();
  }

  runBackgroundTimer(callback: () => void, delay: number) {
    this.start(0);
    const EventEmitter = DeviceEventEmitter;
    this.backgroundListener = EventEmitter.addListener(
      'backgroundTimer',
      () => {
        this.backgroundListener.remove();
        this.backgroundClockMethod(callback, delay);
      }
    );
  }

  async runBackgroundTimerAsync(
    callback: () => Promise<void>,
    interval: number
  ) {
    // Start native async interval
    const id = await BGTimerListener.setIntervalAsync(interval);

    // Save callback as CallbackEntry
    this.callbacks[id] = {
      callback: async () => {
        await callback(); // run async task
        BGTimerListener.markIntervalFinished(id); // notify native
      },
      interval: true,
      timeout: interval,
    };

    return id; // return id so you can clear later
  }

  private backgroundClockMethod(callback: () => void, delay: number) {
    this.backgroundTimer = this.setTimeout(() => {
      callback();
      this.backgroundClockMethod(callback, delay);
    }, delay);
  }

  stopBackgroundTimer() {
    this.stop();
    if (this.backgroundTimer) this.clearTimeout(this.backgroundTimer);
    if (this.backgroundListener) {
      this.backgroundListener.remove();
      this.backgroundListener = null;
    }
  }

  setTimeout(callback: () => void, timeout: number) {
    this.uniqueId += 1;
    const id = this.uniqueId;
    this.callbacks[id] = { callback, interval: false, timeout };
    BGTimerListener.setTimeout(id, timeout);
    return id;
  }

  clearTimeout(id: number) {
    delete this.callbacks[id];
  }

  setInterval(callback: () => void, timeout: number) {
    this.uniqueId += 1;
    const id = this.uniqueId;
    this.callbacks[id] = { callback, interval: true, timeout };
    BGTimerListener.setInterval(id, timeout);
    return id;
  }

  clearInterval(id: number) {
    delete this.callbacks[id];
  }
}

export const backgroundTimer = new BackgroundTimer();
