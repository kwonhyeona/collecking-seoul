package kr.ac.sungshin.colleckingseoul.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import kr.ac.sungshin.colleckingseoul.R;
import kr.ac.sungshin.colleckingseoul.model.response.BaseResult;
import kr.ac.sungshin.colleckingseoul.model.response.VerificationCodeResult;
import kr.ac.sungshin.colleckingseoul.mypage.LogoutFragmentDialog;
import kr.ac.sungshin.colleckingseoul.network.ApplicationController;
import kr.ac.sungshin.colleckingseoul.network.NetworkService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinActivity extends AppCompatActivity {
    @BindView(R.id.join_linearlayout_container)
    LinearLayout containerLayout;
    @BindView(R.id.join_edittext_id)
    EditText editTextId;
    @BindView(R.id.join_button_duplication)
    Button buttonDuplication;
    @BindView(R.id.join_button_requestcode)
    Button buttonRequestCode;
    @BindView(R.id.join_edittext_checkcode)
    EditText editTextcheckCode;
    @BindView(R.id.join_button_checkcode)
    Button buttonCheckCode;
    @BindView(R.id.join_edittext_password)
    EditText editTextPassword;
    @BindView(R.id.join_edittext_repassword)
    EditText editTextRepassword;
    @BindView(R.id.join_edittext_nikname)
    EditText editTextNikname;
    @BindView(R.id.join_edittext_phone)
    EditText editTextPhone;
    @BindView(R.id.join_datepicker_birth)
    DatePicker Datepickerbirth;
    @BindView(R.id.join_button_join)
    Button buttonJoin;
    @BindView(R.id.join_button_profile)
    Button buttonProfile;
    @BindView(R.id.join_image_profile)
    CircleImageView imageProfile;
    @BindView(R.id.join_radioGroup_sex)
    RadioGroup radioGroupSex;
    @BindView(R.id.join_progressbar_progressbar)
    ProgressBar progressBar;

    private NetworkService service;
    private final String TAG = "JoinActivity";

    private boolean isDuplicate = false;
    private boolean isCheckEmail = false;
    private String verificationCode = "";

    private static final int REQ_CODE_SELECT_IMAGE = 100;
    String imgUrl = "";

    Uri data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        service = ApplicationController.getInstance().getNetworkService();
        ButterKnife.bind(this);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Drawable wrapDrawable = DrawableCompat.wrap(progressBar.getIndeterminateDrawable());
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(getBaseContext(), R.color.colorAccent));
            progressBar.setIndeterminateDrawable(DrawableCompat.unwrap(wrapDrawable));
        } else {
            progressBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getBaseContext(), android.R.color.holo_green_light), PorterDuff.Mode.SRC_IN);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayOptions(actionBar.getDisplayOptions() | ActionBar.DISPLAY_SHOW_CUSTOM);
        ImageView imageView = new ImageView(actionBar.getThemedContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(R.drawable.joinus);
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL
                | Gravity.CENTER_VERTICAL);
        layoutParams.rightMargin = 40;
        imageView.setLayoutParams(layoutParams);
        actionBar.setCustomView(imageView);

        bindClickListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //클릭 이벤트 바인딩
    public void bindClickListener() {
        Log.d(TAG, "bindClickListener");
        //갤러리에서 프로필 사진 가져오기
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });

        //email 중복 체크
        buttonDuplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = editTextId.getText().toString();
                if (id.equals("") || !id.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$")) {
                    Toast.makeText(getApplicationContext(), "이메일 형식이 맞지 않습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Call<BaseResult> checkId = service.getDuplicatedResult(id);
                    checkId.enqueue(new Callback<BaseResult>() {
                        @Override
                        public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                            if (response.isSuccessful()) {
                                if (response.body().getMessage().equals("SUCCESS")) {
                                    Toast.makeText(getApplicationContext(), "사용가능한 이메일 입니다.", Toast.LENGTH_SHORT).show();
                                    isDuplicate = true;
                                }
                                if (response.body().getMessage().equals("ALREADY_JOIN")) {
                                    Toast.makeText(getApplicationContext(), "이미 사용중인 이메일이 존재합니다. 다른 이메일로 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                    isDuplicate = false;
                                }
                                if (response.body().getMessage().equals("NOT_MATCH_REGULATION")) {
                                    Toast.makeText(getApplicationContext(), "정규식이 일치 하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    isDuplicate = false;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<BaseResult> call, Throwable t) {

                        }
                    });
                }
            }
        });

        //이메일 인증번호 요청
        buttonRequestCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = editTextId.getText().toString();
                if (!isDuplicate) {
                    Toast.makeText(getApplicationContext(), "이메일 중복 체크를 해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    final Call<VerificationCodeResult> checkNumber = service.getVerifiCodeResult(id);
                    checkNumber.enqueue(new Callback<VerificationCodeResult>() {
                        @Override
                        public void onResponse(Call<VerificationCodeResult> call, Response<VerificationCodeResult> response) {
                            if (response.isSuccessful()) {
                                if (response.body().getMessage().equals("ALREADY_JOIN")) {
                                    Toast.makeText(getApplicationContext(), "이미 사용중인 이메일이 존재합니다. 다른 이메일로 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                }
                                if (response.body().getMessage().equals("failuire")) {
                                    Toast.makeText(getApplicationContext(), "알수 없는 오류 입니다.", Toast.LENGTH_SHORT).show();
                                }
                                if (response.body().getMessage().equals("SUCCESS")) {
                                    verificationCode = response.body().getVerificationCode();
                                    Toast.makeText(getApplicationContext(), "인증번호가 메일로 발송되었습니다. 확인후 입력해 주세요.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<VerificationCodeResult> call, Throwable t) {

                        }
                    });
                }
            }
        });

        //인증번호 확인
        buttonCheckCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code = editTextcheckCode.getText().toString();
                if (code.equals(verificationCode)) {
                    isCheckEmail = true;
                    Toast.makeText(getApplicationContext(), "인증 되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //회원가입 완료
        buttonJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int typeId = radioGroupSex.getCheckedRadioButtonId();

                RadioButton radiobuttonSex = (RadioButton) findViewById(typeId);
                String type = radiobuttonSex.getText().toString();
                int intType = 1;
                if (type.equals("남자")) intType = 0;
                else if (type.equals("여자")) intType = 1;
                String birthYear = Integer.toString(Datepickerbirth.getYear());
                String birthMonth;
                String birthDate;

                if (Datepickerbirth.getMonth() < 9)
                    birthMonth = "0" + Integer.toString(Datepickerbirth.getMonth() + 1);
                else if (Datepickerbirth.getMonth() == 9) birthMonth = "10";
                else birthMonth = Integer.toString(Datepickerbirth.getMonth() + 1);
                if (Datepickerbirth.getDayOfMonth() < 10)
                    birthDate = "0" + Integer.toString(Datepickerbirth.getDayOfMonth());
                else birthDate = Integer.toString(Datepickerbirth.getDayOfMonth());

                if (!checkValid(editTextId.getText().toString(), editTextPassword.getText().toString(), editTextRepassword.getText().toString(), editTextPhone.getText().toString(), editTextNikname.getText().toString())) {
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                RequestBody id = RequestBody.create(MediaType.parse("multipart/form-data"), editTextId.getText().toString().trim());
                RequestBody password1 = RequestBody.create(MediaType.parse("multipart/form-data"), editTextPassword.getText().toString());
                RequestBody password2 = RequestBody.create(MediaType.parse("multipart/form-data"), editTextRepassword.getText().toString());
                RequestBody nickname = RequestBody.create(MediaType.parse("multipart/form-data"), editTextNikname.getText().toString().trim());
                RequestBody phone = RequestBody.create(MediaType.parse("multipart/form-data"), editTextPhone.getText().toString().trim());
                RequestBody birth = RequestBody.create(MediaType.parse("multipart/form-data"), birthYear + "-" + birthMonth + "-" + birthDate);
                RequestBody sex = RequestBody.create(MediaType.parse("multipart/form-data"), String.valueOf(intType));

                MultipartBody.Part body;
                Bitmap bitmap;
                File file;
                if (imgUrl.equals("")) {
                    String phototmp = "/tmp.jpg";
                    file = new File(phototmp);
                    Drawable drawable = imageProfile.getDrawable();
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                } else {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    InputStream in = null;
                    try {
                        in = getContentResolver().openInputStream(data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap = BitmapFactory.decodeStream(in, null, options);
                    file = new File(imgUrl);
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                RequestBody photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray());
                body = MultipartBody.Part.createFormData("photo", file.getName(), photoBody);


                Call<BaseResult> getJoinResult = service.getJoinResult(id, password1, password2, phone, nickname, birth, sex, body);

                getJoinResult.enqueue(new Callback<BaseResult>() {
                    @Override
                    public void onResponse(Call<BaseResult> call, Response<BaseResult> response) {
                        if (response.isSuccessful()) {
                            String message = response.body().getMessage();
                            switch (message) {
                                case "SUCCESS":
                                    Toast.makeText(getApplicationContext(), "회원가입이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "ALREADY_JOIN":
                                    Toast.makeText(getApplicationContext(), "이미 가입된 유저 입니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                case "EMPTY_VALUE":
                                    Toast.makeText(getApplicationContext(), "파라미터 값이 비어있습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                case "NULL_VALUE":
                                    Toast.makeText(getApplicationContext(), "받아야 할 파라미터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                case "NOT_CORRESPOND":
                                    Toast.makeText(getApplicationContext(), "두 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                case "NOT_MATCH_REGULATION":
                                    Toast.makeText(getApplicationContext(), "입력 형식이 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                case "NO_IMAGE":
                                    Toast.makeText(getApplicationContext(), "이미지가 없습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResult> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "죄송합니다. 서버에 오류가 발생하였습니다. 빠른 시일내에 개선하겠습니다", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
                Log.d(TAG, "통신 끝");
            }
        });

        containerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editTextId.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextcheckCode.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextNikname.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextPhone.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextRepassword.getWindowToken(), 0);
            }
        });
    }
    //유효성 체크

    public boolean checkValid(String id, String password, String repassword, String phone, String name) {
        // 빈칸 체크
        if (id.equals("")) {
            Toast.makeText(getBaseContext(), "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.equals("") || repassword.equals("")) {
            Toast.makeText(getBaseContext(), "패스워드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (name.equals("")) {
            Toast.makeText(getBaseContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (phone.equals("") || !phone.matches("^[0-9]{11}+$")) {
            Toast.makeText(getBaseContext(), "전화번호를 올바르게 입력해주세요. - 없이 번호만 입력해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        //비밀번호 일치 체크
        if (!password.equals(repassword)) {
            Toast.makeText(getBaseContext(), "비밀번호와 비밀번호확인이 일치하지 않습니다. 다시 시도해주세요", Toast.LENGTH_SHORT).show();
            return false;
        }
        //이메일 유효성 체크
        if (!id.matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$")) {
            Toast.makeText(getBaseContext(), "이메일 형식이 맞지 않습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        //비밀번호 체크
        if (!password.matches("^.*(?=.{6,20})(?=.*[0-9])(?=.*[a-zA-Z]).*$")) {
            Toast.makeText(getBaseContext(), "비밀번호는 8자리이상 영문 숫자 조합입니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isDuplicate) {
            Toast.makeText(getBaseContext(), "이메일 중복 체크를 하지 않으셨습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isCheckEmail) {
            Toast.makeText(getBaseContext(), "이메일 인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // 선택된 이미지 데이터 받아오기
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            //이미지를 성공적으로 가져왔을 경우
            if (resultCode == Activity.RESULT_OK) {
                try {
                    Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    imageProfile.setImageBitmap(image_bitmap);
                    getImageNameToUri(data.getData());
                    this.data = data.getData();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 선택된 이미지 파일명 가져오기 나중에 코드를 재활용 해서 사용하 면 된다
    public String getImageNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);
        imgUrl = imgPath;

        return imgName;
    }
}