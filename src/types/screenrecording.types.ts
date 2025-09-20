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
  notificationIcon?: string; // notification_icon

  maxFileSize?: number; // in bytes
  maxDuration?: number; // in seconds

  fps?: number;
  bitrate?: number;
  videoEncoder?: SRVideoEncoder;
  outputFormat?: SROutputFormat;
}

export interface ScreenRecordingListenerModule {
  setup(options: SRSetupOptions): Promise<void>;
  startRecording(): Promise<void>;
  pauseRecording(): Promise<void>;
  resumeRecording(): Promise<void>;
  requestOverlayPermission(): Promise<void>;
  showOverlayButton(): Promise<void>;
  showOverlayButtonVisible(): Promise<void>;
  showOverlayButtonDialog(): Promise<void>;
  hasOverlayPermission(): Promise<boolean>;
  stopRecording(): Promise<SROutputResult>;
  isRecording(): Promise<boolean>;
  getRecordingStatus(): Promise<SRCurrentStatus>;
}
