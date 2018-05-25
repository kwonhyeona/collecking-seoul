package kr.ac.sungshin.colleckingseoul.mypage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import kr.ac.sungshin.colleckingseoul.R;
import kr.ac.sungshin.colleckingseoul.model.response.User;
import kr.ac.sungshin.colleckingseoul.model.singleton.InfoManager;
import kr.ac.sungshin.colleckingseoul.network.ApplicationController;

import static android.content.Context.MODE_PRIVATE;

public class LogoutFragmentDialog extends DialogFragment {
    @BindView(R.id.logoutfragmentdialog_button_cancle)
    Button buttonCancle;
    @BindView(R.id.logoutfragmentdialog_button_logout)
    Button buttonLogout;

    public LogoutFragmentDialog() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout_fragment_dialog, container, false);
        ButterKnife.bind(this, view);
        Bundle args = getArguments();
        String value = args.getString("key");

        bindClickListener();

        return view;
    }

    public void bindClickListener() {

        buttonCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag("logout");
                if (fragment != null) {
                    DialogFragment dialogFragment = (DialogFragment) fragment;
                    dialogFragment.dismiss();
                }

            }
        });
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //정보 지우기
                deleteInfo();
                //login 화면으로

                // 스택에 있는거 비우기

            }
        });
    }

    public void goLogin() {

    }

    public void deleteInfo() {
        SharedPreferences.Editor prefs = getActivity().getSharedPreferences("user", MODE_PRIVATE).edit();
        prefs.remove("user");
        prefs.commit();
    }

}
