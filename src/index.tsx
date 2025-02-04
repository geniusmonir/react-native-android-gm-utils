import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-android-gm-utils' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

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

// Access FBNotificationListenerModule
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

export const AGU_NOTIFICATION_LISTENER_HEADLESS_TASK =
  'AGU_NOTIFICATION_LISTENER_HEADLESS_TASK';

export const FB_NOTIFICATION_LISTENER_HEADLESS_TASK =
  'FB_NOTIFICATION_LISTENER_HEADLESS_TASK';

export function multiply(a: number, b: number): Promise<number> {
  return AndroidGmUtils.multiply(a, b);
}

export function addNumbers(a: number, b: number): Promise<number> {
  return NotificationListener.addNumbers(a, b);
}

export function requestNotificationPermission(): void {
  return NotificationListener.requestPermission();
}

export function getNotificationPermissionStatus(): Promise<NotificationPermissionStatusType> {
  return NotificationListener.getPermissionStatus();
}

export function subtractNumbers(a: number, b: number): Promise<number> {
  return FBNotificationListener.subtractNumbers(a, b);
}
