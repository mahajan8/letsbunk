package com.letsbunk.android.letsbunk;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.letsbunk.android.letsbunk.R;
import com.letsbunk.android.letsbunk.data.SubjectContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class BunksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int SUBJECT_LOADER = 0;

    SubjectCursorAdapter mCursorAdapter ;

    private AdView mBanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bunks);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BunksActivity.this,EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView subjectListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        subjectListView.setEmptyView(emptyView);

        mCursorAdapter = new SubjectCursorAdapter(this,null);
        subjectListView.setAdapter(mCursorAdapter);

        getLoaderManager().initLoader(SUBJECT_LOADER,null,this);

        subjectListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(BunksActivity.this,EditorActivity.class);
                Uri currentSubjectUri = ContentUris.withAppendedId(SubjectContract.SubjectEntry.CONTENT_URI,id);

                intent.setData(currentSubjectUri);
                startActivity(intent);
            }
        });

        mBanner = (AdView) findViewById(R.id.bunks_adView);

        AdRequest adRequest = new AdRequest.Builder().build();

        mBanner.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bunks,menu);
        return true;
    }

    private void showDeleteDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog);

        builder.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteSubjects();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteSubjects() {
        int rowsDeleted = getContentResolver().delete(SubjectContract.SubjectEntry.CONTENT_URI,null,null);

        Toast.makeText(this, getString(R.string.all_subjects_deleted) + rowsDeleted, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteDialogue();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                SubjectContract.SubjectEntry._ID,
                SubjectContract.SubjectEntry.COLUMN_SUBJECT_NAME,
                SubjectContract.SubjectEntry.COLUMN_SUBJECT_ATTENDED,
                SubjectContract.SubjectEntry.COLUMN_SUBJECT_BUNKED,
                SubjectContract.SubjectEntry.COLUMN_SUBJECT_MINIMUM};

        return new CursorLoader(this,
                SubjectContract.SubjectEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
        }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
