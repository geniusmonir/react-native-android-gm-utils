import { AndroidGmUtils } from '../native/AndroidGmUtils';

// Method to check if battery optimization is enabled
export function isBatteryOptimizationEnabled(): Promise<boolean> {
  return AndroidGmUtils.isBatteryOptimizationEnabled();
}

// Method to request disabling battery optimization
export function requestDisableBatteryOptimization(): void {
  return AndroidGmUtils.requestDisableBatteryOptimization();
}

// Method to open background auto-start settings based on manufacturer
export function openBackgroundAutoStartSettings(): void {
  return AndroidGmUtils.openBackgroundAutoStartSettings();
}

export function gracefulExitRestart(): void {
  return AndroidGmUtils.gracefulExitRestart();
}
