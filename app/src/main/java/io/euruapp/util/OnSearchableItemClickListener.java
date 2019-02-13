package io.euruapp.util;

public interface OnSearchableItemClickListener<O> {
	void onItemClicked(int position, O object, boolean isLongClick);
}
