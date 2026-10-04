package com.mrzero.videoplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_MEDIA = 101;

    private RecyclerView recyclerView;
    private View emptyView;
    private TextView emptyText;
    private View permissionView;
    private View searchBlock;
    private EditText searchInput;
    private TextView headerText;
    private View headerIcon;
    private ImageButton backButton;
    private String query = "";
    private final List<FolderItem> allFolders = new ArrayList<>();
    private final List<MediaItem> visibleItems = new ArrayList<>();
    private FolderItem currentFolder;
    private final VideoAdapter adapter = new VideoAdapter(this::openItem, this::showItemOptions);
    private final FolderAdapter folderAdapter = new FolderAdapter(this::openFolder);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler);
        emptyView = findViewById(R.id.empty);
        emptyText = findViewById(R.id.empty_text);
        permissionView = findViewById(R.id.permission);
        permissionView.findViewById(R.id.btn_grant).setOnClickListener(v -> requestMediaPermission());

        searchBlock = findViewById(R.id.search_block);
        searchInput = findViewById(R.id.search_input);
        headerText = findViewById(R.id.header);
        headerIcon = findViewById(R.id.header_icon);
        backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> goBackToFolders());
        findViewById(R.id.btn_search).setOnClickListener(v -> {
            if (searchBlock.getVisibility() == View.VISIBLE) {
                closeSearch();
            } else {
                openSearch();
            }
        });
        findViewById(R.id.search_cancel).setOnClickListener(v -> closeSearch());
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s == null ? "" : s.toString();
                applyFilter();
            }
        });

        showFolderGrid(new ArrayList<>());

        if (hasMediaPermission()) {
            loadVideos();
        } else {
            permissionView.setVisibility(View.VISIBLE);
            requestMediaPermission();
        }
    }

    private void openSearch() {
        searchBlock.setVisibility(View.VISIBLE);
        searchInput.requestFocus();
        searchInput.setSelection(searchInput.getText().length());
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSearch() {
        searchBlock.setVisibility(View.GONE);
        searchInput.setText("");
        query = "";
        applyFilter();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
    }

    private boolean hasMediaPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMediaPermission() {
        String perm = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_VIDEO
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        ActivityCompat.requestPermissions(this, new String[]{perm}, REQ_MEDIA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionView.setVisibility(View.GONE);
                loadVideos();
            } else {
                permissionView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadVideos() {
        executor.execute(() -> {
            List<MediaItem> items = queryVideos();
            List<FolderItem> folders = buildFolders(items);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                allFolders.clear();
                allFolders.addAll(folders);
                if (currentFolder != null) {
                    FolderItem match = null;
                    for (FolderItem f : allFolders) {
                        if (f.path.equals(currentFolder.path)) {
                            match = f;
                            break;
                        }
                    }
                    currentFolder = match;
                }
                applyFilter();
            });
        });
    }

    private void applyFilter() {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (currentFolder == null) {
            List<FolderItem> filtered = new ArrayList<>();
            for (FolderItem folder : allFolders) {
                if (q.isEmpty() || folder.name.toLowerCase(Locale.ROOT).contains(q)) {
                    filtered.add(folder);
                }
            }
            if (recyclerView.getAdapter() != folderAdapter) showFolderGrid(filtered);
            else folderAdapter.submit(filtered);
            showEmpty(filtered.isEmpty(), q);
        } else {
            List<MediaItem> filtered = new ArrayList<>();
            for (MediaItem item : currentFolder.videos) {
                if (q.isEmpty() || (item.title != null
                        && item.title.toLowerCase(Locale.ROOT).contains(q))) {
                    filtered.add(item);
                }
            }
            if (recyclerView.getAdapter() != adapter) showVideoList(filtered);
            else adapter.submit(filtered);
            visibleItems.clear();
            visibleItems.addAll(filtered);
            showEmpty(filtered.isEmpty(), q);
        }
    }

    private void showEmpty(boolean empty, String q) {
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (emptyText != null) {
            emptyText.setText(q.isEmpty() ? R.string.no_media : R.string.no_results);
        }
    }

    private void showFolderGrid(List<FolderItem> folders) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(folderAdapter);
        folderAdapter.submit(folders);
    }

    private void showVideoList(List<MediaItem> items) {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(adapter);
        adapter.submit(items);
    }

    private void openFolder(FolderItem folder) {
        currentFolder = folder;
        updateHeader();
        query = "";
        searchInput.setText("");
        applyFilter();
        recyclerView.scrollToPosition(0);
    }

    private void goBackToFolders() {
        if (currentFolder == null) return;
        currentFolder = null;
        updateHeader();
        query = "";
        searchInput.setText("");
        applyFilter();
        recyclerView.scrollToPosition(0);
    }

    private void updateHeader() {
        boolean inFolder = currentFolder != null;
        headerIcon.setVisibility(inFolder ? View.GONE : View.VISIBLE);
        backButton.setVisibility(inFolder ? View.VISIBLE : View.GONE);
        if (inFolder) {
            headerText.setText(currentFolder.name);
        } else {
            headerText.setText(R.string.app_name);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFolder != null) {
            goBackToFolders();
            return;
        }
        super.onBackPressed();
    }

    private List<FolderItem> buildFolders(List<MediaItem> items) {
        LinkedHashMap<String, FolderItem> map = new LinkedHashMap<>();
        for (MediaItem item : items) {
            String dir = parentOf(item.path);
            FolderItem folder = map.get(dir);
            if (folder == null) {
                folder = new FolderItem(dir, folderName(dir));
                map.put(dir, folder);
            }
            folder.videos.add(item);
        }
        return new ArrayList<>(map.values());
    }

    private static String parentOf(String path) {
        if (path == null) return "";
        int idx = path.lastIndexOf('/');
        return idx <= 0 ? "" : path.substring(0, idx);
    }

    private static String folderName(String dir) {
        if (dir == null || dir.isEmpty()) return "/";
        int idx = dir.lastIndexOf('/');
        String name = idx >= 0 ? dir.substring(idx + 1) : dir;
        return name.isEmpty() ? dir : name;
    }

    private List<MediaItem> queryVideos() {
        List<MediaItem> result = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Uri collection = Build.VERSION.SDK_INT >= 29
                ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED
        };
        Cursor cursor = null;
        try {
            // Berkas TypeScript (*.ts, *.tsx, *.mts, *.cts) ikut terindeks MediaStore
            // sebagai video, padahal bukan video.
            String nameCol = MediaStore.Video.Media.DISPLAY_NAME;
            String selection = nameCol + " NOT LIKE '%.ts'"
                    + " AND " + nameCol + " NOT LIKE '%.tsx'"
                    + " AND " + nameCol + " NOT LIKE '%.mts'"
                    + " AND " + nameCol + " NOT LIKE '%.cts'";
            cursor = cr.query(collection, projection, selection, null,
                    MediaStore.Video.Media.DATE_ADDED + " DESC");
            if (cursor == null) return result;
            int idIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int durIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIdx);
                String name = cursor.getString(nameIdx);
                String path = cursor.getString(dataIdx);
                long duration = cursor.getLong(durIdx);
                long size = cursor.getLong(sizeIdx);
                long date = cursor.getLong(dateIdx);
                Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                result.add(new MediaItem(id, name, path, uri, duration, size, date));
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    private void openItem(MediaItem item) {
        int index = visibleItems.indexOf(item);
        if (index < 0) index = 0;
        PlayerActivity.open(this, visibleItems, index);
    }

    /** Tahan (long press) pada video: tampilkan opsi hapus. */
    private void showItemOptions(MediaItem item) {
        new AlertDialog.Builder(this)
                .setTitle(item.title)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.delete, (d, w) -> deleteMedia(item))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteMedia(MediaItem item) {
        executor.execute(() -> {
            boolean ok = deleteFile(item);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (ok) {
                    removeItemLocally(item);
                    Toast.makeText(this, R.string.delete_ok, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.delete_fail, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /** Buang item dari daftar folder yang terlihat lalu render ulang. */
    private void removeItemLocally(MediaItem item) {
        for (FolderItem folder : allFolders) {
            folder.videos.remove(item);
        }
        for (int i = allFolders.size() - 1; i >= 0; i--) {
            if (allFolders.get(i).videos.isEmpty()) {
                allFolders.remove(i);
            }
        }
        visibleItems.remove(item);
        if (currentFolder != null && currentFolder.videos.isEmpty()) {
            currentFolder = null;
            updateHeader();
        }
        applyFilter();
    }

    /** Coba hapus: MediaStore -> File biasa -> root (perangkat uji). */
    private boolean deleteFile(MediaItem item) {
        try {
            if (getContentResolver().delete(item.uri, null, null) > 0) return true;
        } catch (Throwable ignored) {
        }
        File file = item.path == null ? null : new File(item.path);
        try {
            if (file != null && file.exists() && file.delete()) return true;
        } catch (Throwable ignored) {
        }
        if (file != null && file.exists()) {
            return suDelete(item.path);
        }
        return false;
    }

    private static boolean suDelete(String path) {
        try {
            String quoted = "'" + path.replace("'", "'\\''") + "'";
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "rm -f -- " + quoted});
            p.waitFor();
            return !new File(path).exists();
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    static class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.Holder> {

        interface OnClick {
            void onClick(MediaItem item);
        }

        interface OnLongClick {
            void onLongClick(MediaItem item);
        }

        private final List<MediaItem> data = new ArrayList<>();
        private final OnClick click;
        private final OnLongClick longClick;
        private final ExecutorService thumbExecutor = Executors.newFixedThreadPool(2);

        VideoAdapter(OnClick click, OnLongClick longClick) {
            this.click = click;
            this.longClick = longClick;
        }

        void submit(List<MediaItem> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            MediaItem item = data.get(position);
            holder.title.setText(item.title);
            holder.meta.setText(formatDuration(item.duration));
            holder.thumb.setImageResource(R.drawable.ic_placeholder);
            holder.thumb.setTag(item.id);
            thumbExecutor.execute(() -> {
                Bitmap bmp = loadThumb(holder.itemView.getContext(), item);
                if (bmp != null && holder.thumb.getTag() != null
                        && holder.thumb.getTag().equals(item.id)) {
                    holder.thumb.post(() -> holder.thumb.setImageBitmap(bmp));
                }
            });
            holder.itemView.setOnClickListener(v -> click.onClick(item));
            holder.itemView.setOnLongClickListener(v -> {
                if (longClick != null) longClick.onLongClick(item);
                return longClick != null;
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final ImageView thumb;
            final TextView title;
            final TextView meta;

            Holder(@NonNull View itemView) {
                super(itemView);
                thumb = itemView.findViewById(R.id.thumb);
                title = itemView.findViewById(R.id.title);
                meta = itemView.findViewById(R.id.meta);
            }
        }
    }

    static class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.Holder> {

        interface OnClick {
            void onClick(FolderItem folder);
        }

        private final List<FolderItem> data = new ArrayList<>();
        private final OnClick click;
        private final ExecutorService thumbExecutor = Executors.newFixedThreadPool(2);

        FolderAdapter(OnClick click) {
            this.click = click;
        }

        void submit(List<FolderItem> folders) {
            data.clear();
            data.addAll(folders);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
            return new Holder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            FolderItem folder = data.get(position);
            holder.title.setText(folder.name);
            int count = folder.videos.size();
            holder.meta.setText(holder.itemView.getContext().getResources()
                    .getQuantityString(R.plurals.video_count, count, count));
            MediaItem cover = folder.newest();
            holder.thumb.setImageResource(R.drawable.ic_placeholder);
            holder.thumb.setTag(cover == null ? folder.path : cover.id);
            if (cover != null) {
                thumbExecutor.execute(() -> {
                    Bitmap bmp = loadThumb(holder.itemView.getContext(), cover);
                    if (bmp != null && holder.thumb.getTag() != null
                            && holder.thumb.getTag().equals(cover.id)) {
                        holder.thumb.post(() -> holder.thumb.setImageBitmap(bmp));
                    }
                });
            }
            holder.itemView.setOnClickListener(v -> click.onClick(folder));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final ImageView thumb;
            final TextView title;
            final TextView meta;

            Holder(@NonNull View itemView) {
                super(itemView);
                thumb = itemView.findViewById(R.id.thumb);
                title = itemView.findViewById(R.id.title);
                meta = itemView.findViewById(R.id.meta);
            }
        }
    }

    static Bitmap loadThumb(Context ctx, MediaItem item) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            if (item.path != null) {
                mmr.setDataSource(item.path);
            } else {
                mmr.setDataSource(ctx, item.uri);
            }
            Bitmap frame = mmr.getFrameAtTime(-1);
            mmr.release();
            return frame;
        } catch (Throwable t) {
            return null;
        }
    }

    static String formatDuration(long ms) {
        if (ms <= 0) return "00:00";
        long totalSec = ms / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasMediaPermission()) loadVideos();
    }
}
