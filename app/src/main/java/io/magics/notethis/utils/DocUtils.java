package io.magics.notethis.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

//Util for file uploads. Based on Imgur Example app here:
//https://github.com/AKiniyalocts/imgur-android/blob/master/app/src/main/java/akiniyalocts/imgurapiexample/helpers/DocumentHelper.java
public class DocUtils {

    private static final Uri GOOGLE_URI = Uri.parse("com.google.android.apps.photos.content");
    private static final Uri MEDIA_URI = Uri.parse("com.android.providers.media.documents");
    private static final Uri DOWNLOADS_URI = Uri.parse("com.android.providers.downloads.documents");
    private static final Uri STORAGE_URI = Uri.parse("com.android.externalstorage.documents");


    public static String getPath(Context context, Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {

            final String docId = DocumentsContract.getDocumentId(uri);

            if (isDownloadsUri(uri)) {
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId)
                );
                return getDataColumn(context, contentUri, null, null);
            }

            final String[] idSplit = docId.split(":");
            final String type = idSplit[0];

            if (isStorageUri(uri) && "primary".equals(type)) {
                return Environment.getExternalStorageDirectory() + "/" + idSplit[1];
            } else if (isMediaUri(uri)) {
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[] {idSplit[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGoogleUri(uri)) return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri contentUri, String selection,
                                       String[] selectionArgs) {
        String[] projection = {"_data"};
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(contentUri, projection, selection,
                    selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow("_data");
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static boolean isGoogleUri(Uri uri) {
        return GOOGLE_URI.equals(uri);
    }

    public static boolean isMediaUri(Uri uri) {
        return MEDIA_URI.equals(uri);
    }

    public static boolean isDownloadsUri(Uri uri) {
        return DOWNLOADS_URI.equals(uri);
    }

    public static boolean isStorageUri(Uri uri) {
        return STORAGE_URI.equals(uri);
    }

}
