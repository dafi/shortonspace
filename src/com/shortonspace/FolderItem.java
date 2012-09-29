package com.shortonspace;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FolderItem {
	private ArrayList<FolderItem> subfolders;
	private FolderItem parent;
	private File file;
	private boolean isFolderObject;
	private boolean isFileObject;
	private long fileSize;
	private long subFolderSize;
	private long subFoldersCount;
	private long subFilesCount;

	public FolderItem(File path, FolderItem parent) {
		this.file = path;
		this.subfolders = new ArrayList<FolderItem>();
		this.parent = parent;
		
		initAttrs();
	}

	private void initAttrs() {
        try {
			isFileObject = file.isFile() || Utils.isSymlink(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
        isFolderObject = !isFileObject;
        
        if (isFolderObject) {
            fileSize = 0;
        } else {
            fileSize = file.length();
        }
	}

	public List<FolderItem> getSubfolders() {
		return subfolders;
	}

	public void setSubfolders(List<FolderItem> subfolders) {
		this.subfolders.clear();
		this.subfolders.addAll(subfolders);
	}
	
	public FolderItem getParent() {
		return parent;
	}

	public void setParent(FolderItem parent) {
		this.parent = parent;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		initAttrs();
	}

	public boolean isFolderObject() {
		return isFolderObject;
	}

	public boolean isFileObject() {
		return isFileObject;
	}

	public long getFileSize() {
		return fileSize;
	}

	public long getSubFoldersSize() {
		return subFolderSize;
	}

	public void addSubFoldersSize(long subFolderSize) {
		this.subFolderSize += subFolderSize;
	}
	
	public String toString() {
		return getFile().getAbsolutePath();
	}

	public long getSubFilesCount() {
		return subFilesCount;
	}

	public long getSubFoldersCount() {
		return subFoldersCount;
	}

	public void addSubFoldersCount(long i) {
		subFoldersCount += i;
	}

	public void addSubFilesCount(long i) {
		subFilesCount += i;
	}
	
	public boolean renameTo(String name) {
        File newPath = new File(getFile().getParentFile(), name);
        boolean result = getFile().renameTo(newPath);
        if (result) {
        	setFile(newPath);
        }
        return result;
	}
	
	public boolean delete() {
		boolean result = isFileObject ? getFile().delete() : deleteFolder(this);
		
		if (result) {
			parent.deleteFromSubs(this);
		}
		
		return result;
	}

	private static boolean deleteFolder(FolderItem root) {
		boolean result = true;
		List<FolderItem> arr = root.getSubfolders();
		long removedFolders = 0;
		long removedFiles = 0;

		for (int i = arr.size() - 1; i >= 0; i--) {
			FolderItem file = arr.get(i);

			if (file.isFolderObject()) {
				if (deleteFolder(file)) {
					--file.subFoldersCount;
					++removedFolders;
					arr.remove(file);
				} else {
					result = false;
				}
			} else {
				if (file.getFile().delete()) {
					--file.subFilesCount;
					++removedFiles;
					arr.remove(file);
				} else {
					result = false;
				}
			}
		}
		// delete itself 
		if (root.getFile().delete()) {
			++removedFolders;
			root.parent.subfolders.remove(root);
			for (FolderItem p = root.parent; p != null; p = p.parent) {
				p.subFoldersCount -= removedFolders;
				p.subFilesCount -= removedFiles;
			}
		} else {
			result = false;
		}
		
		return result;
	}
	
	private boolean deleteFromSubs(FolderItem folderItem) {
		long sizeToDecrease;
		long subFilesCountToDecrease;
		long subFoldersCountDecrease;
		
		if (folderItem.isFileObject()) {
			sizeToDecrease = folderItem.fileSize;
			subFilesCountToDecrease = 1;
			subFoldersCountDecrease = 0;
		} else {
			sizeToDecrease = folderItem.subFolderSize;
			subFilesCountToDecrease = folderItem.subFilesCount;
			subFoldersCountDecrease = 1 + folderItem.subFoldersCount;
		}

		boolean result = subfolders.remove(folderItem);
		if (result) {
			for(FolderItem p = this; p != null; p = p.parent) {
				p.addSubFilesCount(-subFilesCountToDecrease);
				p.addSubFoldersCount(-subFoldersCountDecrease);
				p.addSubFoldersSize(-sizeToDecrease);
			}
			
		}
		return result;
	}

    public static Comparator<FolderItem> fileSizeComparator = new Comparator<FolderItem>() {
		public int compare(FolderItem lhs, FolderItem rhs) {
			long lhsSize = lhs.isFolderObject() ? lhs.getSubFoldersSize() : lhs.getFileSize();
			long rhsSize = rhs.isFolderObject() ? rhs.getSubFoldersSize() : rhs.getFileSize();
			
			int retVal = (int) (rhsSize - lhsSize);
			if (retVal == 0) {
				return lhs.getFile().getName().compareTo(rhs.getFile().getName());
			}
			return retVal;
		}
	};
	
	public List<FolderItem> readFolder(AtomicBoolean running, boolean recursive) {
		ArrayList<FolderItem> arr = new ArrayList<FolderItem>();

		File[] list = getFile().listFiles();
		
		if (list == null) {
			return arr;
		}
		for (int i = 0; running.get() && (i < list.length); i++) {
			FolderItem fi = new FolderItem(list[i], this);
			
			if (recursive && fi.isFolderObject()) {
				fi.setSubfolders(fi.readFolder(running, recursive));
			}
			if (fi.isFolderObject()) {
				addSubFoldersSize(fi.getSubFoldersSize());
				addSubFoldersCount(1);
			} else {
				addSubFoldersSize(fi.getFileSize());
				addSubFilesCount(1);
			}
			addSubFoldersCount(fi.getSubFoldersCount());
			addSubFilesCount(fi.getSubFilesCount());
			arr.add(fi);
		}
		Collections.sort(arr, fileSizeComparator);
		return arr;
	}

	public static FolderItem getFolderItemTree(String fullPath, AtomicBoolean running, boolean recursive) {
		try {
			FolderItem root = new FolderItem(new File(fullPath), null);
			root.setSubfolders(root.readFolder(running, recursive));
			
			return root;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
