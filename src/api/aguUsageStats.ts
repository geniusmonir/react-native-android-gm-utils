import { AGUUsageStats } from '../native/AndroidGmUtils';
import type {
  AGUUsageMode,
  AGUUsageRange,
  AGUUsageStatsItem,
} from '../types/usage.types';

export function hasAppUsageAccess(): Promise<boolean> {
  return AGUUsageStats.hasUsageAccess();
}

export function requestAppUsageAccess(): void {
  return AGUUsageStats.requestUsageAccess();
}

export function getAppUsageStats(
  timeRange: AGUUsageRange,
  mode: AGUUsageMode
): Promise<AGUUsageStatsItem[]> {
  return AGUUsageStats.getAppUsageStats(timeRange, mode);
}
