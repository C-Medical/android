package iz.supereasycamera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import iz.supereasycamera.dto.MainDto;

/**
 * Created by izumi_j on 2014/11/28.
 */
public final class MainListAdapter extends ArrayAdapter<MainDto> {
    private final LayoutInflater layoutInflater;

    private final Set<Integer> checkedIndexes = new HashSet<Integer>();

    public MainListAdapter(Context context, int resource) {
        super(context, resource);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MainDto dto = (MainDto) getItem(position);

        // 行毎のレイアウトを設定する
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item, null);
        }

        // 中身に値を設定していく
        final TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
        txtName.setText(dto.name);
        final TextView txtCreatedAt = (TextView) convertView.findViewById(R.id.txtCreatedAt);
        txtCreatedAt.setText(Utils.formatDateTime(dto.createdAt));

        final CheckBox cbSel = (CheckBox) convertView.findViewById(R.id.cbSel);
        cbSel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedIndexes.add(position);
                } else {
                    checkedIndexes.remove(position);
                }
            }
        });

        cbSel.setChecked(checkedIndexes.contains(position));

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        // データの状態変更時に、選択状態をクリア
        checkedIndexes.clear();
        super.notifyDataSetChanged();
    }

    /**
     * 選択（チェック）されたデータのリストを返す。
     * @return selected dtos
     */
    public List<MainDto> getSelectedDtos() {
        final List<MainDto> list = new ArrayList<MainDto>();
        for (Integer pos : checkedIndexes) {
            list.add(getItem(pos));
        }
        return list;
    }
}
