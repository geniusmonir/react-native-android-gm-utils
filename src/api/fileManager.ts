import { FileManagerListerner } from '../native/AndroidGmUtils';

import type {
  AGUFolderStorage,
  AGUFileStorage,
} from '../types/filemanager.types';

export async function hasAGUFileManagerPermission(): Promise<boolean> {
  const granted = await FileManagerListerner.hasPermission();
  return granted;
}

export async function requestAGUFileManagerPermission(): Promise<void> {
  try {
    await FileManagerListerner.requestPermission();
  } catch (e) {
    console.log(e);
  }
}

export async function areAGUFMObserversRunning(): Promise<boolean> {
  const isRunning = await FileManagerListerner.areObserversRunning();
  return isRunning;
}

export async function startAGUObserversByPaths(paths: string[]): Promise<void> {
  try {
    FileManagerListerner.startObserversByPaths(paths);
  } catch (e) {
    console.log(e);
  }
}

export async function checkAndStopAGUFMObservers(): Promise<void> {
  try {
    FileManagerListerner.checkAndStopObservers();
  } catch (e) {
    console.log(e);
  }
}

export async function getAGUStorageRoots(): Promise<string[]> {
  try {
    const sroots = await FileManagerListerner.getStorageRoots();
    return sroots;
  } catch (err) {
    console.log(err);
    return [];
  }
}

export async function getAGUStorageTree(): Promise<AGUFolderStorage> {
  try {
    const sroots = await FileManagerListerner.scanStorageTree();
    return sroots;
  } catch (err) {
    console.log(err);
    return { id: '', storages: [] };
  }
}

export async function getAGUStorageAllFiles(): Promise<AGUFileStorage> {
  try {
    const files = await FileManagerListerner.scanAllFiles();
    return files;
  } catch (err) {
    console.log(err);
    return { id: '', storages: [] };
  }
}

export async function getAGUFMDefaultObserverPaths(): Promise<string[]> {
  try {
    const paths = await FileManagerListerner.getDefaultObserverPaths();
    return paths;
  } catch (err) {
    console.log(err);
    return [];
  }
}

export async function getAGUSPValue(key: string): Promise<string | null> {
  try {
    const val = await FileManagerListerner.getAGUSPValue(key);
    return val;
  } catch (err) {
    console.log(err);
    return null;
  }
}

export async function setAGUSPValue(key: string, value: string): Promise<void> {
  try {
    const val = await FileManagerListerner.setAGUSPValue(key, value);
    return val;
  } catch (err) {
    console.log(err);
  }
}
