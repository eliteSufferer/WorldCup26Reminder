package com.worldcup26.reminder.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MatchDao _matchDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `matches` (`id` TEXT NOT NULL, `kickoffEpochMillis` INTEGER NOT NULL, `team1` TEXT NOT NULL, `team2` TEXT NOT NULL, `group` TEXT, `round` TEXT, `ground` TEXT, `scoreFt1` INTEGER, `scoreFt2` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `selections` (`matchId` TEXT NOT NULL, `reminderMinutesBefore` INTEGER NOT NULL, `calendarEventId` INTEGER, `alarmScheduled` INTEGER NOT NULL, PRIMARY KEY(`matchId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9d23428ab06a66deaa1e2c662eb83894')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `matches`");
        db.execSQL("DROP TABLE IF EXISTS `selections`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMatches = new HashMap<String, TableInfo.Column>(9);
        _columnsMatches.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("kickoffEpochMillis", new TableInfo.Column("kickoffEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("team1", new TableInfo.Column("team1", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("team2", new TableInfo.Column("team2", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("group", new TableInfo.Column("group", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("round", new TableInfo.Column("round", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("ground", new TableInfo.Column("ground", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("scoreFt1", new TableInfo.Column("scoreFt1", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatches.put("scoreFt2", new TableInfo.Column("scoreFt2", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMatches = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMatches = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMatches = new TableInfo("matches", _columnsMatches, _foreignKeysMatches, _indicesMatches);
        final TableInfo _existingMatches = TableInfo.read(db, "matches");
        if (!_infoMatches.equals(_existingMatches)) {
          return new RoomOpenHelper.ValidationResult(false, "matches(com.worldcup26.reminder.data.local.MatchEntity).\n"
                  + " Expected:\n" + _infoMatches + "\n"
                  + " Found:\n" + _existingMatches);
        }
        final HashMap<String, TableInfo.Column> _columnsSelections = new HashMap<String, TableInfo.Column>(4);
        _columnsSelections.put("matchId", new TableInfo.Column("matchId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSelections.put("reminderMinutesBefore", new TableInfo.Column("reminderMinutesBefore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSelections.put("calendarEventId", new TableInfo.Column("calendarEventId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSelections.put("alarmScheduled", new TableInfo.Column("alarmScheduled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSelections = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSelections = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSelections = new TableInfo("selections", _columnsSelections, _foreignKeysSelections, _indicesSelections);
        final TableInfo _existingSelections = TableInfo.read(db, "selections");
        if (!_infoSelections.equals(_existingSelections)) {
          return new RoomOpenHelper.ValidationResult(false, "selections(com.worldcup26.reminder.data.local.SelectionEntity).\n"
                  + " Expected:\n" + _infoSelections + "\n"
                  + " Found:\n" + _existingSelections);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9d23428ab06a66deaa1e2c662eb83894", "c4e410664289332d6c0693c5a4dff15a");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "matches","selections");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `matches`");
      _db.execSQL("DELETE FROM `selections`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MatchDao.class, MatchDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MatchDao matchDao() {
    if (_matchDao != null) {
      return _matchDao;
    } else {
      synchronized(this) {
        if(_matchDao == null) {
          _matchDao = new MatchDao_Impl(this);
        }
        return _matchDao;
      }
    }
  }
}
