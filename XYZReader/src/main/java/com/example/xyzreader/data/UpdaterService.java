package com.example.xyzreader.data;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.example.xyzreader.remote.RemoteEndpointUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UpdaterService extends JobIntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";
    public static final String EXTRA_NO_NETWORK_CONNECTION
            = "com.example.xyzreader.intent.extra.NETWORK_CONNECTION";

    static final int JOB_ID = 1000;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, UpdaterService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            sendBroadcast(
                    new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_NO_NETWORK_CONNECTION, true));
            return;
        }

        sendBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        // Don't even inspect the intent, we only do one thing, and that's fetch content.
        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();

        Uri dirUri = ItemsContract.Items.buildDirUri();

        // Delete all items
        cpo.add(ContentProviderOperation.newDelete(dirUri).build());

        try {
            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
            if (array == null) {
                throw new JSONException("Invalid parsed item array");
            }

            for (int i = 0; i < array.length(); i++) {
                ContentValues values = new ContentValues();
                JSONObject object = array.getJSONObject(i);
                values.put(ItemsContract.Items.SERVER_ID, object.getString("id"));
                values.put(ItemsContract.Items.AUTHOR, object.getString("author"));
                values.put(ItemsContract.Items.TITLE, object.getString("title"));
                values.put(ItemsContract.Items.BODY, object.getString("body"));
                values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb"));
                values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo"));
                values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio"));
                values.put(ItemsContract.Items.PUBLISHED_DATE, object.getString("published_date"));
                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
            }

            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);

        } catch (JSONException | RemoteException | OperationApplicationException e) {
            Log.e(TAG, "Error updating content.", e);
        }

        sendBroadcast(
                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        Time time = new Time();
//
//        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo ni = cm.getActiveNetworkInfo();
//        if (ni == null || !ni.isConnected()) {
//            Log.w(TAG, "Not online, not refreshing.");
//            return;
//        }
//
//        sendStickyBroadcast(
//                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));
//
//        // Don't even inspect the intent, we only do one thing, and that's fetch content.
//        ArrayList<ContentProviderOperation> cpo = new ArrayList<ContentProviderOperation>();
//
//        Uri dirUri = ItemsContract.Items.buildDirUri();
//
//        // Delete all items
//        cpo.add(ContentProviderOperation.newDelete(dirUri).build());
//
//        try {
//            JSONArray array = RemoteEndpointUtil.fetchJsonArray();
//            if (array == null) {
//                throw new JSONException("Invalid parsed item array");
//            }
//
//            for (int i = 0; i < array.length(); i++) {
//                ContentValues values = new ContentValues();
//                JSONObject object = array.getJSONObject(i);
//                values.put(ItemsContract.Items.SERVER_ID, object.getString("id"));
//                values.put(ItemsContract.Items.AUTHOR, object.getString("author"));
//                values.put(ItemsContract.Items.TITLE, object.getString("title"));
//                values.put(ItemsContract.Items.BODY, object.getString("body"));
//                values.put(ItemsContract.Items.THUMB_URL, object.getString("thumb"));
//                values.put(ItemsContract.Items.PHOTO_URL, object.getString("photo"));
//                values.put(ItemsContract.Items.ASPECT_RATIO, object.getString("aspect_ratio"));
//                values.put(ItemsContract.Items.PUBLISHED_DATE, object.getString("published_date"));
//                cpo.add(ContentProviderOperation.newInsert(dirUri).withValues(values).build());
//            }
//
//            getContentResolver().applyBatch(ItemsContract.CONTENT_AUTHORITY, cpo);
//
//        } catch (JSONException | RemoteException | OperationApplicationException e) {
//            Log.e(TAG, "Error updating content.", e);
//        }
//
//        sendStickyBroadcast(
//                new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
//    }
}
