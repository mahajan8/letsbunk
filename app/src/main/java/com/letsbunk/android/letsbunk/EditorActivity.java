package com.letsbunk.android.letsbunk;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.letsbunk.android.letsbunk.R;
import com.letsbunk.android.letsbunk.data.SubjectContract.SubjectEntry;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private AdView mBanner;

    private EditText mNameEditText;

    private EditText mAttendedEditText;

    private EditText mBunkedEditText;

    private EditText mMinimumEditText;

    private static final int EXISTING_SUBJECT_LOADER = 0;

    private Uri mCurrentSubjectUri;

    private boolean mSubjectHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mSubjectHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mAttendedEditText = (EditText) findViewById(R.id.edit_attended);
        mBunkedEditText = (EditText) findViewById(R.id.edit_bunked);
        mMinimumEditText = (EditText) findViewById(R.id.required);

        Intent intent = getIntent();
        mCurrentSubjectUri = intent.getData();

        if(mCurrentSubjectUri == null) {
            setTitle(R.string.add_subject);

            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_subject);

            getLoaderManager().initLoader(EXISTING_SUBJECT_LOADER,null,this);
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mAttendedEditText.setOnTouchListener(mTouchListener);
        mBunkedEditText.setOnTouchListener(mTouchListener);
        mMinimumEditText.setOnTouchListener(mTouchListener);

        mBanner = (AdView) findViewById(R.id.editor_adView);

        AdRequest adRequest = new AdRequest.Builder().build();

        mBanner.loadAd(adRequest);

    }

    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String attendedString = mAttendedEditText.getText().toString().trim();
        String bunkedString = mBunkedEditText.getText().toString().trim();
        String minimumString = mMinimumEditText.getText().toString().trim();

        if(mCurrentSubjectUri == null && TextUtils.isEmpty(nameString) || TextUtils.isEmpty(attendedString)
                || TextUtils.isEmpty(bunkedString) || TextUtils.isEmpty(minimumString)) {
            Toast.makeText(this, getString(R.string.empty_data), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(SubjectEntry.COLUMN_SUBJECT_NAME,nameString);

        values.put(SubjectEntry.COLUMN_SUBJECT_ATTENDED,Integer.parseInt(attendedString));
        values.put(SubjectEntry.COLUMN_SUBJECT_BUNKED,Integer.parseInt(bunkedString));
        values.put(SubjectEntry.COLUMN_SUBJECT_MINIMUM,Integer.parseInt(minimumString));

        if(mCurrentSubjectUri == null) {
            Uri newUri = getContentResolver().insert(SubjectEntry.CONTENT_URI,values);

            if(newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_subject_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_subject_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentSubjectUri,values,null,null);

            if(rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_subject_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_subject_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if(mCurrentSubjectUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentSubjectUri,null,null);

            if(rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_subject_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_subject_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentSubjectUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if(!mSubjectHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if(!mSubjectHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.unsaved_changes_dialog_msg);

        builder.setPositiveButton(R.string.discard,discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                SubjectEntry._ID,
                SubjectEntry.COLUMN_SUBJECT_NAME,
                SubjectEntry.COLUMN_SUBJECT_ATTENDED,
                SubjectEntry.COLUMN_SUBJECT_BUNKED,
                SubjectEntry.COLUMN_SUBJECT_MINIMUM};

        return new CursorLoader(this,
                mCurrentSubjectUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1 ) {
            return;
        }

        if(cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(SubjectEntry.COLUMN_SUBJECT_NAME));
            int attended = cursor.getInt(cursor.getColumnIndex(SubjectEntry.COLUMN_SUBJECT_ATTENDED));
            int bunked = cursor.getInt(cursor.getColumnIndex(SubjectEntry.COLUMN_SUBJECT_BUNKED));
            int minimum = cursor.getInt(cursor.getColumnIndex(SubjectEntry.COLUMN_SUBJECT_MINIMUM));

            mNameEditText.setText(name);
            mAttendedEditText.setText(Integer.toString(attended));
            mBunkedEditText.setText(Integer.toString(bunked));
            mMinimumEditText.setText(Integer.toString(minimum));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mAttendedEditText.setText("");
        mBunkedEditText.setText("");
        mMinimumEditText.setText("");
    }

}
