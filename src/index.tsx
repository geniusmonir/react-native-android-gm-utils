import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-android-gm-utils' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

//@ts-ignore

const AndroidGmUtils = NativeModules.AndroidGmUtils
  ? NativeModules.AndroidGmUtils
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// Access AGUNotificationListenerModule
const NotificationListener = NativeModules.AGUNotificationListenerModule
  ? NativeModules.AGUNotificationListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

//@ts-ignore
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const FBNotificationListener = NativeModules.FBNotificationListenerModule
  ? NativeModules.FBNotificationListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// Access AGUUsageStats
const AGUUsageStats = NativeModules.AGUUsageStatsModule
  ? NativeModules.AGUUsageStatsModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export type AGUUsageRange =
  | 'day'
  | 'week'
  | 'month'
  | 'year'
  | 'last24hours'
  | 'last7days'
  | 'last30days';

export type AGUUsageMode = 'last' | 'standard';

export type NotificationListenerType = 'posted' | 'removed';

export type NotificationPermissionStatusType =
  | 'unknown'
  | 'authorized'
  | 'denied';

export interface NotificationGroupedMessage {
  title: string;
  text: string;
}

export interface NotificationPayload {
  listener: NotificationListenerType; // 'posted' or 'removed'
  time: string; // ISO 8601 string
  app: string; // app package name
  title: string; // title of the notification
  titleBig: string; // big title of the notification
  text: string; // text of the notification
  subText: string; // subtext of the notification
  summaryText: string; // summary text of the notification
  bigText: string; // big text of the notification
  audioContentsURI: string; // URI of the audio contents
  imageBackgroundURI: string; // URI of the image background
  extraInfoText: string; // extra info text
  groupedMessages: NotificationGroupedMessage[]; // grouped messages
  icon: string; // base64 encoded string
  image: string; // base64 encoded string (Does not works for telegram and whatsapp)
}

export interface AGUUsageStatsItem {
  packageName: string;
  totalTimeInForeground: number; // time in milliseconds
  lastTimeUsed: number; // timestamp of the last time the app was used
}

export const AGU_NOTIFICATION_LISTENER_HEADLESS_TASK =
  'AGU_NOTIFICATION_LISTENER_HEADLESS_TASK';

export const FB_NOTIFICATION_LISTENER_HEADLESS_TASK =
  'FB_NOTIFICATION_LISTENER_HEADLESS_TASK';

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

export function requestNotificationPermission(): void {
  return NotificationListener.requestPermission();
}

export function getNotificationPermissionStatus(): Promise<NotificationPermissionStatusType> {
  return NotificationListener.getPermissionStatus();
}

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
