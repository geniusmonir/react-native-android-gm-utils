export type UploadEvent = 'progress' | 'error' | 'completed' | 'cancelled';

export type NotificationArgs = {
  enabled?: boolean;
  autoClear?: boolean;
  notificationChannel?: string;
  enableRingTone?: boolean;
  onProgressTitle?: string;
  onProgressMessage?: string;
  onCompleteTitle?: string;
  onCompleteMessage?: string;
  onErrorTitle?: string;
  onErrorMessage?: string;
  onCancelledTitle?: string;
  onCancelledMessage?: string;
};

export type StartUploadArgs = {
  url: string;
  path: string;
  method?: 'PUT' | 'POST';
  type?: 'raw' | 'multipart';
  // This option is needed for multipart type
  field?: string;
  customUploadId?: string;
  // parameters are supported only in multipart type
  parameters?: { [key: string]: string };
  headers?: Object;
  notification?: NotificationArgs;
  useUtf8Charset?: boolean;

  retryOnConnectionFailure?: boolean;
  maxRetries?: number;
  connectTimeout?: number;
  writeTimeout?: number;
  readTimeout?: number;
};
