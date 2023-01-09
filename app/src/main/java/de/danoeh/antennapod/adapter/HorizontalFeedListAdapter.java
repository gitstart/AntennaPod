package de.danoeh.antennapod.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.model.feed.Feed;
import de.danoeh.antennapod.fragment.FeedItemlistFragment;
import de.danoeh.antennapod.ui.common.SquareImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.view.ContextMenu;
import android.view.MenuInflater;
import androidx.annotation.Nullable;

public class HorizontalFeedListAdapter extends SelectableAdapter<HorizontalFeedListAdapter.Holder>
        implements View.OnCreateContextMenuListener  {
    private final WeakReference<MainActivity> mainActivityRef;
    private final List<Feed> data = new ArrayList<>();
    private int dummyViews = 0;
    private Feed longPressedItem;


    public HorizontalFeedListAdapter(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivityRef = new WeakReference<>(mainActivity);
        setHasStableIds(true);

    }

    public void setDummyViews(int dummyViews) {
        this.dummyViews = dummyViews;
    }

    public void updateData(List<Feed> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View convertView = View.inflate(mainActivityRef.get(), R.layout.horizontal_feed_item, null);
        return new Holder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (position >= data.size()) {
            holder.itemView.setAlpha(0.1f);
            Glide.with(mainActivityRef.get()).clear(holder.imageView);
            holder.imageView.setImageResource(R.color.medium_gray);
            return;
        }

        holder.itemView.setAlpha(1.0f);
        final Feed podcast = data.get(position);
        holder.imageView.setContentDescription(podcast.getTitle());
        holder.imageView.setOnClickListener(v ->
                mainActivityRef.get().loadChildFragment(FeedItemlistFragment.newInstance(podcast.getId())));

        Glide.with(mainActivityRef.get())
                .load(podcast.getImageUrl())
                .apply(new RequestOptions()
                        .placeholder(R.color.light_gray)
                        .fitCenter()
                        .dontAnimate())
                .into(holder.imageView);

        holder.imageView.setOnCreateContextMenuListener(this);
        holder.imageView.setOnLongClickListener(v -> {
            int currentItemPosition = holder.getBindingAdapterPosition();
            longPressedItem = data.get(currentItemPosition);
            return false;
        });
    }

    @Nullable
    public Feed getLongPressedItem() {
        return longPressedItem;
    }

    @Override
    public long getItemId(int position) {
        if (position >= data.size()) {
            return RecyclerView.NO_ID; // Dummy views
        }
        return data.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return dummyViews + data.size();
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        MenuInflater inflater = mainActivityRef.get().getMenuInflater();
        if (longPressedItem == null) {
            return;
        }
        inflater.inflate(R.menu.nav_feed_context, contextMenu);
        contextMenu.setHeaderTitle(longPressedItem.getTitle());
    }


    static class Holder extends RecyclerView.ViewHolder {
        SquareImageView imageView;

        public Holder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.discovery_cover);
            imageView.setDirection(SquareImageView.DIRECTION_HEIGHT);
        }
    }
}
