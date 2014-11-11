package ru.ifmo.md.colloquium2;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import static ru.ifmo.md.colloquium2.DatabaseColumns.NAME;
import static ru.ifmo.md.colloquium2.DatabaseColumns.NUMBER;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, ListView.OnItemLongClickListener {
    static final String STATE_FIELD = "ru.ifmo.md.colloquium2.state";
    Menu menu;
    VotingState state = VotingState.BEFORE;
    private final SimpleCursorAdapter.ViewBinder binder = new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            switch (cursor.getColumnName(columnIndex)) {
                case NAME:
                    ((TextView) view).setText(cursor.getString(columnIndex));
                    break;
                case NUMBER:
                    if (state == VotingState.AFTER) {
                        String number = cursor.getString(columnIndex);
                        String result = number == null ? null : "Voted: " + number;
                        int percentage = cursor.getColumnIndex("Percentage");
                        if (cursor.getColumnIndex("Percentage") != -1 && cursor.getString(percentage) != null)
                            result += " (" + cursor.getString(percentage) + "%)";
                        ((TextView) view).setText(result);
                        view.setVisibility(View.VISIBLE);
                    } else
                        view.setVisibility(View.GONE);
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_FIELD, state);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, Bundle args) {
        return new SimpleCursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                return DataStorage.getCandidates(MainActivity.this, state == VotingState.AFTER, state != VotingState.BEFORE);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((CursorAdapter) getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.candidate_entry, null,
                new String[]{NAME, NUMBER}, new int[]{android.R.id.text1, android.R.id.text2}, 0) {
            {
                setViewBinder(binder);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                super.bindView(view, context, cursor);
                if (state == VotingState.AFTER && cursor.getPosition() == 0)
                    view.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                else
                    view.setBackground(null);
            }
        };
        setListAdapter(adapter);
        getLoaderManager().initLoader(0, null, this).forceLoad();
        getListView().setOnItemLongClickListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FIELD)) {
            state = (VotingState) savedInstanceState.getSerializable(STATE_FIELD);
            updateState();
        }
    }

    void updateState() {
        if (menu != null) {
            switch (state) {
                case BEFORE:
                    menu.findItem(R.id.action_new).setVisible(true);
                    menu.findItem(R.id.action_start).setVisible(true);
                    menu.findItem(R.id.action_finish).setVisible(false);
                    menu.findItem(R.id.action_reset).setVisible(true);
                    break;
                case VOTING:
                    menu.findItem(R.id.action_new).setVisible(false);
                    menu.findItem(R.id.action_start).setVisible(false);
                    menu.findItem(R.id.action_finish).setVisible(true);
                    menu.findItem(R.id.action_new).setVisible(true);
                    break;
                case AFTER:
                    menu.findItem(R.id.action_new).setVisible(false);
                    menu.findItem(R.id.action_start).setVisible(true);
                    menu.findItem(R.id.action_finish).setVisible(false);
                    menu.findItem(R.id.action_reset).setVisible(true);
                    break;
            }
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        updateState();
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (state == VotingState.VOTING) {
            DataStorage.vote(this, id);
            getLoaderManager().restartLoader(0, null, this).forceLoad();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (state == VotingState.BEFORE) {
            CandidatePreferences fragment = new CandidatePreferences();
            Bundle args = new Bundle(1);
            args.putString("candidateName", ((TextView) view.findViewById(android.R.id.text1)).getText().toString());
            fragment.setArguments(args);
            fragment.show(getFragmentManager(), "CandidatePreferences");
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_new:
                CandidatePreferences prefs = new CandidatePreferences();
                prefs.show(getFragmentManager(), "CandidatePreferences");
                break;
            case R.id.action_start:
                DataStorage.startVoting(this);
                state = VotingState.VOTING;
                getLoaderManager().restartLoader(0, null, this).forceLoad();
                break;
            case R.id.action_finish:
                state = VotingState.AFTER;
                getLoaderManager().restartLoader(0, null, this).forceLoad();
                break;
            case R.id.action_reset:
                state = VotingState.BEFORE;
                DataStorage.deleteAll(this);
                getLoaderManager().restartLoader(0, null, this).forceLoad();
                break;
        }
        updateState();

        return super.onOptionsItemSelected(item);
    }

    enum VotingState {BEFORE, VOTING, AFTER}
}
