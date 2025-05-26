# react-native-android-gm-utils

Native Functionality Integration to React Native Android Application That does not exist on React Native Directory

## Installation

```sh
npm install react-native-android-gm-utils
```

```sh
yarn add react-native-android-gm-utils
```

## Usage

```js
import {
  requestNotificationPermission,
  getNotificationPermissionStatus,
  getCurrentNotifications,
  hasAppUsageAccess,
  requestAppUsageAccess,
  getAppUsageStats,
  AGU_NOTIFICATION_LISTENER_HEADLESS_TASK,
  FB_NOTIFICATION_LISTENER_HEADLESS_TASK,
  AGU_ACCESSIBILITY_LISTENER_HEADLESS_TASK,
  SCREEN_RECORDING_LISTENER_HEADLESS_TASK,
  AGUUsageRange,
  AGUStandardUsageRange,
  AGUUsageMode,
  NotificationListenerType,
  NotificationPermissionStatusType,
  NotificationPayload,
  AccessibilityContentPayload,
  AGUUsageStatsItem,
  isBatteryOptimizationEnabled,
  requestDisableBatteryOptimization,
  openBackgroundAutoStartSettings,
  gracefulExitRestart,
  hasAppUsageAccess,
  requestAppUsageAccess,
  getAppUsageStats,
  requestAccessibilityPermission,
  isAccessibilityServiceEnabled,
  setAccessibilityStorageDuration,
  setupScreenRecording,
  startScreenRecording,
  pauseScreenRecording,
  resumeScreenRecording,
  stopScreenRecording,
  isScreenRecording,
  getScreenRecordingStatus,
  checkOverlayPermission,
  requestOverlayPermission,
  showSROverlayButton,
  hideSROverlayButton,
} from 'react-native-android-gm-utils';
```

Need to install react native background fetch also, com.transistorsoft.tsbackgroundfetch.BootBroadcastReceiver to start this receiver.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
