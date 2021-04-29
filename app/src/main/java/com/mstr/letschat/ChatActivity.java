package com.mstr.letschat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mstr.letschat.adapters.MessageCursorAdapter;
import com.mstr.letschat.bitmapcache.BitmapUtils;
import com.mstr.letschat.databases.ChatContract;
import com.mstr.letschat.databases.ChatContract.ChatMessageTable;
import com.mstr.letschat.databases.ChatMessageTableHelper;
import com.mstr.letschat.providers.DatabaseContentProvider;
import com.mstr.letschat.utils.FileUtils;
import com.mstr.letschat.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2021/4/13 0013.
 */

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,
        TextWatcher, LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private EditText messageText;
    private Button sendButton;

    private static final int REQUEST_IMAGE_PICKER = 2;
    private static final int REQUEST_FILE_PICKER = 3;
    private String to;
    private String nickname;
    private String body;
    private ListView messageListView;
    private CardView attachOptionsContainer;
    private Button attachGalleryButton;
    private Button attachFileButton;
    private MessageCursorAdapter adapter;
    private File file;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        to = "xxxxxxxxx";
        nickname = "彬";
        messageText = (EditText) findViewById(R.id.et_message);
        messageText.addTextChangedListener(this);
        sendButton = (Button) findViewById(R.id.btn_send);
        sendButton.setOnClickListener(this);
        sendButton.setEnabled(false);
        messageListView = (ListView) findViewById(R.id.message_list);
        adapter = new MessageCursorAdapter(this, null, 0);
        messageListView.setAdapter(adapter);
        messageListView.setOnItemClickListener(this);

        initAttachOptions();
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if (v == sendButton) {
            ContentValues values = null;
            try {
                long timeMills = System.currentTimeMillis();
                body = messageText.getText().toString();
                values = ChatMessageTableHelper.newPlainTextMessage(to, body, timeMills, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ContentResolver contentResolver = getContentResolver();
            Uri newMessageUri = null;
            try {
                newMessageUri = insertNewMessage(contentResolver, values);
            } catch (Exception e) {
                e.printStackTrace();
            }
            contentResolver.update(newMessageUri, ChatMessageTableHelper.newSuccessStatusContentValues(), null, null);
        }
        if (v == attachGalleryButton) {
            pickImage();
            return;
        }
        if (v == attachFileButton) {
            pickFile();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_attach:
                setAttachOptionsVisibility(attachOptionsContainer.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean setAttachOptionsVisibility(int visibility) {
        if (attachOptionsContainer.getVisibility() == visibility) {
            return false;
        }
        if (visibility == View.VISIBLE) {
            showAttachOptions();
            return true;
        } else if (visibility == View.INVISIBLE) {
            hideAttachOptions();
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void showAttachOptions() {
        if (Utils.hasLollipop()) {
            int cx = attachOptionsContainer.getWidth() / 2;
            int cy = attachOptionsContainer.getHeight() / 2;
            float endRadius = (float) Math.hypot(cx, cy);

            Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsContainer, cx, cy, 0, endRadius);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(getResources().getInteger(R.integer.attach_views_anim_duration));
            animator.addListener(new AnimatorListenerAdapter() {
            });
            attachOptionsContainer.setVisibility(View.VISIBLE);
            animator.start();
        } else {
            attachOptionsContainer.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void hideAttachOptions() {
        if (Utils.hasLollipop()) {
            int cx = attachOptionsContainer.getWidth() / 2;
            int cy = attachOptionsContainer.getHeight() / 2;
            float startRadius = (float) Math.hypot(cx, cy);

            Animator animator = ViewAnimationUtils.createCircularReveal(attachOptionsContainer, cx, cy, startRadius, 0);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    attachOptionsContainer.setVisibility(View.INVISIBLE);
                }
            });
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(getResources().getInteger(R.integer.attach_views_anim_duration));
            animator.start();
        } else {
            attachOptionsContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void initAttachOptions() {
        attachOptionsContainer = (CardView) findViewById(R.id.attach_options_container);
        attachGalleryButton = (Button) findViewById(R.id.attach_from_gallery);
        attachGalleryButton.setOnClickListener(this);
        attachFileButton = (Button) findViewById(R.id.attach_file);
        attachFileButton.setOnClickListener(this);
    }

    private void pickImage() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, REQUEST_IMAGE_PICKER);
    }

    private void pickFile() {
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickIntent.setType("*/*");
        pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(pickIntent, REQUEST_FILE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_PICKER:
                    String filename = String.valueOf(System.currentTimeMillis());
                    int size = this.getResources().getDimensionPixelSize(R.dimen.sent_image_size);
                    Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromStream(this, data.getData(), size, size);
                    long time = System.currentTimeMillis();
                    try {
                        file = createSentImageFile(this, filename, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ContentValues values;
                    values = ChatMessageTableHelper.newImageMessage(to, getString(R.string.image_message_body), time, file.getAbsolutePath(), true);
                    ContentResolver contentResolver = this.getContentResolver();
                    Uri newMessageUri = null;
                    try {
                        newMessageUri = insertNewMessage(contentResolver, values);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    contentResolver.update(newMessageUri, ChatMessageTableHelper.newFailureStatusContentValues(), null, null);
                    break;
                case REQUEST_FILE_PICKER:
                    String path = data.getData().getPath();
                    long time2 = System.currentTimeMillis();
                    try {
                        byte[] bytes = FileUtils.readBytes(this, data.getData());
                        file = createSentFile(this, FileUtils.getName(path), FileUtils.getEnd(data), bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ContentValues values2;
                    values2 = ChatMessageTableHelper.newFileMessage(to, FileUtils.getName(path), time2, file.getAbsolutePath(), true);
                    ContentResolver contentResolver1 = this.getContentResolver();
                    Uri newMessageUri2 = null;
                    try {
                        newMessageUri2 = insertNewMessage(contentResolver1, values2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    contentResolver1.update(newMessageUri2, ChatMessageTableHelper.newFailureStatusContentValues(), null, null);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File createSentImageFile(Context context, String fileName, Bitmap bitmap) throws IOException {
        file = new File(FileUtils.getSentImagesDir(context), fileName + FileUtils.IMAGE_EXTENSION);
        FileOutputStream outputStream = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.flush();
        outputStream.close();

        return file;
    }

    private File createSentFile(Context context, String filename, String fileend, byte[] bytes) {
        String end = null;
        if (fileend.equals(FileUtils.IMAGE_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_GIF_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_PNG_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_JPEG_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_APK_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_PPT_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_XLS_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_DOC_EXTENSION)) {
            end = fileend;
        } else if (fileend.equals(FileUtils.FILE_TXT_EXTENSION)) {
            end = fileend;
        }
        file = new File(FileUtils.getSentFileDir(context), filename + end);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(bytes);
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public Uri insertNewMessage(ContentResolver contentResolver,
                                ContentValues messageValues) throws RemoteException, OperationApplicationException {
        long timeMillis = messageValues.getAsLong(ChatContract.ChatMessageTable.COLUMN_NAME_TIME);
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(ChatContract.ChatMessageTable.CONTENT_URI).withValues(messageValues).build());
        ContentProviderResult[] result = contentResolver.applyBatch(DatabaseContentProvider.AUTHORITY, operations);
        return result[0].uri;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ChatMessageTable._ID,
                ChatMessageTable.COLUMN_NAME_MESSAGE,
                ChatMessageTable.COLUMN_NAME_TIME,
                ChatMessageTable.COLUMN_NAME_TYPE,
                ChatMessageTable.COLUMN_NAME_STATUS,
                ChatMessageTable.COLUMN_NAME_ADDRESS,
                ChatMessageTable.COLUMN_NAME_LATITUDE,
                ChatMessageTable.COLUMN_NAME_LONGITUDE,
                ChatMessageTable.COLUMN_NAME_MEDIA_URL
        };
        return new CursorLoader(this, ChatMessageTable.CONTENT_URI, projection,
                ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{to}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().trim().length() > 0) {
            sendButton.setEnabled(true);
        } else {
            sendButton.setEnabled(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ListView listView = (ListView) parent;
        Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        String filePath = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MEDIA_URL));
        if (filePath != null) {
            Intent intent = FileUtils.openFile(this, filePath);
            startActivity(intent);
        }
    }
}
