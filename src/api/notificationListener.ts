import { NotificationListener } from '../native/AndroidGmUtils';
import type {
  NotificationPayload,
  NotificationPermissionStatusType,
} from '../types/notification.types';

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
