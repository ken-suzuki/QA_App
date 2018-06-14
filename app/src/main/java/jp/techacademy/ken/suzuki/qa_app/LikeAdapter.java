package jp.techacademy.ken.suzuki.qa_app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class LikeAdapter extends BaseAdapter {
    private List<String> mLikeList;

    public void setLikeList(List<String> likeList) {
        mLikeList = likeList;
    }

    @Override
    public int getCount() {
        return mLikeList.size();
    }

    @Override
    public Object getItem(int position) {
        return mLikeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
