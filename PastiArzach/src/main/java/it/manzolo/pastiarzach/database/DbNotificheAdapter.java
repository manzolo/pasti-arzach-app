package it.manzolo.pastiarzach.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbNotificheAdapter {
    @SuppressWarnings("unused")
    private static final String LOG_TAG = DbNotificheAdapter.class.getSimpleName();

    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    // Database fields
    private static final String DATABASE_TABLE = "notifiche";

    public static final String KEY_CONTACTID = "_id";
    public static final String KEY_NOTIFICATION_DATE = "notification_date";

    public DbNotificheAdapter(Context context) {
        this.context = context;
    }

    public DbNotificheAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    private ContentValues createContentValues(String notification_date) {
        ContentValues values = new ContentValues();
        values.put(KEY_NOTIFICATION_DATE, notification_date);

        return values;
    }

    // create a contact
    public long createNotification(String notification_date) {
        ContentValues initialValues = createContentValues(notification_date);
        return database.insertOrThrow(DATABASE_TABLE, null, initialValues);
    }

    // update a contact
    public boolean updateNotification(long contactID, String notification_date) {
        ContentValues updateValues = createContentValues(notification_date);
        return database.update(DATABASE_TABLE, updateValues, KEY_CONTACTID
                + "=" + contactID, null) > 0;
    }

    // delete a contact
    public boolean deleteNotification(long contactID) {
        return database.delete(DATABASE_TABLE, KEY_CONTACTID + "=" + contactID,
                null) > 0;
    }

    // fetch all contacts
    public Cursor fetchAllNotifications() {
        return database.query(DATABASE_TABLE, new String[]{KEY_CONTACTID,
                KEY_NOTIFICATION_DATE}, null, null, null, null, null);
    }

    //
    public boolean NotificationByDateExists(String notification_date) {
        Cursor mCursor = database.query(true, DATABASE_TABLE, new String[]{KEY_CONTACTID, KEY_NOTIFICATION_DATE}, KEY_NOTIFICATION_DATE + " = '" + notification_date + "'", null, null, null, null, null);
        return (mCursor.getCount() == 0 ? false : true);

    }

    // fetch contacts filter by a string
    public Cursor fetchNotificationByDate(String notification_date) {
        Cursor mCursor = database.query(true, DATABASE_TABLE, new String[]{
                KEY_CONTACTID, KEY_NOTIFICATION_DATE}, KEY_NOTIFICATION_DATE + " = '" + notification_date + "'", null, null, null, null, null);
        return mCursor;
    }
}
