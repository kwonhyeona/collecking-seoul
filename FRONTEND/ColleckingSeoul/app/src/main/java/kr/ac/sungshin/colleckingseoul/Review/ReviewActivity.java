package kr.ac.sungshin.colleckingseoul.Review;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.ac.sungshin.colleckingseoul.R;
import kr.ac.sungshin.colleckingseoul.model.response.BoardResult;
import kr.ac.sungshin.colleckingseoul.network.ApplicationController;
import kr.ac.sungshin.colleckingseoul.network.NetworkService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    @BindView(R.id.review_textview_title)
    TextView textViewTitle;
    @BindView(R.id.review_textview_nickname)
    TextView textViewNickname;
    @BindView(R.id.review_textview_date)
    TextView textViewdate;
    @BindView(R.id.review_textview_content)
    TextView textViewContent;
    @BindView(R.id.review_imageview_photo)
    ImageView imageViewPhoto;
    @BindView(R.id.review_ratingbar_ratingbar)
    RatingBar ratingBarRating;
    @BindView(R.id.review_button_modify)
    Button buttonModify;

    private NetworkService service;
    private final String TAG = "ReviewActivity";
    private final int REQUEST_CODE_EDIT = 10000;
    private int idx;
    private String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView actionBarimageView = new ImageView(actionBar.getThemedContext());
        actionBarimageView.setScaleType(ImageView.ScaleType.CENTER);
        actionBarimageView.setImageResource(R.drawable.main_title_cloeckingseoul);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL
                | Gravity.CENTER_VERTICAL);
        layoutParams.rightMargin = 40;
        actionBarimageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(actionBarimageView);

        service = ApplicationController.getInstance().getNetworkService();
        ButterKnife.bind(this);
        buttonModify.setVisibility(View.GONE);

        Intent intent = getIntent();
        idx = intent.getIntExtra("idx", 0);

        loadReviewInfo();
        bindClickListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadReviewInfo() {
        Call<BoardResult> boardResult = service.getBoardResult(idx);
        boardResult.enqueue(new Callback<BoardResult>() {
            @Override
            public void onResponse(Call<BoardResult> call, Response<BoardResult> response) {
                if (response.isSuccessful()) {
                    if (response.body().getMessage().equals("SUCCESS")) {
                        textViewTitle.setText(response.body().getBoard().getTitle());
                        textViewNickname.setText(response.body().getBoard().getNickname());
                        textViewdate.setText(response.body().getBoard().getDate().substring(0, 10));
                        Log.d(TAG, String.valueOf(response.body().getBoard().getGrade()));
                        ratingBarRating.setRating(response.body().getBoard().getGrade());
                        textViewContent.setText(response.body().getBoard().getContent());
                        Log.d(TAG, response.body().getBoard().getUrl());
                        url = response.body().getBoard().getUrl();

                        if (!url.equals("")) {
                            Glide.with(getApplicationContext())
                                    .load(url)
                                    .into(imageViewPhoto);
                        }
                        SharedPreferences userInfo = getSharedPreferences("user", MODE_PRIVATE);
                        String userIdx = userInfo.getString("idx", "0");
                        if (String.valueOf(response.body().getBoard().getuIdx()).equals(userIdx)) {
                            buttonModify.setVisibility(View.VISIBLE);
                        }
                    }
                    if (response.body().getMessage().equals("NULL_VALUE")) {
                        Toast.makeText(getApplicationContext(), "값을 입력해 주세요.", Toast.LENGTH_SHORT).show();

                    }
                    if (response.body().getMessage().equals("NOT_LOGIN")) {
                        Toast.makeText(getApplicationContext(), "로그인 하지 않은 사용자입니다.", Toast.LENGTH_SHORT).show();

                    }
                    if (response.body().getMessage().equals("NO_DATA")) {
                        Toast.makeText(getApplicationContext(), "요청하신 리뷰는 존재하지 않습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onFailure(Call<BoardResult> call, Throwable t) {
            }
        });
    }

    private void bindClickListener() {
        buttonModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), RegisterReviewActivity.class);
                intent.putExtra("idx", idx + "");
                intent.putExtra("title", textViewTitle.getText().toString());
                intent.putExtra("content", textViewContent.getText().toString());
                intent.putExtra("grade", ratingBarRating.getRating());
                intent.putExtra("photo", url);
                intent.putExtra("purpose", "edit");

                startActivityForResult(intent, REQUEST_CODE_EDIT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            loadReviewInfo();
        }
    }
}
