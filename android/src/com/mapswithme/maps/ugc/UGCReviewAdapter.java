package com.mapswithme.maps.ugc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.mapswithme.maps.R;
import com.mapswithme.maps.widget.RatingView;
import com.mapswithme.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class UGCReviewAdapter extends Adapter<UGCReviewAdapter.ViewHolder>
{
  static final int MAX_COUNT = 3;
  @NonNull
  private final ArrayList<UGC.Review> mItems = new ArrayList<>();

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
  {
    return new ViewHolder(LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.item_ugc_comment, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position)
  {
    holder.bind(mItems.get(position));
  }

  @Override
  public int getItemCount()
  {
    return Math.min(mItems.size(), MAX_COUNT);
  }

  public void setItems(@NonNull List<UGC.Review> items)
  {
    this.mItems.clear();
    this.mItems.addAll(items);
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder
  {
    @NonNull
    final TextView mAuthor;
    @NonNull
    final TextView mCommentDate;
    @NonNull
    final TextView mReview;
    @NonNull
    final RatingView mRating;

    public ViewHolder(View itemView)
    {
      super(itemView);
      mAuthor = itemView.findViewById(R.id.name);
      mCommentDate = itemView.findViewById(R.id.date);
      mReview = itemView.findViewById(R.id.review);
      mRating = itemView.findViewById(R.id.rating);
      // TODO: remove "gone" visibility when review rating behaviour is fixed on the server.
      mRating.setVisibility(View.GONE);
    }

    public void bind(UGC.Review review)
    {
      mAuthor.setText(review.getAuthor());
      mCommentDate.setText(DateUtils.getMediumDateFormatter().format(new Date(review.getTime())));
      mReview.setText(review.getText());
      mRating.setRating(Impress.values()[review.getImpress()], String.valueOf(review.getRating()));
    }
  }
}
