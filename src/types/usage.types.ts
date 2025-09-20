export type AGUUsageRange =
  | 'day'
  | 'week'
  | 'month'
  | 'year'
  | 'last24hours'
  | 'last7days'
  | 'last30days';

export type AGUStandardUsageRange = 'day' | 'week' | 'month' | 'year';

export type AGUUsageMode = 'last' | 'standard';

export interface AGUUsageStatsItem {
  packageName: string;
  totalTimeInForeground: number; // time in milliseconds
  lastTimeUsed: number; // timestamp of the last time the app was used
}
