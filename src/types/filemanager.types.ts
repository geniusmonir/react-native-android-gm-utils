export interface AGUFolder {
  name: string;
  files: string[];
  size: number;
  modification: number;
  folders: AGUFolder[];
}

export interface AGUFolderStorage {
  id: string;
  storages: string[];
  storage0?: AGUFolder;
  storage1?: AGUFolder;
  storage2?: AGUFolder;
}

export interface AGUFile {
  fileName: string;
  filePath: string;
  fileUri: string;
  modification: number;
  size: number;
  extname: string;
  fileType: string;
}

export interface AGUEventFile extends AGUFile {
  eventName: string;
  isDir: boolean;
  fileUriCache?: string;
}

export interface AGUEventFileEvents {
  count: number;
  batchTimestamp: string;
  events: AGUEventFile[];
}

export interface AGUFiles {
  name: string;
  files: AGUFile[];
}
export interface AGUFileStorage {
  id: string;
  storages: string[];
  storage0?: AGUFiles;
  storage1?: AGUFiles;
  storage2?: AGUFiles;
}

export interface FileManagerListenerModule {
  hasPermission(): Promise<boolean>;
  requestPermission(): Promise<void>;
  areObserversRunning(): Promise<boolean>;
  startObserversByPaths(paths: string[]): Promise<void>;
  checkAndStopObservers(): Promise<void>;
  getStorageRoots(): Promise<string[]>;
  scanStorageTree(): Promise<AGUFolderStorage>;
  scanAllFiles(): Promise<AGUFileStorage>;
  setAGUSPValue(key: string, value: string): Promise<void>;
  getAGUSPValue(key: string): Promise<string | null>;
  getDefaultObserverPaths(): Promise<string[]>;
}
