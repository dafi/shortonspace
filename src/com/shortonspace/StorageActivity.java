package com.shortonspace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

@SuppressWarnings("unchecked")
public class StorageActivity extends ListActivity implements OnItemClickListener {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.storage_activity);

		getListView().setOnItemClickListener(this);
		fillStorageList();
	}

	private void fillStorageList() {
		ArrayList<Map<String, Object>> storageList = new ArrayList<Map<String, Object>>();
		
		storageList.add(createStorageInfoMap(new File("/"), R.string.whole_file_system));
		storageList.add(createStorageInfoMap(Environment.getRootDirectory(), R.string.root_file_system));
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			storageList.add(createStorageInfoMap(Environment.getExternalStorageDirectory(), R.string.sd_file_system));
		}
		String publicDir[] = new String[] {
				Environment.DIRECTORY_DOWNLOADS,
				Environment.DIRECTORY_MOVIES,
				Environment.DIRECTORY_MUSIC,
				Environment.DIRECTORY_PICTURES
		};
		int[] publicDescriptions = new int[] {
				R.string.folder_download,
				R.string.folder_movies,
				R.string.folder_music,
				R.string.folder_pictures
		};
		for (int i = 0; i < publicDir.length; i++) {
			File f = Environment.getExternalStoragePublicDirectory(publicDir[i]);
			
			if (f.exists()) {
				storageList.add(createStorageInfoMap(f, publicDescriptions[i]));
			}
		}
		StorageArrayAdapter adapter = new StorageArrayAdapter(this,
				android.R.layout.simple_list_item_1, storageList);
		setListAdapter(adapter);
	}
	
	public Map<String, Object> createStorageInfoMap(File storageFile, int descriptionId) {
		StatFs stat = new StatFs(storageFile.getPath());

		long bytesCapacity = (long) stat.getBlockSize() * (long) stat.getBlockCount();
		long bytesFree = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
		long bytesUsed = bytesCapacity - bytesFree;
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(StorageArrayAdapter.STORAGE_PATH, storageFile.getAbsolutePath());
		map.put(StorageArrayAdapter.STORAGE_DESCRIPTION, getResources().getString(descriptionId));
		if (bytesCapacity != 0) {
			map.put(StorageArrayAdapter.STORAGE_CAPACITY, bytesCapacity);
			map.put(StorageArrayAdapter.STORAGE_USED_SPACE, bytesUsed);
			map.put(StorageArrayAdapter.STORAGE_FREE_SPACE, bytesFree);
		}
		
		return map;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Map<String, Object> map = (Map<String, Object>) parent
				.getItemAtPosition(position);

		Intent intent = new Intent(this, FolderRetrieverActivity.class);
		Bundle bundle = new Bundle();

		bundle.putString("startFolder",
				(String) map.get(StorageArrayAdapter.STORAGE_PATH));
		intent.putExtras(bundle);

		this.startActivity(intent);
	}
}
