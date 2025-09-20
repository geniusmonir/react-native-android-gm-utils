import { NativeModules, Platform } from 'react-native';
import type { ScreenRecordingListenerModule } from '../types/screenrecording.types';
import type { FileManagerListenerModule } from '../types/filemanager.types';

const LINKING_ERROR =
  `The package 'react-native-android-gm-utils' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export const AndroidGmUtils = NativeModules.AndroidGmUtils
  ? NativeModules.AndroidGmUtils
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const NotificationListener = NativeModules.AGUNotificationListenerModule
  ? NativeModules.AGUNotificationListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const FBNotificationListener = NativeModules.FBNotificationListenerModule
  ? NativeModules.FBNotificationListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const AGUUsageStats = NativeModules.AGUUsageStatsModule
  ? NativeModules.AGUUsageStatsModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const AGUAccessibilityListener =
  NativeModules.AGUAccessibilityListenerModule
    ? NativeModules.AGUAccessibilityListenerModule
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export const ScreenRecordingListener: ScreenRecordingListenerModule =
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

export const FileManagerListerner: FileManagerListenerModule =
  NativeModules.FileManagerListenerModule
    ? NativeModules.FileManagerListenerModule
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export const BGTimerListener = NativeModules.BGTimerListenerModule
  ? NativeModules.BGTimerListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const BGUploaderListener = NativeModules.BGUploaderListenerModule
  ? NativeModules.BGUploaderListenerModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
