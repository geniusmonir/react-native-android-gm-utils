import { ScreenRecordingListener } from '../native/AndroidGmUtils';
import type {
  SRCurrentStatus,
  SROutputResult,
  SRSetupOptions,
} from '../types/screenrecording.types';

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

export async function showSROverlayButtonVisible() {
  try {
    await ScreenRecordingListener.showOverlayButtonVisible();
  } catch (e) {
    console.log(e);
  }
}

export async function showSROverlayButtonDialog() {
  try {
    await ScreenRecordingListener.showOverlayButtonDialog();
  } catch (e) {
    console.log(e);
  }
}
