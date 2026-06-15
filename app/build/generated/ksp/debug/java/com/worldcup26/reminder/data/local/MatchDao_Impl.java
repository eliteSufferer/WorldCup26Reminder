package com.worldcup26.reminder.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.EntityUpsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MatchDao_Impl implements MatchDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SelectionEntity> __insertionAdapterOfSelectionEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteSelection;

  private final EntityUpsertionAdapter<MatchEntity> __upsertionAdapterOfMatchEntity;

  public MatchDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSelectionEntity = new EntityInsertionAdapter<SelectionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `selections` (`matchId`,`reminderMinutesBefore`,`calendarEventId`,`alarmScheduled`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SelectionEntity entity) {
        statement.bindString(1, entity.getMatchId());
        statement.bindLong(2, entity.getReminderMinutesBefore());
        if (entity.getCalendarEventId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getCalendarEventId());
        }
        final int _tmp = entity.getAlarmScheduled() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__preparedStmtOfDeleteSelection = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM selections WHERE matchId = ?";
        return _query;
      }
    };
    this.__upsertionAdapterOfMatchEntity = new EntityUpsertionAdapter<MatchEntity>(new EntityInsertionAdapter<MatchEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT INTO `matches` (`id`,`kickoffEpochMillis`,`team1`,`team2`,`group`,`round`,`ground`,`scoreFt1`,`scoreFt2`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MatchEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getKickoffEpochMillis());
        statement.bindString(3, entity.getTeam1());
        statement.bindString(4, entity.getTeam2());
        if (entity.getGroup() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getGroup());
        }
        if (entity.getRound() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRound());
        }
        if (entity.getGround() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getGround());
        }
        if (entity.getScoreFt1() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getScoreFt1());
        }
        if (entity.getScoreFt2() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getScoreFt2());
        }
      }
    }, new EntityDeletionOrUpdateAdapter<MatchEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE `matches` SET `id` = ?,`kickoffEpochMillis` = ?,`team1` = ?,`team2` = ?,`group` = ?,`round` = ?,`ground` = ?,`scoreFt1` = ?,`scoreFt2` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MatchEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getKickoffEpochMillis());
        statement.bindString(3, entity.getTeam1());
        statement.bindString(4, entity.getTeam2());
        if (entity.getGroup() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getGroup());
        }
        if (entity.getRound() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRound());
        }
        if (entity.getGround() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getGround());
        }
        if (entity.getScoreFt1() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getScoreFt1());
        }
        if (entity.getScoreFt2() == null) {
          statement.bindNull(9);
        } else {
          statement.bindLong(9, entity.getScoreFt2());
        }
        statement.bindString(10, entity.getId());
      }
    });
  }

  @Override
  public Object upsertSelection(final SelectionEntity selection,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSelectionEntity.insert(selection);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSelection(final String matchId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteSelection.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, matchId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteSelection.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertMatches(final List<MatchEntity> matches,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __upsertionAdapterOfMatchEntity.upsert(matches);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MatchWithSelection>> observeMatches() {
    final String _sql = "\n"
            + "        SELECT m.*, s.reminderMinutesBefore AS reminderMinutesBefore,\n"
            + "               s.calendarEventId AS calendarEventId\n"
            + "        FROM matches m\n"
            + "        LEFT JOIN selections s ON s.matchId = m.id\n"
            + "        ORDER BY m.kickoffEpochMillis ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"matches",
        "selections"}, new Callable<List<MatchWithSelection>>() {
      @Override
      @NonNull
      public List<MatchWithSelection> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKickoffEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "kickoffEpochMillis");
          final int _cursorIndexOfTeam1 = CursorUtil.getColumnIndexOrThrow(_cursor, "team1");
          final int _cursorIndexOfTeam2 = CursorUtil.getColumnIndexOrThrow(_cursor, "team2");
          final int _cursorIndexOfGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "group");
          final int _cursorIndexOfRound = CursorUtil.getColumnIndexOrThrow(_cursor, "round");
          final int _cursorIndexOfGround = CursorUtil.getColumnIndexOrThrow(_cursor, "ground");
          final int _cursorIndexOfScoreFt1 = CursorUtil.getColumnIndexOrThrow(_cursor, "scoreFt1");
          final int _cursorIndexOfScoreFt2 = CursorUtil.getColumnIndexOrThrow(_cursor, "scoreFt2");
          final int _cursorIndexOfReminderMinutesBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutesBefore");
          final int _cursorIndexOfCalendarEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarEventId");
          final List<MatchWithSelection> _result = new ArrayList<MatchWithSelection>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MatchWithSelection _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpKickoffEpochMillis;
            _tmpKickoffEpochMillis = _cursor.getLong(_cursorIndexOfKickoffEpochMillis);
            final String _tmpTeam1;
            _tmpTeam1 = _cursor.getString(_cursorIndexOfTeam1);
            final String _tmpTeam2;
            _tmpTeam2 = _cursor.getString(_cursorIndexOfTeam2);
            final String _tmpGroup;
            if (_cursor.isNull(_cursorIndexOfGroup)) {
              _tmpGroup = null;
            } else {
              _tmpGroup = _cursor.getString(_cursorIndexOfGroup);
            }
            final String _tmpRound;
            if (_cursor.isNull(_cursorIndexOfRound)) {
              _tmpRound = null;
            } else {
              _tmpRound = _cursor.getString(_cursorIndexOfRound);
            }
            final String _tmpGround;
            if (_cursor.isNull(_cursorIndexOfGround)) {
              _tmpGround = null;
            } else {
              _tmpGround = _cursor.getString(_cursorIndexOfGround);
            }
            final Integer _tmpScoreFt1;
            if (_cursor.isNull(_cursorIndexOfScoreFt1)) {
              _tmpScoreFt1 = null;
            } else {
              _tmpScoreFt1 = _cursor.getInt(_cursorIndexOfScoreFt1);
            }
            final Integer _tmpScoreFt2;
            if (_cursor.isNull(_cursorIndexOfScoreFt2)) {
              _tmpScoreFt2 = null;
            } else {
              _tmpScoreFt2 = _cursor.getInt(_cursorIndexOfScoreFt2);
            }
            final Integer _tmpReminderMinutesBefore;
            if (_cursor.isNull(_cursorIndexOfReminderMinutesBefore)) {
              _tmpReminderMinutesBefore = null;
            } else {
              _tmpReminderMinutesBefore = _cursor.getInt(_cursorIndexOfReminderMinutesBefore);
            }
            final Long _tmpCalendarEventId;
            if (_cursor.isNull(_cursorIndexOfCalendarEventId)) {
              _tmpCalendarEventId = null;
            } else {
              _tmpCalendarEventId = _cursor.getLong(_cursorIndexOfCalendarEventId);
            }
            _item = new MatchWithSelection(_tmpId,_tmpKickoffEpochMillis,_tmpTeam1,_tmpTeam2,_tmpGroup,_tmpRound,_tmpGround,_tmpScoreFt1,_tmpScoreFt2,_tmpReminderMinutesBefore,_tmpCalendarEventId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getMatch(final String id, final Continuation<? super MatchEntity> $completion) {
    final String _sql = "SELECT * FROM matches WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MatchEntity>() {
      @Override
      @Nullable
      public MatchEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfKickoffEpochMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "kickoffEpochMillis");
          final int _cursorIndexOfTeam1 = CursorUtil.getColumnIndexOrThrow(_cursor, "team1");
          final int _cursorIndexOfTeam2 = CursorUtil.getColumnIndexOrThrow(_cursor, "team2");
          final int _cursorIndexOfGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "group");
          final int _cursorIndexOfRound = CursorUtil.getColumnIndexOrThrow(_cursor, "round");
          final int _cursorIndexOfGround = CursorUtil.getColumnIndexOrThrow(_cursor, "ground");
          final int _cursorIndexOfScoreFt1 = CursorUtil.getColumnIndexOrThrow(_cursor, "scoreFt1");
          final int _cursorIndexOfScoreFt2 = CursorUtil.getColumnIndexOrThrow(_cursor, "scoreFt2");
          final MatchEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpKickoffEpochMillis;
            _tmpKickoffEpochMillis = _cursor.getLong(_cursorIndexOfKickoffEpochMillis);
            final String _tmpTeam1;
            _tmpTeam1 = _cursor.getString(_cursorIndexOfTeam1);
            final String _tmpTeam2;
            _tmpTeam2 = _cursor.getString(_cursorIndexOfTeam2);
            final String _tmpGroup;
            if (_cursor.isNull(_cursorIndexOfGroup)) {
              _tmpGroup = null;
            } else {
              _tmpGroup = _cursor.getString(_cursorIndexOfGroup);
            }
            final String _tmpRound;
            if (_cursor.isNull(_cursorIndexOfRound)) {
              _tmpRound = null;
            } else {
              _tmpRound = _cursor.getString(_cursorIndexOfRound);
            }
            final String _tmpGround;
            if (_cursor.isNull(_cursorIndexOfGround)) {
              _tmpGround = null;
            } else {
              _tmpGround = _cursor.getString(_cursorIndexOfGround);
            }
            final Integer _tmpScoreFt1;
            if (_cursor.isNull(_cursorIndexOfScoreFt1)) {
              _tmpScoreFt1 = null;
            } else {
              _tmpScoreFt1 = _cursor.getInt(_cursorIndexOfScoreFt1);
            }
            final Integer _tmpScoreFt2;
            if (_cursor.isNull(_cursorIndexOfScoreFt2)) {
              _tmpScoreFt2 = null;
            } else {
              _tmpScoreFt2 = _cursor.getInt(_cursorIndexOfScoreFt2);
            }
            _result = new MatchEntity(_tmpId,_tmpKickoffEpochMillis,_tmpTeam1,_tmpTeam2,_tmpGroup,_tmpRound,_tmpGround,_tmpScoreFt1,_tmpScoreFt2);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSelection(final String matchId,
      final Continuation<? super SelectionEntity> $completion) {
    final String _sql = "SELECT * FROM selections WHERE matchId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, matchId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SelectionEntity>() {
      @Override
      @Nullable
      public SelectionEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "matchId");
          final int _cursorIndexOfReminderMinutesBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutesBefore");
          final int _cursorIndexOfCalendarEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarEventId");
          final int _cursorIndexOfAlarmScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmScheduled");
          final SelectionEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpMatchId;
            _tmpMatchId = _cursor.getString(_cursorIndexOfMatchId);
            final int _tmpReminderMinutesBefore;
            _tmpReminderMinutesBefore = _cursor.getInt(_cursorIndexOfReminderMinutesBefore);
            final Long _tmpCalendarEventId;
            if (_cursor.isNull(_cursorIndexOfCalendarEventId)) {
              _tmpCalendarEventId = null;
            } else {
              _tmpCalendarEventId = _cursor.getLong(_cursorIndexOfCalendarEventId);
            }
            final boolean _tmpAlarmScheduled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAlarmScheduled);
            _tmpAlarmScheduled = _tmp != 0;
            _result = new SelectionEntity(_tmpMatchId,_tmpReminderMinutesBefore,_tmpCalendarEventId,_tmpAlarmScheduled);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllSelections(final Continuation<? super List<SelectionEntity>> $completion) {
    final String _sql = "SELECT * FROM selections";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SelectionEntity>>() {
      @Override
      @NonNull
      public List<SelectionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfMatchId = CursorUtil.getColumnIndexOrThrow(_cursor, "matchId");
          final int _cursorIndexOfReminderMinutesBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderMinutesBefore");
          final int _cursorIndexOfCalendarEventId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarEventId");
          final int _cursorIndexOfAlarmScheduled = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmScheduled");
          final List<SelectionEntity> _result = new ArrayList<SelectionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SelectionEntity _item;
            final String _tmpMatchId;
            _tmpMatchId = _cursor.getString(_cursorIndexOfMatchId);
            final int _tmpReminderMinutesBefore;
            _tmpReminderMinutesBefore = _cursor.getInt(_cursorIndexOfReminderMinutesBefore);
            final Long _tmpCalendarEventId;
            if (_cursor.isNull(_cursorIndexOfCalendarEventId)) {
              _tmpCalendarEventId = null;
            } else {
              _tmpCalendarEventId = _cursor.getLong(_cursorIndexOfCalendarEventId);
            }
            final boolean _tmpAlarmScheduled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfAlarmScheduled);
            _tmpAlarmScheduled = _tmp != 0;
            _item = new SelectionEntity(_tmpMatchId,_tmpReminderMinutesBefore,_tmpCalendarEventId,_tmpAlarmScheduled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
