package com.ericksilva.cuacuapp.activities.onbording;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.ericksilva.cuacuapp.R;
import com.ericksilva.cuacuapp.activities.dashboard.CuacListActivity;
import com.ericksilva.cuacuapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nightonke.wowoviewpager.Animation.ViewAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoPositionAnimation;
import com.nightonke.wowoviewpager.Animation.WoWoScaleAnimation;
import com.nightonke.wowoviewpager.Enum.Ease;
import com.nightonke.wowoviewpager.WoWoViewPager;
import com.nightonke.wowoviewpager.WoWoViewPagerAdapter;

import java.util.Arrays;

public class OnboardingActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 307;

    private boolean animationAdded = false;
    protected int screenW;
    protected int screenH;

    protected WoWoViewPager wowo;
    private View btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_onboarding);
        wowo = findViewById(R.id.wowo_viewpager);
        wowo.setAdapter(WoWoViewPagerAdapter.builder()
                .fragmentManager(getSupportFragmentManager())
                .count(3)
                .colorsRes(fragmentColorsRes())
                .build());
        wowo.addOnPageChangeListener(onPageChangeListener);

        btnSkip = findViewById(R.id.btn_skip);
        btnSkip.setOnClickListener(onClickListener);

        Display display = (this).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenW = size.x;
        screenH = size.y;
    }

    protected Integer[] fragmentColorsRes() {
        return new Integer[]{
                R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorPrimary
        };
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        addAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // Listeners
    View.OnClickListener onClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            checkFirebaseLogin();
        }
    };

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 2) {
                btnSkip.setVisibility(View.VISIBLE);
            }else{
                btnSkip.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    //WoWo Animation
    private void addAnimations() {

        if (animationAdded) return;
        animationAdded = true;

        View view = findViewById(R.id.page1);
        float radius = view.getWidth() / 2;
        ViewAnimation viewAnimation = new ViewAnimation(view);
        viewAnimation.add(WoWoPositionAnimation.builder().page(0).start(0.25).end(0.75)
                .fromX(screenW / 2 - radius).toX(-radius*2)
                .fromY(screenH / 2 - radius).toY(screenH/2-radius)
                .ease(Ease.Linear).build());

        View view2 = findViewById(R.id.page2);
        float radius2 = view2.getWidth() / 2;
        ViewAnimation viewAnimation2 = new ViewAnimation(view2);
        viewAnimation2.add(WoWoPositionAnimation.builder().page(0).start(0.25).end(0.75)
                .fromX(screenW).toX(screenW/2-radius2)
                .fromY(screenH / 2 - radius2).toY(screenH/2-radius2)
                .ease(Ease.Linear).build());
        viewAnimation2.add(WoWoPositionAnimation.builder().page(1).start(0.25).end(0.75)
                .fromX(screenW / 2 - radius2).toX(-radius2*2)
                .fromY(screenH / 2 - radius2).toY(screenH/2-radius2)
                .ease(Ease.Linear).build());

        View view3 = findViewById(R.id.page3);
        float radius3 = view3.getWidth() / 2;
        ViewAnimation viewAnimation3 = new ViewAnimation(view3);
        viewAnimation3.add(WoWoPositionAnimation.builder().page(0).start(0.25).end(0.75)
                .fromX(screenW).toX(screenW)
                .fromY(screenH / 2 - radius3).toY(screenH/2-radius3)
                .ease(Ease.Linear).build());
        viewAnimation3.add(WoWoPositionAnimation.builder().page(1).start(0.25).end(0.75)
                .fromX(screenW).toX(screenW/2-radius3)
                .fromY(screenH / 2 - radius3).toY(screenH/2-radius3)
                .ease(Ease.Linear).build());


        View dot1 = findViewById(R.id.indicator1);
        ViewAnimation dotAnimation = new ViewAnimation(dot1);
        dotAnimation.add(WoWoScaleAnimation.builder().page(0).start(0.25).end(0.75)
                .fromX(2).toX(1)
                .fromY(1.5).toY(1).build());

        View dot2 = findViewById(R.id.indicator2);
        ViewAnimation dotAnimation2 = new ViewAnimation(dot2);
        dotAnimation2.add(WoWoScaleAnimation.builder().page(0).start(0.25).end(0.75)
                .fromX(1).toX(2)
                .fromY(1).toY(1.5).build());
        dotAnimation2.add(WoWoScaleAnimation.builder().page(1).start(0.25).end(0.75)
                .fromX(2).toX(1)
                .fromY(1.5).toY(1).build());

        View dot3 = findViewById(R.id.indicator3);
        ViewAnimation dotAnimation3 = new ViewAnimation(dot3);
        dotAnimation3.add(WoWoScaleAnimation.builder().page(0).start(0.25).end(0.75)
                .fromXY(1).toXY(1).build());
        dotAnimation3.add(WoWoScaleAnimation.builder().page(1).start(0.25).end(0.75)
                .fromX(1).toX(2)
                .fromY(1).toY(1.5).build());

        wowo.addAnimation(viewAnimation);
        wowo.addAnimation(viewAnimation2);
        wowo.addAnimation(viewAnimation3);
        wowo.addAnimation(dotAnimation);
        wowo.addAnimation(dotAnimation2);
        wowo.addAnimation(dotAnimation3);
        wowo.setUseSameEaseBack(true);
        wowo.ready();
    }


//    FIREBASE LOGIN
    private void checkFirebaseLogin(){

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
//                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.FacebookBuilder().build(),
                                new AuthUI.IdpConfig.EmailBuilder().build()
                        ))
                        .setTheme(R.style.LoginTheme)
                        .setLogo(R.drawable.logog)
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                createUser(response);
                startActivity(CuacListActivity.createIntent(this));
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
//                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
//                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

//                showSnackbar(R.string.unknown_error);
                Log.e("LOGIN", "Sign-in error: ", response.getError());
            }
        }
    }

    private void createUser(IdpResponse response ){
        if (response!= null){
            User user = new User(response.getUser().getName(),response.getEmail(),0);
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("users").document(uid).set(user.getMap());
        }
    }
}
