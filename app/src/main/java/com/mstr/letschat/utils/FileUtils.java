package com.mstr.letschat.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static android.os.Environment.isExternalStorageRemovable;

/**
 * Created by Administrator on 2021/4/9 0009.
 */

public class FileUtils {
    //    public static final String FILE_M4A_EXTENSION = ".m4a";
//    public static final String FILE_MP3_EXTENSION = ".mp3";
//    public static final String FILE_MID_EXTENSION = ".mid";
//    public static final String FILE_XMF_EXTENSION = ".xmf";
//    public static final String FILE_OGG_EXTENSION = ".ogg";
//    public static final String FILE_WAV_EXTENSION = ".wav";
//    public static final String FILE_AMR_EXTENSION = ".amr";
//    public static final String FILE_3GP_EXTENSION = ".3gp";
//    public static final String FILE_MP4_EXTENSION = ".mp4";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String FILE_GIF_EXTENSION = ".gif";
    public static final String FILE_PNG_EXTENSION = ".png";
    public static final String FILE_JPEG_EXTENSION = ".jpeg";
    //    public static final String FILE_BMP_EXTENSION = ".bmp";
    public static final String FILE_APK_EXTENSION = ".apk";
    public static final String FILE_PPT_EXTENSION = ".ppt";
    public static final String FILE_XLS_EXTENSION = ".xls";
    public static final String FILE_DOC_EXTENSION = ".doc";
    //    public static final String FILE_PDF_EXTENSION = ".pdf";
//    public static final String FILE_CHM_EXTENSION = ".chm";
    public static final String FILE_TXT_EXTENSION = ".txt";
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = isExternalStorageWritable() || !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static File getSentImagesDir(Context context) {
        File dir = new File(getImagesDir(context), "sent");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File getSentFileDir(Context context){
        File dir = new File(getFileDir(context),"sent");
        if (!dir.exists()){
            dir.mkdirs();
        }
        return dir;
    }

    private static File getImagesDir(Context context) {
        File file1 = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file2 = context.getDir("images", Context.MODE_PRIVATE);
        return isExternalStorageWritable() ? context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : context.getDir("images", Context.MODE_PRIVATE);
    }
    private static File getFileDir(Context context){
        return isExternalStorageWritable() ? context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS):context.getDir("files",Context.MODE_PRIVATE);
    }

    public static Intent openFile(Context context,String filePath) {
        if (filePath == null) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists())
            return null;
		/* 取得扩展名 */
        String end = file
                .getName()
                .substring(file.getName().lastIndexOf(".") + 1,
                        file.getName().length()).toLowerCase();
        end = end.trim().toLowerCase();
        if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")){
            return getImageFileIntent(context,filePath);
        }else if (end.equals("apk")){
            return getApkFileIntent(context,filePath);
        }else if (end.equals("ppt")) {
            return getPptFileIntent(context,filePath);
        } else if (end.equals("xls")) {
            return getExcelFileIntent(context,filePath);
        } else if (end.equals("doc")){
            return getWordFileIntent(context,filePath);
        } else if (end.equals("txt")) {
            return getTextFileIntent(context,filePath, false);
        }
        else {
            return getAllIntent(context,filePath);
        }
    }

    // Android获取一个用于打开图片文件的intent
    public static Intent getImageFileIntent(Context context,String param) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri photoURI = FileProvider.getUriForFile(context,
                "com.mstr.letschat.fileprovider",
                new File(param));//file即为所要共享的文件的file
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//授予临时权限别忘了
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(photoURI, "image/*");
        return intent;
    }

    // Android获取一个用于打开APK文件的intent
    public static Intent getApkFileIntent(Context context,String param) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, "com.mstr.letschat.fileprovider", new File(param));
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(new File(param)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    // Android获取一个用于打开PPT文件的intent
    public static Intent getPptFileIntent(Context context,String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = Uri.fromFile(new File(param));
        Uri uri = FileProvider.getUriForFile(context, "com.mstr.letschat.fileprovider", new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        return intent;
    }

    // Android获取一个用于打开Excel文件的intent
    public static Intent getExcelFileIntent(Context context,String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = Uri.fromFile(new File(param));
        Uri uri = FileProvider.getUriForFile(context, "com.mstr.letschat.fileprovider", new File(param));
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        return intent;
    }

    // Android获取一个用于打开Word文件的intent
    public static Intent getWordFileIntent(Context context,String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        Uri uri = Uri.fromFile(new File(param));
        Uri uri = FileProvider.getUriForFile(context, "com.mstr.letschat.fileprovider", new File(param));
        intent.setDataAndType(uri, "application/msword");
        return intent;
    }

    public static Intent getTextFileIntent(Context context,String param, boolean paramBoolean) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (paramBoolean) {
            Uri uri1 = Uri.parse(param);
            intent.setDataAndType(uri1, "text/plain");
        } else {

//            Uri uri2 = Uri.fromFile(new File(param));
            Uri uri2 = FileProvider.getUriForFile(context, "com.mstr.letschat.fileprovider", new File(param));
            intent.setDataAndType(uri2, "text/plain");
        }
        return intent;
    }

    // Android获取一个用于打开APK文件的intent
    public static Intent getAllIntent(Context context,String param) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(new File(param));
        intent.setDataAndType(uri, "*/*");
        return intent;
    }

    public static byte[] readBytes(Context context,Uri inUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(inUri);

        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
            Log.i("welhzh_f", "" + len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    public static String getName(String path){
        int start= path.lastIndexOf(":");
        int end = path.lastIndexOf(".");
        return path.substring(start+1,end);
    }

    public static String getEnd(Intent data){
        String path = data.getData().getPath();
        int start = path.lastIndexOf(".");
        return "." + path.substring(start+1,path.length());
    }
}
