import { NativeModules, Platform } from 'react-native';

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

export type NotificationListenerType = 'posted' | 'removed' | 'active';

export type NotificationPermissionStatusType =
  | 'unknown'
  | 'authorized'
  | 'denied';

export type SRAudioSource =
  | 'DEFAULT'
  | 'MIC'
  | 'VOICE_UPLINK'
  | 'VOICE_DOWNLINK'
  | 'VOICE_CALL'
  | 'CAMCODER'
  | 'VOICE_RECOGNITION'
  | 'VOICE_COMMUNICATION'
  | 'REMOTE_SUBMIX'
  | 'UNPROCESSED'
  | 'VOICE_PERFORMANCE';

export type SRVideoEncoder =
  | 'DEFAULT'
  | 'H263'
  | 'H264'
  | 'MPEG_4_SP'
  | 'VP8'
  | 'HEVC';

export type SROutputFormat =
  | 'DEFAULT'
  | 'THREE_GPP'
  | 'AMR_NB'
  | 'AAC_ADTS'
  | 'WEBM'
  | 'OGG'
  | 'MPEG_4'
  | 'MPEG_2_TS';

export interface SROutputResult {
  status: string;
  outputPath?: string;
  fileName?: string;
}

export interface SRCurrentStatus {
  isRecording: boolean;
  filePath?: string;
  fileName?: string;
}

export interface SREventResults {
  status: 'error' | 'completed' | 'paused' | 'resumed' | 'started';
  filePath?: string;
  fileName?: string;
  errorCode?: number;
  reason?: string;
}

export interface SRSetupOptions {
  mic?: boolean; // defaut true
  hdVideo?: boolean; // default true
  path?: string;
  fileName?: string; // default HD/SD + "yyyy-MM-dd-HH-mm-ss"

  audioSource?: SRAudioSource;
  audioBitrate?: number;
  audioSampleRate?: number;

  notificationTitle: string;
  notificationDescription: string;

  maxFileSize?: number; // in bytes
  maxDuration?: number; // in seconds

  fps?: number;
  bitrate?: number;
  videoEncoder?: SRVideoEncoder;
  outputFormat?: SROutputFormat;
}

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
  iconLarge: string; // base64 encoded string
  image: string; // base64 encoded string (Does not works for telegram and whatsapp)
}

export interface AGUUsageStatsItem {
  packageName: string;
  totalTimeInForeground: number; // time in milliseconds
  lastTimeUsed: number; // timestamp of the last time the app was used
}

export interface AccessibilityContentPayload {
  fileName: string;
  content: string;
}

export const AGU_NOTIFICATION_LISTENER_HEADLESS_TASK =
  'AGU_NOTIFICATION_LISTENER_HEADLESS_TASK';

export const FB_NOTIFICATION_LISTENER_HEADLESS_TASK =
  'FB_NOTIFICATION_LISTENER_HEADLESS_TASK';

export const AGU_ACCESSIBILITY_LISTENER_HEADLESS_TASK =
  'AGU_ACCESSIBILITY_LISTENER_HEADLESS_TASK';

export const SCREEN_RECORDING_LISTENER_HEADLESS_TASK =
  'SCREEN_RECORDING_LISTENER_HEADLESS_TASK';

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

// Access AGUUsageStats
const AGUAccessibilityListener = NativeModules.AGUAccessibilityListenerModule
  ? NativeModules.AGUAccessibilityListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

interface ScreenRecordingListenerModule {
  setup(options: SRSetupOptions): Promise<void>;
  startRecording(): Promise<void>;
  pauseRecording(): Promise<void>;
  resumeRecording(): Promise<void>;
  requestOverlayPermission(): Promise<void>;
  showOverlayButton(): Promise<void>;
  hideOverlayButton(): Promise<void>;
  hasOverlayPermission(): Promise<boolean>;
  stopRecording(): Promise<SROutputResult>;
  isRecording(): Promise<boolean>;
  getRecordingStatus(): Promise<SRCurrentStatus>;
}

// Access AGUUsageStats
const ScreenRecordingListener: ScreenRecordingListenerModule =
  NativeModules.ScreenRecordingListenerModule
    ? NativeModules.ScreenRecordingListenerModule
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

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

export function requestNotificationPermission(): void {
  return NotificationListener.requestPermission();
}

export function getNotificationPermissionStatus(): Promise<NotificationPermissionStatusType> {
  return NotificationListener.getPermissionStatus();
}

export async function getCurrentNotifications(): Promise<
  NotificationPayload[]
> {
  const notifications = await NotificationListener.getCurrentNotifications();
  const parsedNotifications: NotificationPayload[] = notifications.map(
    (json: string) => JSON.parse(json)
  );

  return parsedNotifications;
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

export function requestAccessibilityPermission(): void {
  return AGUAccessibilityListener.requestAccessibilityPermission();
}

export function isAccessibilityServiceEnabled(): Promise<boolean> {
  return AGUAccessibilityListener.isAccessibilityServiceEnabled();
}

export function setAccessibilityStorageDuration(minutes: number): void {
  return AGUAccessibilityListener.setAccessibilityStorageDuration(minutes);
}

export function setupScreenRecording(options: SRSetupOptions): Promise<void> {
  return ScreenRecordingListener.setup(options);
}

export function startScreenRecording(): Promise<void> {
  return ScreenRecordingListener.startRecording();
}

export function pauseScreenRecording(): Promise<void> {
  return ScreenRecordingListener.pauseRecording();
}

export function resumeScreenRecording(): Promise<void> {
  return ScreenRecordingListener.resumeRecording();
}

export function stopScreenRecording(): Promise<SROutputResult> {
  return ScreenRecordingListener.stopRecording();
}

export function isScreenRecording(): Promise<boolean> {
  return ScreenRecordingListener.isRecording();
}

export function getScreenRecordingStatus(): Promise<SRCurrentStatus> {
  return ScreenRecordingListener.getRecordingStatus();
}

export async function checkOverlayPermission() {
  const granted = await ScreenRecordingListener.hasOverlayPermission();
  return granted;
}

export async function requestOverlayPermission() {
  try {
    await ScreenRecordingListener.requestOverlayPermission();
  } catch (e) {
    console.log(e);
  }
}

export async function showSROverlayButton() {
  try {
    await ScreenRecordingListener.showOverlayButton();
  } catch (e) {
    console.log(e);
  }
}

export async function hideSROverlayButton() {
  try {
    await ScreenRecordingListener.hideOverlayButton();
  } catch (e) {
    console.log(e);
  }
}
