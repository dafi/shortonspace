package com.shortonspace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unchecked")
public class FolderRetrieverActivity extends ListActivity implements
		OnItemClickListener, ActionBar.OnNavigationListener {
	private String startFolder;
	private ArrayAdapter<FolderItem> parentsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.retriever_activity);

        registerForContextMenu(getListView());
		getListView().setOnItemClickListener(this);
//		new ViewSwipeDetector(getListView(), new DefaultViewSwipeListener() {
//			public void onRightToLeftSwipe() {
//				moveUp();
//			}
//		});

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayShowTitleEnabled(false);
		
		Bundle bundle = getIntent().getExtras();
		startFolder = bundle.getString("startFolder");
		new FolderRetrieverAsyncTask(this).execute(new String[] { startFolder });
	}

	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.retriever_context_menu, menu);

		AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)menuInfo;
		FolderItem fi = (FolderItem) getListView().getItemAtPosition(contextMenuInfo.position);

		menu.setHeaderTitle(fi.getFile().getName());
		menu.findItem(R.id.view_file).setVisible(!fi.isFolderEmpty());
	}	

    public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		FolderItem fi = (FolderItem) getListView().getItemAtPosition(contextMenuInfo.position);

		switch (item.getItemId()) {
            case R.id.view_file:
            	viewFolderItem(fi, true);
            	break;
            case R.id.rename_file:
            	renameFolderItem(fi);
            	break;
            case R.id.delete_file:
            	deleteFolderItem(fi);
            	break;
        }
        return true;
    }

    private void renameFolderItem(final FolderItem fi) {
    	final EditText editText = new EditText(this);
    	final File currFile = fi.getFile();
    	final Context context = this;
		editText.setText(currFile.getName());
    	
		new AlertDialog.Builder(this)
        .setTitle(R.string.rename_file)
        .setMessage(currFile.getName())
        .setView(editText)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = editText.getText().toString();
                if (!name.equals(currFile.getName())) {
                    if (fi.renameTo(name)) {
                    	((ArrayAdapter<FolderItem>)getListView().getAdapter()).notifyDataSetChanged();
                    } else {
            			Toast.makeText(
            					context,
            					context.getResources().getString(
            							R.string.error_unable_to_rename), Toast.LENGTH_LONG)
            					.show();
                    }
                }
            }
        }).setNegativeButton(android.R.string.cancel, null)
        .show();
	}

    private void deleteFolderItem(final FolderItem fi) {
    	final File currFile = fi.getFile();
    	final Context context = this;
    	String message = getResources().getString(fi.isFileObject() ? R.string.delete_file_message : R.string.delete_folder_message);

		new AlertDialog.Builder(this)
        .setTitle(R.string.confirm_delete_file)
        .setMessage(String.format(message, currFile.getName()))
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	FolderItem parent = fi.getParent();
                if (fi.delete()) {
            		updateFolderRootText(parent);
                	((ArrayAdapter<FolderItem>)getListView().getAdapter()).notifyDataSetChanged();
                } else {
        			Toast.makeText(
        					context,
        					context.getResources().getString(
        							R.string.error_unable_to_delete), Toast.LENGTH_LONG)
        					.show();
                }
            }
        }).setNegativeButton(R.string.no, null)
        .show();
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		ListView list = (ListView) parent;
		FolderItem fi = (FolderItem) list.getItemAtPosition(position);
		viewFolderItem(fi, false);
	}

	private void viewFolderItem(FolderItem fi, boolean viewFile) {
		boolean isFolderEmpty = fi.getSubFilesCount() == 0 && fi.getSubFoldersCount() == 0;

		if (fi.isFolderObject()) {
			if (!isFolderEmpty) {
				fillParentList(fi);
				// foldersList is updated inside onItemSelected
				ActionBar actionBar = getActionBar();
				actionBar.setSelectedNavigationItem(actionBar.getNavigationItemCount() - 1);
			}
		} else if (viewFile) {
			try {
				Uri uriFile = Uri.fromFile(fi.getFile());
				String fileExtension = MimeTypeMap
						.getFileExtensionFromUrl(uriFile.toString());
				String mimeType = MimeTypeMap.getSingleton()
						.getMimeTypeFromExtension(fileExtension);

				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);  
				intent.setDataAndType(uriFile, mimeType);
				startActivity(intent);
			} catch (Exception e) {
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle(getResources().getString(R.string.error_unable_to_view_file));
				alertDialog.setMessage(e.getMessage());
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(android.R.string.ok), (Message)null);
				alertDialog.show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			new FolderRetrieverAsyncTask(this).execute(new String[] {startFolder});
			break;
		case R.id.moveup:
			moveUp();
			break;
		case R.id.about:
			showAboutDialog();
			break;
		}
		return true;
	}

	private void showAboutDialog() {
		String aboutText = String.format(getResources().getString(R.string.about_text), getResources().getString(R.string.app_version));
		new AlertDialog.Builder(this)
			        .setTitle(R.string.app_name)
        .setIcon(R.drawable.ic_launcher)
        .setMessage(aboutText )
        .setNegativeButton(android.R.string.ok, null)
        .show();
	}

	private void fillFoldersList(FolderItem root) {
		FolderItemArrayAdapter adapter = new FolderItemArrayAdapter(this,
				android.R.layout.simple_list_item_1, root.getSubfolders());
		setListAdapter(adapter);
		
		updateFolderRootText(root);
	}

	private void updateFolderRootText(FolderItem root) {
		FolderItemArrayAdapter.setRowText(this, root, 0xff000000, null,
				(TextView) findViewById(R.id.filecount),
				(TextView) findViewById(R.id.filesize));
	}

	private void fillParentList(FolderItem root) {
		ArrayList<FolderItem> list = new ArrayList<FolderItem>();
		for (FolderItem fi = root; fi != null; fi = fi.getParent()) {
			list.add(0, fi);
		}
		parentsAdapter = new ArrayAdapter<FolderItem>(
				this, R.layout.path_spinner, list);
		parentsAdapter
				.setDropDownViewResource(R.layout.path_spinner_dropdown_item);

		ActionBar actionBar = getActionBar();
		actionBar.setListNavigationCallbacks(parentsAdapter, this);
	}

	private boolean moveUp() {
		int position = getActionBar().getSelectedNavigationIndex();

		if (position > 0) {
			getActionBar().setSelectedNavigationItem(position - 1);
			return true;
		}
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (moveUp()) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		FolderItem fi = parentsAdapter.getItem(itemPosition);

		if (fi.isFolderObject()) {
			// remove subfolders from list
			for (int i = parentsAdapter.getCount() - 1; i > itemPosition; i--) {
				parentsAdapter.remove(parentsAdapter.getItem(i));
			}
			fillFoldersList(fi);
		}
		return true;
	}
	
	private class FolderRetrieverAsyncTask extends
			AsyncTask<String, Object, FolderItem> {
		ProgressDialog progressDialog;
		private final Context context;

		public FolderRetrieverAsyncTask(Context context) {
			this.context = context;
			progressDialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			progressDialog.setMessage(context.getResources().getString(
					R.string.preparing));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(1);
			progressDialog
					.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							if (getStatus() != AsyncTask.Status.FINISHED) {
								cancel(true);
							}
						}
					});
			progressDialog.show();
		}

		@Override
		protected FolderItem doInBackground(String... params) {
			int i = 1;
			AtomicBoolean running = new AtomicBoolean(true);
			FolderItem root = FolderItem.getFolderItemTree(params[0], running,
					false);
			Object progress[] = { null, null, root.getSubfolders().size() };

			for (FolderItem fi : root.getSubfolders()) {
				if (isCancelled()) {
					running.set(false);
					break;
				}
				fi.setSubfolders(fi.readFolder(running, true));
				// root was filled not recursively so now we update subfolders size
				root.addSubFoldersSize(fi.getSubFoldersSize());
				root.addSubFilesCount(fi.getSubFilesCount());
				root.addSubFoldersCount(fi.getSubFoldersCount());
				progress[0] = Integer.valueOf((int) i);
				progress[1] = fi.getFile().getName();
				publishProgress(progress);
				++i;
			}

			Collections.sort(root.getSubfolders(),
					FolderItem.fileSizeComparator);
			return root;
		}

		@Override
		protected void onProgressUpdate(Object... progress) {
			progressDialog.setMax((Integer) progress[2]);
			progressDialog.setMessage((String) progress[1]);
			progressDialog.setProgress((Integer) progress[0]);
		}

		@Override
		protected void onCancelled(FolderItem root) {
			// onCancelled is called starting since Honeycomb
			Toast.makeText(
					context,
					context.getResources().getString(
							R.string.scanning_not_completed), Toast.LENGTH_LONG)
					.show();
			fillFoldersList(root);
			fillParentList(root);
		}

		@Override
		protected void onPostExecute(FolderItem root) {
			progressDialog.dismiss();
			fillFoldersList(root);
			fillParentList(root);
		}
	}
}
