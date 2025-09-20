export type NotificationListenerType = 'posted' | 'removed' | 'active';

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
  iconLarge: string; // base64 encoded string
  image: string; // base64 encoded string (Does not works for telegram and whatsapp)
}
