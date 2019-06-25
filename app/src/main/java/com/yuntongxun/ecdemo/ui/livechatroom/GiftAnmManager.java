package com.yuntongxun.ecdemo.ui.livechatroom;

import android.content.Context;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.yuntongxun.ecdemo.R;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by yan on 2016/12/14.
 */

public class GiftAnmManager {

    private BSRGiftLayout giftLayout;
    private Context context;
    int[] car1Ids = new int[]{
            R.drawable.paoche0,
            R.drawable.paoche1,
            R.drawable.paoche2,
            R.drawable.paoche3,
            R.drawable.paoche4,
            R.drawable.paoche5,
            R.drawable.paoche6,
            R.drawable.paoche7,
            R.drawable.paoche8,
            R.drawable.paoche9,
            R.drawable.paoche10,
            R.drawable.paoche11,
            R.drawable.paoche12,
            R.drawable.paoche13,
            R.drawable.paoche14,
            R.drawable.paoche15,
            R.drawable.paoche16,
            R.drawable.paoche17,
            R.drawable.paoche18,
    };
    int[] car2Ids = new int[]{
//            R.drawable.gift_car_t1,
//            R.drawable.gift_car_t2,
    };
    int[] dragonIds = new int[]{
            R.drawable.lanseyaoji00001,
            R.drawable.lanseyaoji00002,
            R.drawable.lanseyaoji00003,
            R.drawable.lanseyaoji00004,
            R.drawable.lanseyaoji00005,
            R.drawable.lanseyaoji00006,
            R.drawable.lanseyaoji00007,
            R.drawable.lanseyaoji00008,
            R.drawable.lanseyaoji00009,
            R.drawable.lanseyaoji00010,
            R.drawable.lanseyaoji00011,
            R.drawable.lanseyaoji00012,
            R.drawable.lanseyaoji00013,
            R.drawable.lanseyaoji00014,
            R.drawable.lanseyaoji00015,
            R.drawable.lanseyaoji00016,
            R.drawable.lanseyaoji00017,
            R.drawable.lanseyaoji00018,
            R.drawable.lanseyaoji00019,
            R.drawable.lanseyaoji00020,
            R.drawable.lanseyaoji00021,
            R.drawable.lanseyaoji00022,
    };

    public GiftAnmManager(BSRGiftLayout giftLayout, Context context) {
        this.context = context;
        this.giftLayout = giftLayout;
    }

    public void showCarOne(){
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        bsrGiftView.setAlphaTrigger(-1);

        final int during = 150;
        final Subscription[] subscription = new Subscription[1];

        Flowable.interval(during, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Subscriber<Long>() {
                    int index = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription[0] = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        BSRPathPoint carOne = new BSRPathPoint();
                        carOne.setDuring(during);
                        carOne.setInterpolator(new LinearInterpolator());
                        carOne.setRes(context, car1Ids[index++ % 7]);
                        carOne.setAdjustScaleInScreen(0.8f);
                        carOne.setAntiAlias(true);
                        bsrGiftView.addBSRPathPointAndDraw(carOne);
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                    }
                });

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setPositionInScreen(true);
        bsrPathView.addPositionControlPoint(1, 0.1f);
        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
        bsrPathView.addPositionControlPoint(-1f, 0.5f);

        bsrPathView.setDuring(3000);
        bsrPathView.addEndListeners(new OnAnmEndListener() {
            @Override
            public void onAnimationEnd(BSRPathBase bsrPathPoint) {
                subscription[0].cancel();
            }
        });

        giftLayout.addChild(bsrPathView);
    }
    public void showLove(){
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        bsrGiftView.setAlphaTrigger(-1);

        final int during = 150;
        final Subscription[] subscription = new Subscription[1];

        Flowable.interval(during, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Subscriber<Long>() {
                    int index = 0;

                    @Override
                    public void onSubscribe(Subscription s){
                        subscription[0] = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong){
                        BSRPathPoint carOne = new BSRPathPoint();
                        carOne.setDuring(during);
                        carOne.setInterpolator(new LinearInterpolator());
                        carOne.setRes(context, dragonIds[index++ % 7]);
                        carOne.setAdjustScaleInScreen(1.0f);
                        carOne.setAntiAlias(true);
                        bsrGiftView.addBSRPathPointAndDraw(carOne);
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                    }
                });

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setPositionInScreen(true);
//        bsrPathView.addPositionControlPoint(1, 0.1f);
//        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
//        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
//        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
//        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
//        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
//        bsrPathView.addPositionControlPoint(0.05f, 0.3f);
//        bsrPathView.addPositionControlPoint(-1f, 0.5f);

        bsrPathView.setDuring(3000);
        bsrPathView.addEndListeners(new OnAnmEndListener() {
            @Override
            public void onAnimationEnd(BSRPathBase bsrPathPoint) {
                subscription[0].cancel();
            }
        });

        giftLayout.addChild(bsrPathView);
    }

    public void showCarOnePath() {
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        bsrGiftView.setAlphaTrigger(-1);
        final int during = 150;
        final Subscription[] subscription = new Subscription[1];

        Flowable.interval(during, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Subscriber<Long>() {
                    int index = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription[0] = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        BSRPathPoint carOne = new BSRPathPoint();
                        carOne.setDuring(during);
                        carOne.setInterpolator(new LinearInterpolator());
                        carOne.setRes(context, car1Ids[index++ % 7]);
                        carOne.centerInside();
                        carOne.setAntiAlias(true);
                        bsrGiftView.addBSRPathPointAndDraw(carOne);
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                    }
                });

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setPositionInScreen(true);
        bsrPathView.setPositionXPercent(0.5f);
        bsrPathView.setPositionYPercent(0.5f);
        bsrPathView.addPositionControlPoint(0, 0);
        bsrPathView.addPositionControlPoint(0, 1);
        bsrPathView.addPositionControlPoint(1, 1);
        bsrPathView.addPositionControlPoint(1, 0);
        bsrPathView.setScale(0.3f);
        bsrPathView.setFirstRotation(90);
        bsrPathView.setXPercent(0.5f);
        bsrPathView.setYPercent(0.5f);
        bsrPathView.setAutoRotation(true);
        bsrPathView.setDuring(3000);
        bsrPathView.addEndListeners(new OnAnmEndListener() {
            @Override
            public void onAnimationEnd(BSRPathBase bsrPathPoint) {
                subscription[0].cancel();
            }
        });
        giftLayout.addChild(bsrPathView);
    }

    public void showCarTwo() {
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        bsrGiftView.setAlphaTrigger(-1);
        final int during = 300;
        final Subscription[] subscription = new Subscription[1];
        Flowable.interval(during, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Subscriber<Long>() {
                    int index = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription[0] = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        BSRPathPoint carTwo = new BSRPathPoint();
                        carTwo.setDuring(during);
                        carTwo.setInterpolator(new LinearInterpolator());
                        carTwo.setRes(context, car2Ids[index++ % 2]);
                        carTwo.setAdjustScaleInScreen(1f);
                        carTwo.setAntiAlias(true);
                        bsrGiftView.addBSRPathPointAndDraw(carTwo);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setPositionInScreen(true);
        bsrPathView.addPositionControlPoint(0f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addPositionControlPoint(0.06f, 0.3f);
        bsrPathView.addScaleControl(0.2f);
        bsrPathView.addScaleControl(0.8f);
        bsrPathView.addScaleControl(0.8f);
        bsrPathView.addScaleControl(0.8f);
        bsrPathView.addScaleControl(0.8f);
        bsrPathView.addScaleControl(0.8f);
        bsrPathView.addScaleControl(0.8f);
        bsrPathView.addScaleControl(10f);
        bsrPathView.setXPercent(0f);
        bsrPathView.setYPercent(0f);
        bsrPathView.setDuring(2000);
        bsrPathView.setInterpolator(new AccelerateInterpolator());
        bsrPathView.addEndListeners(new OnAnmEndListener() {
            @Override
            public void onAnimationEnd(BSRPathBase bsrPathPoint) {
                subscription[0].cancel();
            }
        });
        giftLayout.addChild(bsrPathView);
    }

    public void showKQ() {
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        int during = 1200;
        bsrGiftView.setAlphaTrigger(1);
        final List<BSRPathPoint> bsrPathPoints = new ArrayList<BSRPathPoint>();
        for (int i = 0; i < 13; i++) {
            BSRPathPoint bsrPointT = new BSRPathPoint();

            if (i == 0) {
                bsrPointT.setLastRotation(0);
            } else if (i > 0 && i < 7) {
                bsrPointT.setLastRotation(i * 20);
            } else if (i >= 7 && i < 13) {
                bsrPointT.setLastRotation(-(i - 6) * 20);
            }
            bsrPointT.setPositionInScreen(true);
            bsrPointT.setPositionPoint(0.5f, 0.1f);
            bsrPointT.setPositionXPercent(0.5f);
            bsrPointT.setRes(context, R.drawable.def_usericon);
//            bsrPointT.setRes(context, R.drawable.kongque_cibang1);//
            bsrPointT.setDuring(during);
            bsrPointT.setXPercent(0.5f);
            bsrPointT.setYPercent(1.0f);
            bsrPointT.setAdjustScaleInScreen(1f);
            bsrPointT.setScale(0.2f);
            bsrPointT.setAntiAlias(true);
            bsrPointT.setInterpolator(new DecelerateInterpolator());
            bsrPathPoints.add(bsrPointT);
        }

        BSRPathPoint bsrPoint = new BSRPathPoint();
        bsrPoint.setPositionInScreen(true);
        bsrPoint.attachPoint(bsrPathPoints.get(0), -0.01f, 0.13f);
        bsrPoint.setRes(context, R.drawable.def_usericon);
//        bsrPoint.setRes(context, R.drawable.kongque_main);
        bsrPoint.setDuring(during);
        bsrPoint.setAdjustScaleInScreen(1);
        bsrPoint.setScale(0.4f);
        bsrPoint.setInterpolator(new DecelerateInterpolator());
        bsrPoint.setAntiAlias(true);
        bsrPathPoints.add(bsrPoint);

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setDuring(during * 2);

        giftLayout.addChild(bsrPathView);
        bsrGiftView.addBSRPathPoints(bsrPathPoints);
    }

    public void showPositionInScreen() {
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        int during = 2000;
        bsrGiftView.setAlphaTrigger(0.99f);
        final List<BSRPathPoint> bsrPathPoints = new ArrayList<BSRPathPoint>();

        BSRPathPoint bsrPoint = new BSRPathPoint();
        bsrPoint.setRes(context, R.drawable.def_usericon);
//        bsrPoint.setRes(context, R.drawable.gift_car_t2);
        bsrPoint.setDuring(during);
        bsrPoint.setPositionInScreen(true);
        bsrPoint.setFirstRotation(-90);
        bsrPoint.setAutoRotation(true);
        bsrPoint.setAdjustScaleInScreen(1f);
        bsrPoint.addPositionControlPoint(0f, 0f);
        bsrPoint.addPositionControlPoint(1f, 0f);
        bsrPoint.addPositionControlPoint(1f, 1f);
        bsrPoint.addPositionControlPoint(0f, 1f);
        bsrPoint.setScale(0.4f);
        bsrPoint.setInterpolator(new DecelerateInterpolator());
        bsrPoint.setAntiAlias(true);
        bsrPoint.setPositionXPercent(0.5f);
        bsrPoint.setPositionYPercent(0.5f);
        bsrPathPoints.add(bsrPoint);

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setDuring(during * 2);

        giftLayout.addChild(bsrPathView);
        bsrGiftView.addBSRPathPoints(bsrPathPoints);
    }

    public void showDragon() {
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        final int during = 60;
        Flowable.interval(during, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Subscriber<Long>() {
                    Subscription subscription;
                    int index = 0;

                    @Override
                    public void onSubscribe(Subscription s) {
                        subscription = s;
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (index++ < 33) {
                            BSRPathPoint dragon = new BSRPathPoint();
                            dragon.setDuring(during + 2);
                            dragon.setInterpolator(new LinearInterpolator());
                            dragon.setRes(context, dragonIds[index]);
                            bsrGiftView.addBSRPathPointAndDraw(dragon);
                        } else {
                            bsrGiftView.addBSRPathPointAndDraw(null);
                            subscription.cancel();
                        }
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                    }
                });
        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setDuring(2000 * 2);
        giftLayout.setAlphaTrigger(0.99f);
        giftLayout.addChild(bsrPathView);
    }

    public void showKiss(){
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        int during = 2000;
        bsrGiftView.setAlphaTrigger(0.99f);
        List<BSRPathPoint> bsrPathPoints = new ArrayList<BSRPathPoint>();

        BSRPathPoint pathPoint = new BSRPathPoint();
        pathPoint.setRes(context, R.drawable.def_usericon);
//        pathPoint.setRes(context, R.drawable.kiss_chun);
        pathPoint.setDuring(during);
        pathPoint.setPositionInScreen(true);
        pathPoint.setPositionXPercent(0.5f);
        pathPoint.setPositionYPercent(0.5f);
        pathPoint.setPositionPoint(0.68f, 0.45f);
        pathPoint.addScaleControl(0.2f);
        pathPoint.addScaleControl(0.8f);
        pathPoint.addScaleControl(0.8f);
        pathPoint.addScaleControl(0.8f);
        pathPoint.addScaleControl(0.8f);
        pathPoint.addScaleControl(0.8f);
        pathPoint.addScaleControl(0.8f);
        pathPoint.setXPercent(0.5f);
        pathPoint.setYPercent(0.5f);
        pathPoint.setAdjustScaleInScreen(0.5f);
        pathPoint.setAntiAlias(true);
        bsrPathPoints.add(pathPoint);

        BSRPathPoint pathPointHeart1 = new BSRPathPoint();
        pathPointHeart1.setRes(context, R.drawable.def_usericon);
//        pathPointHeart1.setRes(context, R.drawable.kiss_xin);
        pathPointHeart1.setDuring(during);
        pathPointHeart1.setPositionInScreen(true);
        pathPointHeart1.setPositionXPercent(0.5f);
        pathPointHeart1.setPositionYPercent(0.5f);
        pathPointHeart1.setPositionPoint(0.3f, 0.45f);
        pathPointHeart1.addScaleControl(0.0f);
        pathPointHeart1.addScaleControl(0.0f);
        pathPointHeart1.addScaleControl(0.0f);
        pathPointHeart1.addScaleControl(0.0f);
        pathPointHeart1.addScaleControl(0.8f);
        pathPointHeart1.addScaleControl(0.8f);
        pathPointHeart1.addScaleControl(0.8f);
        pathPointHeart1.addScaleControl(0.8f);
        pathPointHeart1.setXPercent(0.5f);
        pathPointHeart1.setYPercent(0.5f);
        pathPointHeart1.setAdjustScaleInScreen(0.21f);
        pathPointHeart1.setAntiAlias(true);
        bsrPathPoints.add(pathPointHeart1);

        BSRPathPoint pathPointHeart2 = new BSRPathPoint();
        pathPointHeart2.setRes(context, R.drawable.def_usericon);
//        pathPointHeart2.setRes(context, R.drawable.kiss_xin);
        pathPointHeart2.setDuring(during);
        pathPointHeart2.setPositionInScreen(true);
        pathPointHeart2.setPositionXPercent(0.5f);
        pathPointHeart2.setPositionYPercent(0.5f);
        pathPointHeart2.setPositionPoint(0.22f, 0.45f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.0f);
        pathPointHeart2.addScaleControl(0.5f);
        pathPointHeart2.addScaleControl(0.5f);
        pathPointHeart2.setXPercent(0.5f);
        pathPointHeart2.setYPercent(0.5f);
        pathPointHeart2.setAdjustScaleInScreen(0.17f);
        pathPointHeart2.setAntiAlias(true);
        bsrPathPoints.add(pathPointHeart2);

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setDuring(during * 2);

        giftLayout.addChild(bsrPathView);
        bsrGiftView.addBSRPathPoints(bsrPathPoints);
    }

    public void showShip() {
        final BSRGiftView bsrGiftView = new BSRGiftView(context);
        int during = 2000;
        bsrGiftView.setAlphaTrigger(0.99f);
        List<BSRPathPoint> bsrPathPoints = new ArrayList<BSRPathPoint>();

        BSRPathPoint pathPoint = new BSRPathPoint();
        pathPoint.setRes(context, R.drawable.def_usericon);
//        pathPoint.setRes(context, R.drawable.ship02);
        pathPoint.setDuring(during);

        pathPoint.setPositionInScreen(true);
        pathPoint.addPositionControlPoint(0f, 0.6f);
        pathPoint.addPositionControlPoint(0.45f, 0.5f);
        pathPoint.addPositionControlPoint(0.45f, 0.5f);
        pathPoint.addPositionControlPoint(0.45f, 0.5f);
        pathPoint.addPositionControlPoint(0.45f, 0.5f);
        pathPoint.setPositionXPercent(0.5f);
        pathPoint.setPositionYPercent(0.5f);
        pathPoint.setAdjustScaleInScreen(1);
        pathPoint.addScaleControl(0.3f);
        pathPoint.addScaleControl(0.7f);
        pathPoint.addScaleControl(0.7f);
        pathPoint.addScaleControl(0.7f);
        pathPoint.addScaleControl(0.7f);
        pathPoint.setXPercent(0.5f);
        pathPoint.setYPercent(0.5f);
        pathPoint.setAntiAlias(true);
        bsrPathPoints.add(pathPoint);

        BSRPathPoint pathPoint2 = new BSRPathPoint();
        pathPoint2.setRes(context, R.drawable.def_usericon);
//        pathPoint2.setRes(context, R.drawable.ship01);
        pathPoint2.setDuring(during);
        pathPoint2.setPositionInScreen(true);
        pathPoint2.attachPoint(bsrPathPoints.get(0), 0, 0.265f);
        pathPoint2.setXPercent(0.5f);
        pathPoint2.setYPercent(0.5f);
        pathPoint2.setAdjustScaleInScreen(1);
        pathPoint2.addRotationControl(-2f);
        pathPoint2.addRotationControl(-2f);
        pathPoint2.addRotationControl(-2f);
        pathPoint2.addRotationControl(-2f);
        pathPoint2.addRotationControl(5f);
        pathPoint2.addRotationControl(5f);
        pathPoint2.addRotationControl(0f);
        pathPoint2.setAntiAlias(true);
        bsrPathPoints.add(pathPoint2);

        BSRPathView bsrPathView = new BSRPathView();
        bsrPathView.setChild(bsrGiftView);
        bsrPathView.setDuring(during * 2);

        giftLayout.addChild(bsrPathView);
        bsrGiftView.addBSRPathPoints(bsrPathPoints);

    }
}
