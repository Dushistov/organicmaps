package com.mapswithme.maps.ugc;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

public class UGC
{
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ RATING_NONE, RATING_HORRIBLE, RATING_BAD, RATING_NORMAL, RATING_GOOD,
            RATING_EXCELLENT, RATING_COMING_SOON })

  public @interface Impress
  {}

  public static final int RATING_NONE = 0;
  public static final int RATING_HORRIBLE = 1;
  public static final int RATING_BAD = 2;
  public static final int RATING_NORMAL = 3;
  public static final int RATING_GOOD = 4;
  public static final int RATING_EXCELLENT = 5;
  public static final int RATING_COMING_SOON = 6;

  @NonNull
  private final Rating[] mRatings;
  @Nullable
  private final Review[] mReviews;
  private final int mBasedOnCount;
  private final float mAverageRating;

  private UGC(@NonNull Rating[] ratings, float averageRating, @Nullable Review[] reviews,
              int basedOnCount)
  {
    mRatings = ratings;
    mReviews = reviews;
    mAverageRating = averageRating;
    mBasedOnCount = basedOnCount;
  }

  int getBasedOnCount()
  {
    return mBasedOnCount;
  }

  @NonNull
  List<Rating> getRatings()
  {
    return Arrays.asList(mRatings);
  }

  @Nullable
  public List<Review> getReviews()
  {
    if (mReviews == null)
      return null;

    return Arrays.asList(mReviews);
  }

  public static native int nativeToImpress(float rating);

  @NonNull
  public static native String nativeFormatRating(float rating);

  public static class Rating implements Parcelable
  {
    public static final Creator<Rating> CREATOR = new Creator<Rating>()
    {
      @Override
      public Rating createFromParcel(Parcel in)
      {
        return new Rating(in);
      }

      @Override
      public Rating[] newArray(int size)
      {
        return new Rating[size];
      }
    };

    @NonNull
    private final String mName;
    private float mValue;

    Rating(@NonNull String name, float value)
    {
      mName = name;
      mValue = value;
    }

    protected Rating(Parcel in)
    {
      mName = in.readString();
      mValue = in.readFloat();
    }

    public float getValue()
    {
      return mValue;
    }

    @NonNull
    public String getName()
    {
      return mName;
    }

    public void setValue(float value)
    {
      mValue = value;
    }

    @Override
    public int describeContents()
    {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
      dest.writeString(mName);
      dest.writeFloat(mValue);
    }
  }

  public static class Review implements Parcelable
  {
    public static final Creator<Review> CREATOR = new Creator<Review>()
    {
      @Override
      public Review createFromParcel(Parcel in)
      {
        return new Review(in);
      }

      @Override
      public Review[] newArray(int size)
      {
        return new Review[size];
      }
    };

    @NonNull
    private final String mText;
    @NonNull
    private final String mAuthor;
    private final long mTimeMillis;
    private final float mRating;
    @Impress
    private final int mImpress;

    private Review(@NonNull String text, @NonNull String author, long timeMillis,
                   float rating, @Impress int impress)
    {
      mText = text;
      mAuthor = author;
      mTimeMillis = timeMillis;
      mRating = rating;
      mImpress = impress;
    }

    protected Review(Parcel in)
    {
      mText = in.readString();
      mAuthor = in.readString();
      mTimeMillis = in.readLong();
      mRating = in.readFloat();
      //noinspection WrongConstant
      mImpress = in.readInt();
    }

    @NonNull
    public String getText()
    {
      return mText;
    }

    @NonNull
    String getAuthor()
    {
      return mAuthor;
    }

    long getTime()
    {
      return mTimeMillis;
    }

    public float getRating()
    {
      return mRating;
    }

    int getImpress()
    {
      return mImpress;
    }

    @Override
    public int describeContents()
    {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
      dest.writeString(mText);
      dest.writeString(mAuthor);
      dest.writeLong(mTimeMillis);
      dest.writeFloat(mRating);
      dest.writeInt(mImpress);
    }
  }
}
