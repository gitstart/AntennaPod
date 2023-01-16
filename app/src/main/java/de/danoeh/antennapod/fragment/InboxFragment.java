package de.danoeh.antennapod.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.model.feed.FeedItem;
import de.danoeh.antennapod.model.feed.FeedItemFilter;
import de.danoeh.antennapod.model.feed.SortOrder;

import java.util.List;

/**
 * Like 'EpisodesFragment' except that it only shows new episodes and
 * supports swiping to mark as read.
 */
public class InboxFragment extends EpisodesListFragment {
    public static final String TAG = "NewEpisodesFragment";
    private static final String PREF_NAME = "PrefNewEpisodesFragment";
    private static final String PREF_DO_NOT_PROMPT_REMOVE_ALL_FROM_INBOX = "prefDoNotPromptRemovalAllFromInbox";
    public static final String PREF_INBOX_SORT_ORDER = "prefInboxSortOrder";
    private SharedPreferences prefs;

    private SortOrder sortType = SortOrder.DATE_NEW_OLD;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);
        toolbar.inflateMenu(R.menu.inbox);
        toolbar.inflateMenu(R.menu.inbox_sort);
        sortType = getSortOrder();

        toolbar.setTitle(R.string.inbox_label);
        updateToolbar();
        emptyView.setIcon(R.drawable.ic_inbox);
        emptyView.setTitle(R.string.no_inbox_head_label);
        emptyView.setMessage(R.string.no_inbox_label);
        speedDialView.removeActionItemById(R.id.mark_unread_batch);
        speedDialView.removeActionItemById(R.id.remove_from_queue_batch);
        speedDialView.removeActionItemById(R.id.delete_batch);
        return root;
    }

    @Override
    protected FeedItemFilter getFilter() {
        return new FeedItemFilter(FeedItemFilter.NEW);
    }

    @Override
    protected String getFragmentTag() {
        return TAG;
    }

    @Override
    protected String getPrefName() {
        return PREF_NAME;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (super.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.remove_all_inbox_item) {
            if (prefs.getBoolean(PREF_DO_NOT_PROMPT_REMOVE_ALL_FROM_INBOX, false)) {
                removeAllFromInbox();
            } else {
                showRemoveAllDialog();
            }
            return true;
        } else if (item.getItemId() == R.id.inbox_sort_date_desc) {
            saveSortOrderAndRefresh(SortOrder.DATE_NEW_OLD);
            return true;
        } else if (item.getItemId() == R.id.inbox_sort_date_asc) {
            saveSortOrderAndRefresh(SortOrder.DATE_OLD_NEW);
            return true;
        }
        return false;
    }

    private void saveSortOrderAndRefresh(SortOrder type) {
        sortType = type;
        prefs.edit().putString(PREF_INBOX_SORT_ORDER, sortType.name()).apply();
        loadItems();
    }

    private SortOrder getSortOrder() {
        String sortOrderStr = prefs.getString(PREF_INBOX_SORT_ORDER, "use-default");
        return SortOrder.parseWithDefault(sortOrderStr, SortOrder.DATE_NEW_OLD);
    }

    @NonNull
    @Override
    protected List<FeedItem> loadData() {
        return DBReader.getNewItemsList(0, page * EPISODES_PER_PAGE, sortType);
    }

    @NonNull
    @Override
    protected List<FeedItem> loadMoreData(int page) {
        return DBReader.getNewItemsList((page - 1) * EPISODES_PER_PAGE, EPISODES_PER_PAGE, sortType);
    }

    @Override
    protected int loadTotalItemCount() {
        return DBReader.getTotalEpisodeCount(new FeedItemFilter(FeedItemFilter.NEW));
    }

    private void removeAllFromInbox() {
        DBWriter.removeAllNewFlags();
        ((MainActivity) getActivity()).showSnackbarAbovePlayer(R.string.removed_all_inbox_msg, Toast.LENGTH_SHORT);
    }

    private void showRemoveAllDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(R.string.remove_all_inbox_label);
        builder.setMessage(R.string.remove_all_inbox_confirmation_msg);

        View view = View.inflate(getContext(), R.layout.checkbox_do_not_show_again, null);
        CheckBox checkNeverAskAgain = view.findViewById(R.id.checkbox_do_not_show_again);
        builder.setView(view);

        builder.setPositiveButton(R.string.confirm_label, (dialog, which) -> {
            dialog.dismiss();
            removeAllFromInbox();
            prefs.edit().putBoolean(PREF_DO_NOT_PROMPT_REMOVE_ALL_FROM_INBOX, checkNeverAskAgain.isChecked()).apply();
        });
        builder.setNegativeButton(R.string.cancel_label, null);
        builder.show();
    }
}
