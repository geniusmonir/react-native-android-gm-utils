import { AGUAccessibilityListener } from '../native/AndroidGmUtils';

export function requestAccessibilityPermission(): void {
  return AGUAccessibilityListener.requestAccessibilityPermission();
}

export function isAccessibilityServiceEnabled(): Promise<boolean> {
  return AGUAccessibilityListener.isAccessibilityServiceEnabled();
}

export function setAccessibilityStorageDuration(minutes: number): void {
  return AGUAccessibilityListener.setAccessibilityStorageDuration(minutes);
}
