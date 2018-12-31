package nl.asymmetrics.droidshows.ui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import nl.asymmetrics.droidshows.DroidShows;
import nl.asymmetrics.droidshows.R;
import nl.asymmetrics.droidshows.utils.SQLiteStore;
import nl.asymmetrics.droidshows.utils.SQLiteStore.EpisodeRow;
import nl.asymmetrics.droidshows.utils.SwipeDetect;
import android.app.DatePickerDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;

import java.text.ParseException;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

public class SerieEpisodes extends ListActivity {
	private EpisodesAdapter episodesAdapter;
	private String serieName;
	private String serieId;
	private int seasonNumber;
	private List<EpisodeRow> episodes = null;
	private ListView listView;
	private SwipeDetect swipeDetect = new SwipeDetect();
	private SimpleDateFormat sdfseen = new SimpleDateFormat("yyyyMMdd");
	private SQLiteStore db;
	private DatePickerDialog dateDialog;
	private int backFromEpisode = -1;

	/* Context Menus */
	private static final int VIEWEP_CONTEXT = Menu.FIRST;
	private static final int SEENDATE_CONTEXT = Menu.FIRST + 1;
	private static final int DELEP_CONTEXT = SEENDATE_CONTEXT + 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.overridePendingTransition(R.anim.right_enter, R.anim.right_exit);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.serie_episodes);
		db = SQLiteStore.getInstance(this);
		serieId = getIntent().getStringExtra("serieId");
		serieName = db.getSerieName(serieId);
		seasonNumber = getIntent().getIntExtra("seasonNumber", 0);
		setTitle(serieName +" - "+ (seasonNumber == 0 ? getString(R.string.messages_specials) : getString(R.string.messages_season) +" "+ seasonNumber));
		episodes = db.getEpisodeRows(serieId, seasonNumber);
		episodesAdapter = new EpisodesAdapter(this, R.layout.row_serie_episodes, episodes);
		setListAdapter(episodesAdapter);
		listView = getListView();
		listView.setOnTouchListener(swipeDetect);
		registerForContextMenu(getListView());
		if (getIntent().getBooleanExtra("nextEpisode", false))
			listView.setSelection(db.getNextEpisode(serieId, seasonNumber).episode -3);
	}

	/* context menu */
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, VIEWEP_CONTEXT, 0, getString(R.string.messsages_view_ep_details));
		menu.add(0, SEENDATE_CONTEXT, 0, getString(R.string.messsages_edit_seen_date));
		menu.add(0, DELEP_CONTEXT, 0, getString(R.string.menu_context_delete));
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case VIEWEP_CONTEXT:
			startViewEpisode(info.position);
			return true;
		case SEENDATE_CONTEXT:
			int year = 0, month = 0, day = 0;
			int seen = episodes.get(info.position).seen;
			if (seen > 1) {
				year = seen / 10000;
				month = seen % 10000 / 100 -1;
				day = seen % 100;
			} else {
				Calendar cal = Calendar.getInstance();
				year = cal.get(Calendar.YEAR);
				month = cal.get(Calendar.MONTH);
				day = cal.get(Calendar.DAY_OF_MONTH);
			}

			final int position = info.position;
			dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {			
				public void onDateSet(DatePicker view, int year, int month, int day) {
					check(position, 10000 * year + 100 * (month +1) + day);
				}
			}, year, month, day);
			dateDialog.show();
			return true;
		case DELEP_CONTEXT:
			db.deleteEpisode(serieId, episodes.get(info.position).id);
			episodes.remove(info.position);
			episodesAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	public void openContext(View v) {
		this.openContextMenu(v);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (swipeDetect.value != 0) return;
		if (DroidShows.fullLineCheckOption) {
			try {
				CheckBox c = (CheckBox) v.findViewById(R.id.seen);
				c.setChecked(!c.isChecked());
				check(position, v, -1);
			} catch (Exception e) {
				Log.e(SQLiteStore.TAG, "Could not set episode seen state: "+ e.getMessage());
			}
		} else {
			try {
				startViewEpisode(position);
			} catch (Exception e) {
				Log.e(SQLiteStore.TAG, e.getMessage());
			}
		}
	}

	public class EpisodesAdapter extends ArrayAdapter<EpisodeRow> {

		private List<EpisodeRow> items;
		private LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		private ColorStateList textViewColors = new TextView(getContext()).getTextColors();

		private final String strAired = getString(R.string.messages_aired);
		private final String strEp = (getString(R.string.messages_ep).isEmpty() ? "" : getString(R.string.messages_ep) +" ");
		
		public EpisodesAdapter(Context context, int textViewResourceId, List<EpisodeRow> episodes) {
			super(context, textViewResourceId, episodes);
			this.items = episodes;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			if (convertView == null) {
				convertView = vi.inflate(R.layout.row_serie_episodes, parent, false);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.aired = (TextView) convertView.findViewById(R.id.aired);
				holder.seenDate = (TextView) convertView.findViewById(R.id.seendate);
				holder.seen = (CheckBox) convertView.findViewById(R.id.seen);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			EpisodeRow ep = items.get(position);

			if (holder.name != null) {
				String name = strEp + ep.name;
				holder.name.setText(name);
			}
			
			if (holder.aired != null) {
				if (!ep.aired.isEmpty())
					holder.aired.setText(strAired + " "+ ep.aired);
				else
					holder.aired.setText("");
				holder.aired.setEnabled(ep.airedDate != null &&
						ep.airedDate.compareTo(Calendar.getInstance().getTime()) <= 0);
			}
			
			holder.seen.setChecked(ep.seen > 0);
			if (ep.seen > 1)	// If seen value is a date
				try {
					holder.seenDate.setTextColor(textViewColors);
					holder.seenDate.setText(SimpleDateFormat.getDateInstance().format(sdfseen.parse(ep.seen +"")));
				} catch (ParseException e) { Log.e(SQLiteStore.TAG, e.getMessage()); }
			else
				holder.seenDate.setText("");

			return convertView;
		}
	}

	static class ViewHolder {
		TextView name, aired, seenDate;
		CheckBox seen;
	}

	private View getViewByPosition(int position) {
		try {
			final int firstListItemPosition = listView.getFirstVisiblePosition();
			final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
			if (position < firstListItemPosition || position > lastListItemPosition)
				return listView.getAdapter().getView(position, null, listView); 
			else					
				return listView.getChildAt(position - firstListItemPosition);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void check(View v) {
		int position = listView.getPositionForView(v);
		check(position, v, -1);
	}
	
	private void check(int position, int seen) {
		check(position, getViewByPosition(position), seen);
	}
	
	private void check(int position, View v, int seen) {
		if (v != null) {
			CheckBox c = (CheckBox) v.findViewById(R.id.seen);
			TextView d = (TextView) getViewByPosition(position).findViewById(R.id.seendate);
			if (seen > -1)
				c.setChecked(true);
			if (c.isChecked()) {
				d.setTextColor(getResources().getColor(android.R.color.white));
				if (seen == -1) {
					Calendar cal = Calendar.getInstance();
					seen = 10000 * cal.get(Calendar.YEAR) + 100 * (cal.get(Calendar.MONTH) +1) + cal.get(Calendar.DAY_OF_MONTH);
				}
				episodes.get(position).seen = seen;
				try { d.setText(SimpleDateFormat.getDateInstance().format(sdfseen.parse(seen +"")));
				} catch (ParseException e) { e.printStackTrace(); }
			} else {
				d.setText("");
				episodes.get(position).seen = 0;
			}
		}
		db.updateUnwatchedEpisode(serieId, episodes.get(position).id, seen);
	}

	private void startViewEpisode(int position) {
		backFromEpisode = position;
		Intent viewEpisode = new Intent(SerieEpisodes.this, ViewEpisode.class);
		viewEpisode.putExtra("serieId", serieId);
		viewEpisode.putExtra("serieName", serieName);
		viewEpisode.putExtra("episodeId", episodes.get(position).id);
		startActivity(viewEpisode);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.left_enter, R.anim.left_exit);
	}

	@Override
	public void onRestart() {
		super.onRestart();
		if (backFromEpisode != -1) {
			episodes.set(backFromEpisode, db.getEpisodeRow(serieId, seasonNumber, episodes.get(backFromEpisode).id));
			episodesAdapter.notifyDataSetChanged();
			backFromEpisode = -1;
		}
	}
}