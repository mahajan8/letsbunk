package com.letsbunk.android.letsbunk;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.letsbunk.android.letsbunk.R;
import com.letsbunk.android.letsbunk.data.SubjectContract;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

/**
 * Created by intel on 6/6/2017.
 */

public class SubjectCursorAdapter extends CursorAdapter {

    int touchCount = 2;

    Toast mToast;

    private InterstitialAd mInterstitial;

    public SubjectCursorAdapter(Context context,Cursor c) {
        super(context,c,0);
        loadInterstitial(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final TextView subjectName = (TextView) view.findViewById(R.id.title);
        final TextView subjectAttendance = (TextView) view.findViewById(R.id.attendance);
        TextView bunkTip = (TextView) view.findViewById(R.id.tip);
        Button attendButton = (Button) view.findViewById(R.id.attendButton);
        Button bunkedButton = (Button) view.findViewById(R.id.bunkButton);

        final int id = cursor.getInt(cursor.getColumnIndex(SubjectContract.SubjectEntry._ID));
        final String name = cursor.getString(cursor.getColumnIndex(SubjectContract.SubjectEntry.COLUMN_SUBJECT_NAME));
        final int attended = cursor.getInt(cursor.getColumnIndex(SubjectContract.SubjectEntry.COLUMN_SUBJECT_ATTENDED));
        final int bunked = cursor.getInt(cursor.getColumnIndex(SubjectContract.SubjectEntry.COLUMN_SUBJECT_BUNKED));
        int minimum = cursor.getInt(cursor.getColumnIndex(SubjectContract.SubjectEntry.COLUMN_SUBJECT_MINIMUM));

        subjectName.setText(name);
        double attendance = calculate(attended,bunked);

        subjectAttendance.setText(context.getString(R.string.attendance) + " " +
                Double.toString(attendance) + "%");

        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(context,name + " " + context.getString(R.string.attended));
                int currentAttended = attended;
                currentAttended++;

                ContentValues values = new ContentValues();
                Uri currentUri = ContentUris.withAppendedId(SubjectContract.SubjectEntry.CONTENT_URI,id);

                values.put(SubjectContract.SubjectEntry.COLUMN_SUBJECT_ATTENDED,currentAttended);
                context.getContentResolver().update(currentUri,values,null,null);

                touchCount++;
            }
        });

        bunkedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast(context,name + " " + context.getString(R.string.bunked));
                int currentBunked = bunked;
                currentBunked++;

                ContentValues values = new ContentValues();
                Uri currentUri = ContentUris.withAppendedId(SubjectContract.SubjectEntry.CONTENT_URI,id);

                values.put(SubjectContract.SubjectEntry.COLUMN_SUBJECT_BUNKED,currentBunked);
                context.getContentResolver().update(currentUri,values,null,null);

                touchCount++;
            }
        });

        int bunks;

        if (attended == 0 && bunked == 0){
            bunkTip.setBackgroundResource(R.color.not_added);
            bunkTip.setText(context.getString(R.string.not_added));

        } else if(attendance>minimum) {
            bunks = bunksLeft(attended,bunked,minimum);

            bunkTip.setBackgroundResource(R.color.attendance_high);
            subjectAttendance.setTextColor(ContextCompat.getColor(context,R.color.attendance_high));


            if(bunks==0) {
                bunkTip.setText(context.getString(R.string.attendance_met));
            }else if(bunks<3) {
                bunkTip.setText(context.getString(R.string.bunk_under_three_one) + " " +
                        Integer.toString(bunks) + " " + context.getString(R.string.bunk_under_three_two));
            } else if(bunks<7) {
                bunkTip.setText(context.getString(R.string.bunk_over_three_one) + " " +
                        Integer.toString(bunks) + " " + context.getString(R.string.bunk_over_three_two));
            } else {
                bunkTip.setText(context.getString(R.string.bunk_over_seven));
            }

        } else if (attendance<minimum) {
            bunks = toAttend(attended,bunked,minimum);

            bunkTip.setBackgroundResource(R.color.attendance_low);
            subjectAttendance.setTextColor(ContextCompat.getColor(context,R.color.attendance_low));

            if(bunks<3) {
                bunkTip.setText(context.getString(R.string.attend_under_three_one) + " " +
                        Integer.toString(bunks) + " " + context.getString(R.string.attend_under_three_two));
            } else if(bunks<7) {
                bunkTip.setText(context.getString(R.string.attend_under_seven_one) + " " +
                        Integer.toString(bunks) + " " + context.getString(R.string.attend_under_seven_two));
            } else if(bunks<17) {
                bunkTip.setText(context.getString(R.string.attend_over_seven_one) + " " +
                        Integer.toString(bunks) + " " + context.getString(R.string.attend_over_seven_two));
            } else {
                bunkTip.setText(context.getString(R.string.attend_over_seventeen));
            }

        } else {
            bunkTip.setBackgroundResource(R.color.attendance_high);
            subjectAttendance.setTextColor(ContextCompat.getColor(context,R.color.attendance_high));

            bunkTip.setText(context.getString(R.string.attendance_equal));
        }


        if(touchCount==3) {
            showInterstitial(context);
            touchCount = 0;
        }

    }

    private double calculate (int attend,int bunk) {
        double attendance;
        int sum;
        sum = attend + bunk;

        attendance = (((double) attend)/sum)*100;

        double newAttendance = Math.round(attendance*100.0)/100.0;

        return newAttendance;
    }

    private int bunksLeft(int attend,int bunk,int min) {
        int bunksToGo;
        int i;

        for(i=0;;i++) {
            int newBunks = bunk + i;
            int sum = attend + newBunks;

            double attendance = (((double) attend)/sum)*100;

            if(attendance<min)
                break;
        }

        bunksToGo = i-1;

        return bunksToGo;
    }

    private int toAttend(int attend,int bunk,int min) {
        int toAttend;
        int i;

        for(i=0;;i++) {
            int newAttend = attend + i;
            int sum = newAttend + bunk;

            double attendance = (((double) newAttend)/sum)*100;

            if(attendance>min)
                break;
        }

        toAttend = i;

        return toAttend;
    }

    private void loadInterstitial(Context context) {
        mInterstitial = new InterstitialAd(context);
        mInterstitial.setAdUnitId(context.getString(R.string.interstitial_id));

        AdRequest ar = new AdRequest.Builder().build();
        mInterstitial.loadAd(ar);
    }

    private void showInterstitial(Context context) {
        if(mInterstitial.isLoaded()) {
            mInterstitial.show();
        }

        loadInterstitial(context);
    }

    private void showToast(Context context,String showString) {

        if(mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(context, showString, Toast.LENGTH_SHORT);

        mToast.show();
    }
}
