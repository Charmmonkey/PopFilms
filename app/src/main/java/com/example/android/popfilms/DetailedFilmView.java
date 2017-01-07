package com.example.android.popfilms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popfilms.data.FilmContract;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by jerye on 12/14/2016.
 */

public class DetailedFilmView extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailedFilmView.class.getSimpleName();

    private Context mContext = DetailedFilmView.this;

    private static final int GENERAL_LOADER_ID = 0;
    private static final int REVIEW_LOADER_ID = 1;
    private static final int TRAILER_LOADER_ID = 2;


    private final int REVIEW_ID = 100;
    private final int TRAILER_ID = 101;

    public ArrayList<String[]> trailerData = new ArrayList<String[]>();

    private RecyclerView.LayoutManager mTrailerLayoutManager;
    private RecyclerView trailerRecyclerView;
    private RecyclerView.Adapter trailerClickableRecyclerAdapter;

    public ArrayList<String[]> reviewData = new ArrayList<String[]>();

    private RecyclerView.LayoutManager mReviewLayoutManager;
    private RecyclerView reviewRecyclerView;
    private RecyclerView.Adapter reviewClickableRecyclerAdapter;

    private int favoritesMarker = 0;

    private Uri filmDetailContentUri;
    private Uri filmFavoritesContentUri;
    private String detailedFilmId;
    private Uri filmReviewContentUri;
    private Uri filmTrailerContentUri;

    private ContentValues favoritesValues = new ContentValues();

    private String[] detailedFilmIdArray;

    private Bundle mSavedInstanceState;


    @Override
    protected void onStart() {
        super.onStart();
        ActionBar mActionBar = getSupportActionBar();

        Drawable drawable = ContextCompat.getDrawable(mContext,R.drawable.background_translucent_down);
        mActionBar.setBackgroundDrawable(drawable);
        mActionBar.setDisplayShowTitleEnabled(false);
        Log.v(LOG_TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
        // Fixes screen rotation crash
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "onCreate");

        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        mSavedInstanceState = savedInstanceState;

        filmDetailContentUri = getIntentData();
        detailedFilmId = FilmContract.FilmEntry.getMovieIdFromUri(filmDetailContentUri);
        Log.v(LOG_TAG, " detailedFilmId =: " + detailedFilmId);
        filmFavoritesContentUri = FilmContract.FilmEntry.buildFavoritesUriWithId(detailedFilmId);
        filmReviewContentUri = FilmContract.FilmEntry.buildReviewContentUriWithId(detailedFilmId);
        filmTrailerContentUri = FilmContract.FilmEntry.buildTrailerContentUriWithId(detailedFilmId);

        // Initiate loader for the 3 queries
        getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, savedInstanceState, this);
        getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, savedInstanceState, this);
        getSupportLoaderManager().initLoader(GENERAL_LOADER_ID, savedInstanceState, this);

        setContentView(R.layout.detailed_item);


        trailerRecyclerView = (RecyclerView) findViewById(R.id.trailer_recycler);
        trailerRecyclerView.setHasFixedSize(true);
        mTrailerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        trailerRecyclerView.setLayoutManager(mTrailerLayoutManager);
        Log.v(LOG_TAG, "LayoutManager trailer set");

        reviewRecyclerView = (RecyclerView) findViewById(R.id.review_recycler);
        reviewRecyclerView.setHasFixedSize(true);
        mReviewLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        reviewRecyclerView.setLayoutManager(mReviewLayoutManager);


    }


    // Implementing the Loader this way prevents duplicate records of the same film
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, " Loader created");

        switch (id) {
            case GENERAL_LOADER_ID:
                if (Utility.getSortingPreference(mContext).equals("favorites")) {
                    return new CursorLoader(DetailedFilmView.this, filmFavoritesContentUri, Utility.ENTRY_COLUMN, null, null, null);
                } else {
                    return new CursorLoader(DetailedFilmView.this, filmDetailContentUri, Utility.ENTRY_COLUMN, null, null, null);
                }
            case REVIEW_LOADER_ID:
                return new CursorLoader(DetailedFilmView.this, filmReviewContentUri, Utility.REVIEW_COLUMN, null, null, null);
            case TRAILER_LOADER_ID:
                return new CursorLoader(DetailedFilmView.this, filmTrailerContentUri, Utility.TRAILER_COLUMN, null, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.v(LOG_TAG, " Loader Reset");
    }

    // If query returns empty cursor, fetch the data, which triggers a requery then set.
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        switch (loader.getId()) {

            case GENERAL_LOADER_ID:
                if (cursor.moveToFirst()) {
                    String detailedTitle = cursor.getString(Utility.COL_ORIGINAL_TITLE_ID);
                    Log.v(LOG_TAG, detailedTitle);
                    String detailedOverview = cursor.getString(Utility.COL_OVERVIEW_ID);
                    String detailedReleaseDate = cursor.getString(Utility.COL_RELEASE_DATE_ID);
                    String detailedVoteAverage = cursor.getString(Utility.COL_VOTE_AVERAGE_ID);
                    String detailedPosterPath = cursor.getString(Utility.COL_POSTER_PATH_ID);
                    String detailedBackdropPath = cursor.getString(Utility.COL_BACKDROP_PATH_ID);
//                    String detailedFavorites = cursor.getString(Utility.COL_FAVORITES_ID);

                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_ORIGINAL_TITLE, detailedTitle);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_RELEASE_DATE, detailedReleaseDate);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_OVERVIEW, detailedOverview);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_VOTE_AVERAGE, detailedVoteAverage);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_POSTER_PATH, detailedPosterPath);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_BACKDROP_PATH, detailedBackdropPath);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_SPECIFIC_ID, detailedFilmId);
                    favoritesValues.put(FilmContract.FilmEntry.COLUMN_FAVORITE, "1");

                    // Find the views associated with id
                    TextView titleView = (TextView) findViewById(R.id.detailed_title);
                    TextView overviewView = (TextView) findViewById(R.id.detailed_overview);
//                    TextView releaseDateView = (TextView) findViewById(R.id.detailed_release_date);
                    TextView voteAverageView = (TextView) findViewById(R.id.detailed_vote_average);
                    ImageView backdropImageView = (ImageView) findViewById(R.id.detailed_backdrop_image);
                    final ImageView favoriteButton = (ImageView) findViewById(R.id.favorite_button);


                    // Set content to respective view
                    Picasso.with(DetailedFilmView.this).load(Utility.buildPosterUri(detailedBackdropPath)).into(backdropImageView);
                    titleView.setText(detailedTitle);
                    overviewView.setText(detailedOverview);
//                    releaseDateView.setText(detailedReleaseDate);
                    voteAverageView.setText(detailedVoteAverage);

                    final Toast mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);

                    String[] detailedFilmIdArrayTemp = {detailedFilmId};
                    detailedFilmIdArray = detailedFilmIdArrayTemp;

                    final Cursor favoritesCursor = mContext.getContentResolver().query(FilmContract.FilmEntry.FAVORITES_URI,
                            null,
                            FilmContract.FilmEntry.COLUMN_SPECIFIC_ID + " = ?",
                            detailedFilmIdArray,
                            null);

                    if (!favoritesCursor.moveToFirst()){
                        favoriteButton.setImageResource(R.drawable.ic_favorite_border_white_36dp);

                    }else{
                        favoriteButton.setImageResource(R.drawable.ic_favorite_white_36dp);

                    }


                    favoriteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!favoritesCursor.moveToFirst()) {



                                if (favoritesMarker == 1) {
                                    favoritesMarker = -1;

                                    favoriteButton.setImageResource(R.drawable.ic_favorite_border_white_36dp);

                                    mToast.setText("Unfavorited");
                                    mToast.show();

                                } else {
                                    favoritesMarker = 1;

                                    favoriteButton.setImageResource(R.drawable.ic_favorite_white_36dp);

                                    mToast.setText("Favorited");
                                    mToast.show();
                                }
                            } else {
                                if (favoritesMarker == -1) {
                                    favoritesMarker = 1;

                                    favoriteButton.setImageResource(R.drawable.ic_favorite_white_36dp);


                                    mToast.setText("Favorited");
                                    mToast.show();
                                } else {
                                    favoritesMarker = -1;

                                    favoriteButton.setImageResource(R.drawable.ic_favorite_border_white_36dp);


                                    mToast.setText("Unfavorited");
                                    mToast.show();
                                }

                            }

                        }
                    });

                }

                break;

            case REVIEW_LOADER_ID:
                if (cursor.moveToFirst()) {
                    do {
                        String[] dataSet = {cursor.getString(Utility.COL_REVIEW_AUTHOR_ID),
                                cursor.getString(Utility.COL_REVIEW_CONTENT_ID)};
                        reviewData.add(dataSet);
                    } while (cursor.moveToNext());

                    reviewClickableRecyclerAdapter = new RecyclerAdapter(reviewData, REVIEW_ID, mContext);
                    reviewRecyclerView.setAdapter(reviewClickableRecyclerAdapter);


                } else {
                    VolleyFetcher.volleyFetcher(Utility.buildFilmReviewUriWithId(detailedFilmId, mContext).toString(), Utility.REVIEW_COLUMN, mContext);
                }
                break;

            case TRAILER_LOADER_ID:
                if (cursor.moveToFirst()) {
                    Log.v(LOG_TAG, "Trailer info fetched cursor exists");
                    do {
                        String[] dataSet = {cursor.getString(Utility.COL_TRAILER_NAME_ID),
                                cursor.getString(Utility.COL_TRAILER_KEY_ID)};
                        trailerData.add(dataSet);
                    } while (cursor.moveToNext());

                    Log.v(LOG_TAG, "Ready to set adapter");
                    trailerClickableRecyclerAdapter = new RecyclerAdapter(trailerData, TRAILER_ID, mContext);
                    trailerRecyclerView.setAdapter(trailerClickableRecyclerAdapter);


                } else {
                    VolleyFetcher.volleyFetcher(Utility.buildFilmTrailerUriWithId(detailedFilmId, mContext).toString(), Utility.TRAILER_COLUMN, mContext);
                    Log.v(LOG_TAG, "Called volley for trailer");
                }
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");

        switch (favoritesMarker) {
            // Unfavorited
            case -1:
                mContext.getContentResolver().delete(FilmContract.FilmEntry.FAVORITES_URI,
                        FilmContract.FilmEntry.COLUMN_SPECIFIC_ID + " = ?",
                        detailedFilmIdArray);
                break;

            // No change
            case 0:
                break;

            // Favorited
            case 1:
                mContext.getContentResolver().delete(FilmContract.FilmEntry.FAVORITES_URI,
                        FilmContract.FilmEntry.COLUMN_SPECIFIC_ID + " = ?",
                        detailedFilmIdArray);

                mContext.getContentResolver().insert(FilmContract.FilmEntry.FAVORITES_URI,
                        favoritesValues);

                break;

        }
    }

    // Must retrieve intent data inside a override method or else will cause Nullpointerexception
    private Uri getIntentData() {
        return getIntent().getData();
    }


}
