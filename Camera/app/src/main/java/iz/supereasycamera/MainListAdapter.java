package iz.supereasycamera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import iz.supereasycamera.dto.MainDto;
import iz.supereasycamera.service.ContentsService;
import iz.supereasycamera.utils.Misc;
import iz.supereasycamera.utils.PictureUtils;

/**
 * Created by izumi_j on 2014/11/28.
 */
public final class MainListAdapter extends ArrayAdapter<MainDto> {
    private final ContentsService contentsService = new ContentsService();

    private final LayoutInflater layoutInflater;

    private final Set<Integer> checkedIndexes = new HashSet<Integer>();

    public MainListAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        // データの状態変更時に、選択状態をクリア
        checkedIndexes.clear();
        super.notifyDataSetChanged();
    }

    /**
     * 選択（チェック）されたデータのリストを返す。
     *
     * @return selected dtos
     */
    public ArrayList<MainDto> getSelectedDtos() {
        final ArrayList<MainDto> list = new ArrayList<MainDto>();
        for (Integer pos : checkedIndexes) {
            list.add(getItem(pos));
        }
        return list;
    }

    private class ViewHolder {
        private final ImageView imgIndex;
        private final TextView txtName;
        private final TextView txtCreatedAt;
        private final TextView txtSize;
        private final CheckBox cbSel;

        private ViewHolder(View view) {
            imgIndex = (ImageView) view.findViewById(R.id.imgIndex);
            txtName = (TextView) view.findViewById(R.id.txtName);
            txtCreatedAt = (TextView) view.findViewById(R.id.txtCreatedAt);
            txtSize = (TextView) view.findViewById(R.id.txtSize);
            cbSel = (CheckBox) view.findViewById(R.id.cbSel);
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MainDto dto = (MainDto) getItem(position);

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 中身に値を設定していく
        if (dto.dirOrPic == MainDto.DirOrPic.DIR) {
            viewHolder.imgIndex.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_dir));
        } else {
            loadPicture(getContext(), dto.id, viewHolder.imgIndex);
        }

        viewHolder.txtName.setText(dto.name);
        viewHolder.txtCreatedAt.setText(Misc.formatDateTime(dto.createdAt));
        if (dto.dirOrPic == MainDto.DirOrPic.PIC) {
            viewHolder.txtSize.setText(Misc.toKB(dto.size));
        } else {
            viewHolder.txtSize.setText("");
        }

        viewHolder.cbSel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedIndexes.add(position);
                } else {
                    checkedIndexes.remove(position);
                }
            }
        });

        viewHolder.cbSel.setChecked(checkedIndexes.contains(position));

        return convertView;
    }

    /**
     * 写真の読み込みを実行。
     *
     * @param context
     * @param id
     * @param imageView
     */
    private void loadPicture(Context context, long id, ImageView imageView) {
        // 同じタスクが走っていないか、同じ ImageView で古いタスクが走っていないかチェック
        if (!canCreateNewTask(id, imageView)) {
            return;
        }

        final BitmapWorkerTask task = new BitmapWorkerTask(imageView, id);
        final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), task);
        imageView.setImageDrawable(asyncDrawable);
        task.execute();
    }

    /**
     * 新しいタスクを実行して良いかチェック。
     *
     * @param id
     * @param imageView
     * @return true if can create new task
     */
    private boolean canCreateNewTask(long id, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTaskOf(imageView);

        if (bitmapWorkerTask != null) {
            if (bitmapWorkerTask.id != id) {
                // 以前のタスクをキャンセル
                Misc.debug("Cancel task of " + id);
                bitmapWorkerTask.cancel(true);
            } else {
                // 同じタスクがすでに走っているので、このタスクは実行しない
                Misc.debug("Same task is working.");
                return false;
            }
        }
        // この ImageView に関連する新しいタスクを実行してもOK
        return true;
    }

    /**
     * 引数のImageViewを処理しているタスクを取得。
     *
     * @param imageView
     * @return task
     */
    private static BitmapWorkerTask getBitmapWorkerTaskOf(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * 非同期タスクと連携するためのDrawable
     */
    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        private AsyncDrawable(Resources res, BitmapWorkerTask bitmapWorkerTask) {
            super(res, ((BitmapDrawable)res.getDrawable(R.drawable.ic_pic)).getBitmap());
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        private BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * 非同期に画像をdecodeするタスク
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final ContentsService contentsService = new ContentsService();
        private final WeakReference<ImageView> imageViewRef;
        private final long id;

        private BitmapWorkerTask(ImageView imageView, long id) {
            this.imageViewRef = new WeakReference<ImageView>(imageView);
            this.id = id;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Misc.debug("Start to create Bitmap of " + id);
            final byte[] picture = contentsService.getPicture(getContext(), id);
            final Bitmap ret = PictureUtils.toIndexBitmap(picture);
            if (ret == null) {
                Misc.warn("Picture is null! id = " + id);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Misc.debug("Finish task for " + id);

            if (isCancelled()) {
                Misc.debug("Task has been canceled.");
                bitmap = null;
                return;
            }

            if (imageViewRef == null || bitmap == null) {
                Misc.debug("Image is null for " + id);
                return;
            }

            final ImageView imageView = imageViewRef.get();
            if (imageView != null) {
                final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTaskOf(imageView);
                // ImageViewに結びつくタスクが自分自身だったらOK（同じ参照だったらOK）
                if (this == bitmapWorkerTask) {
                    Misc.debug("Set Bitmap of " + id);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}
