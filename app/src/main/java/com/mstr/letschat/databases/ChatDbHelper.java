package com.mstr.letschat.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2021/4/13 0013.
 */

public class ChatDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "chat.db";

    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";

    private static ChatDbHelper instance;

    public static synchronized ChatDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ChatDbHelper(context.getApplicationContext());
        }

        return instance;
    }

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        ChatMessageTableHelper.onCreate(db);
//        ConversationTableHelper.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == 2) {
            updateToVersion2(db);
            return;
        }
    }

    private void updateToVersion2(SQLiteDatabase db) {

    }
}
