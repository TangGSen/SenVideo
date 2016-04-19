package player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.sen.redbull.mode.EventNoThing;
import com.sen.redbull.mode.EventUpateStudyProgress;
import com.sen.redbull.mode.StudyProgressBean;
import com.sen.redbull.tools.AcountManager;
import com.sen.redbull.tools.Constants;
import com.sen.redbull.tools.StudyProgressManager;
import com.sen.redbull.tools.ToastUtils;
import com.sen.videolib.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;


public class VideoPlayerActivity extends VideoBaseActivity {


    // 计算学时
    long startTime;
    long endTime;
    int useTime;

    private int alltime;
    private boolean eventBusPost;

    private VideoView video_view;
    // 顶部控制面板控件
    private LinearLayout ll_top_control;
    private TextView tv_name, tv_system_time;
    private ImageView iv_battery;
    // 底部控制面板控件
    private LinearLayout ll_bottom_control;
    private SeekBar video_seekbar;
    private TextView tv_current_progress, tv_duration;
    private AppCompatTextView btn_exit, btn_play, btn_screen, btn_exit_video, tv_volume;
    private RelativeLayout ll_loading;
    private LinearLayout ll_buffering;

    private BatteryChangeReceiver batteryChangeReceiver;
    //音量广播
    private VolumeReceiver mVolumeReceiver;

    private final int MESSAGE_UPDATE_SYSTEM_TIME = 0;// 更新系统时间
    private final int MESSAGE_UPDATE_PLAY_PROGRESS = 1;// 更新播放进度
    private final int MESSAGE_HIDE_CONTROL = 2;// 延时隐藏控制面板

    private final int CLOSE_VOLUME_SHOW = 3;//延时关闭声音

    private AudioManager audioManager;
    private int currentVolume;// 当前的音量
    private int maxVolume;// 系统最大音量
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    private boolean isMute = false;// 是否是静音模式

    private final int STUDY_PROGRESS_RESUTLS = 4;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_SYSTEM_TIME:
                    updateSystemTime();
                    break;
                case MESSAGE_UPDATE_PLAY_PROGRESS:
                    updatePlayProgress();
                    break;
                case MESSAGE_HIDE_CONTROL:
                    hideControlLayout();
                    break;
                case STUDY_PROGRESS_RESUTLS:
                    showStudyResoults(msg);
                    break;
                case CLOSE_VOLUME_SHOW:
                    if (tv_volume != null) {
                        tv_volume.setVisibility(View.GONE);
                    }
                    break;
            }
        }


    };


//    private void showStudyResoults(Message message) {
//        StudyProgressBean progressBean = (StudyProgressBean) message.obj;
//        String msg = progressBean.getMsg();
//        String success = progressBean.getSuccess();
//        if ("true".equals(success) && "学习数据更新成功！".equals(msg)) {
//            ToastUtils.showTextToast(VideoPlayerActivity.this, "更新学分成功");
//        }
//        if ("false".equals(success)) {
//            if (!"学习进度更新失败!".equals(msg) || !"学习总时长更新失败!".equals(msg)
//                    || !"该课程不存在，无法记录学时!".equals(msg)) {
//                // 如果不是这些的话，那么就要删除原来的
//                StudyProgressManager.deleLeidData(AcountManager.getAcountId(), courseId);
//
//            } else if ("学习进度更新失败!".equals(msg) || "学习总时长更新失败!".equals(msg)) {
//                StudyProgressManager.insertTimeById(AcountManager.getAcountId(), courseId, alltime);
//
//            }
//
//        }
//    }

    private int screenWidth;
    private int screenHeight;
    private int mTouchSlop;// 滑动的界限值
    private int currentPosition;// 当前视频在videoList中的位置
    private ArrayList<VideoItem> videoList;// 视频列表
    private GestureDetector gestureDetector;
    private boolean isShowContol = false;// 是否是显示控制面板


    //DBDao mDbDao;

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_video_player);
        video_view = (VideoView) findViewById(R.id.video_view);

        ll_top_control = (LinearLayout) findViewById(R.id.ll_top_control);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_system_time = (TextView) findViewById(R.id.tv_system_time);
        iv_battery = (ImageView) findViewById(R.id.iv_battery);

        ll_bottom_control = (LinearLayout) findViewById(R.id.ll_bottom_control);
        video_seekbar = (SeekBar) findViewById(R.id.video_seekbar);
        btn_play = (AppCompatTextView) findViewById(R.id.btn_play);
        btn_exit = (AppCompatTextView) findViewById(R.id.btn_exit);
        btn_exit_video = (AppCompatTextView) findViewById(R.id.btn_exit_video);
        btn_screen = (AppCompatTextView) findViewById(R.id.btn_screen);
        tv_current_progress = (TextView) findViewById(R.id.tv_current_progress);
        tv_duration = (TextView) findViewById(R.id.tv_duration);

        tv_volume = (AppCompatTextView) findViewById(R.id.tv_volume);

        ll_loading = (RelativeLayout) findViewById(R.id.ll_loading);
        ll_buffering = (LinearLayout) findViewById(R.id.ll_buffering);

    }

    public void onEvent(EventNoThing childItemBean) {


    }

    @Override
    protected void initListener() {
        btn_exit.setOnClickListener(this);
        btn_exit_video.setOnClickListener(this);
        btn_screen.setOnClickListener(this);
        btn_play.setOnClickListener(this);

        video_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, 5000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(MESSAGE_HIDE_CONTROL);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    video_view.seekTo(progress);
                    tv_current_progress.setText(StringUtil
                            .formatVideoDuration(progress));
                }
            }
        });
        // 监听播放结束
        video_view.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //  btn_play.setBackgroundResource(R.drawable.selector_btn_play);
                endTime = System.currentTimeMillis();
                //    calculateAndUpload(startTime, endTime);
                Toast.makeText(getApplicationContext(), "视频播放完毕", Toast.LENGTH_SHORT).show();
                if (eventBusPost) {

                    EventBus.getDefault().post(new EventUpateStudyProgress());
                }
                finish();
            }
        });
        video_view
                .setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        // LogUtil.e(this, "percent: "+percent);
                        // percent:0-100
                        int bufferedProgress = (int) ((percent / 100.0f) * video_view
                                .getDuration());
                        video_seekbar.setSecondaryProgress(bufferedProgress);
                    }
                });
        video_view.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:// 当拖动卡顿开始时调用
                        ll_buffering.setVisibility(View.VISIBLE);
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:// 当拖动卡顿结束调用
                        ll_buffering.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
        video_view.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Toast.makeText(VideoPlayerActivity.this, "不支持该格式", Toast.LENGTH_SHORT)
                                .show();
                        break;

                }
                return false;
            }
        });
    }

    @Override
    protected void initData() {
        gestureDetector = new GestureDetector(this, new MyGestureLitener());
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        mTouchSlop = ViewConfiguration.getTouchSlop();
        updateSystemTime();
        registerBatteryChangeReceiver();
        RegisterReceiverVolumeChange();
        //初始化音量
        initVolume();
        Uri uri = getIntent().getData();
//        String fileName = getIntent().getStringExtra("courseName");
        courseId = getIntent().getStringExtra("courseId");
        eventBusPost = getIntent().getBooleanExtra("eventCanPost", false);
        Log.e("sen", eventBusPost + "");
        if (TextUtils.isEmpty(courseId)) {
            courseId = "";
        }
        if (uri != null) {
            // 从文件发起的播放请求

            tv_name.setText("");
            video_view.setVideoURI(uri);

        } else {
            // 正常从视频列表进入的
            currentPosition = getIntent().getExtras().getInt("currentPosition");
            videoList = (ArrayList<VideoItem>) getIntent().getExtras()
                    .getSerializable("videoList");

            playVideo();
        }

        video_view.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // ll_loading.setVisibility(View.GONE);
                // 给加载界面增加渐隐动画
                ViewPropertyAnimator.animate(ll_loading).alpha(0)
                        .setDuration(600).setListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator arg0) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0) {
                    }

                    @Override
                    public void onAnimationEnd(Animator arg0) {
                        ll_loading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator arg0) {
                    }
                });
                // 视频开始播放
                video_view.start();
//                btn_play.setBackgroundResource(R.drawable.selector_btn_pause);
                startTime = System.currentTimeMillis();
                video_seekbar.setMax(video_view.getDuration());
                tv_duration.setText(StringUtil.formatVideoDuration(video_view
                        .getDuration()));
                updatePlayProgress();
            }
        });

        // video_view.setMediaController(new MediaController(this));
    }

    /**
     * 初始化音量
     */
    private void initVolume() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.e("sen", maxVolume + "");
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        updataVolumeTextImg(tv_volume, currentVolume);

    }

    /**
     * 注册当音量发生变化时接收的广播
     */
    private void RegisterReceiverVolumeChange() {
        mVolumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION") && tv_volume != null) {
                int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                updataVolumeTextImg(tv_volume, currVolume);
            }
        }
    }

    //更新volume img and text
    private void updataVolumeTextImg(AppCompatTextView tv, int currnentVolume) {
        handler.removeMessages(CLOSE_VOLUME_SHOW);
        tv.setVisibility(View.VISIBLE);
        int percent = currnentVolume * 100 / maxVolume;
        int drawableId = 0;
        if (percent <= 0) {
            //low
            drawableId = R.drawable.volume_off;
        } else if (percent > 0 && percent <= 30) {
            drawableId = R.drawable.volume_middle;
        } else {
            drawableId = R.drawable.volume_high;
        }

        Drawable volumeImg = getResources().getDrawable(drawableId);

        volumeImg.setBounds(0, 0, volumeImg.getMinimumWidth(), volumeImg.getMinimumHeight());
        tv.setCompoundDrawables(volumeImg, null, null, null);
        tv.setText(percent + "");
        handler.sendEmptyMessageDelayed(CLOSE_VOLUME_SHOW, 3000);
    }


    // 在电话来的时候，或者被其他应用，就暂停播放
    @Override
    protected void onPause() {
        if (video_view.isPlaying()) {
            video_view.pause();
            endTime = System.currentTimeMillis();
            // calculateAndUpload(startTime, endTime);
            handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
        }
        super.onPause();
    }

    /**
     * 播放currentPosition当前位置的视频
     */
    private void playVideo() {
        if (videoList == null || videoList.size() == 0) {
            finish();
            return;
        }

        VideoItem videoItem = videoList.get(currentPosition);
        tv_name.setText(videoItem.getTitle());
        video_view.setVideoURI(Uri.parse(videoItem.getPath()));

    }

    /**
     * 更新播放进度
     */
    private void updatePlayProgress() {
        tv_current_progress.setText(StringUtil.formatVideoDuration(video_view
                .getCurrentPosition()));
        video_seekbar.setProgress(video_view.getCurrentPosition());
        handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PLAY_PROGRESS, 1000);
    }


    /**
     * 注册电量变化的广播接受者
     */
    private void registerBatteryChangeReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryChangeReceiver = new BatteryChangeReceiver();
        registerReceiver(batteryChangeReceiver, filter);
    }

    /**
     * 更新系统时间
     */
    private void updateSystemTime() {
        tv_system_time.setText(StringUtil.formatSystemTime());
        handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_SYSTEM_TIME, 1000);
    }

    @Override
    protected void processClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exit:
                if (video_view.isPlaying()) {
                    video_view.pause();
                    handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
                }
                if (eventBusPost) {

                    EventBus.getDefault().post(new EventUpateStudyProgress());
                }
                finish();
                break;
            case R.id.btn_exit_video:
                finish();
                break;
            case R.id.btn_play:
                if (video_view.isPlaying()) {
                    video_view.pause();
                    endTime = System.currentTimeMillis();
                    //正式才打开
                    //    calculateAndUpload(startTime, endTime);
                    handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
                } else {
                    video_view.start();
                    startTime = System.currentTimeMillis();
                    handler.sendEmptyMessage(MESSAGE_UPDATE_PLAY_PROGRESS);
                }
                updatePlayBtnBg();
                break;

            case R.id.btn_screen:
                video_view.switchScreen();
                updateScreenBtnBg();
                break;
        }
    }

    /**
     * 更新屏幕按钮的背景图片
     */
    private void updateScreenBtnBg() {
        Drawable drawable = ContextCompat.getDrawable(this, video_view.isFullScreen() ? R.drawable.btn_fullscreen
                : R.drawable.btn_defualt_screen);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        btn_screen.setCompoundDrawables(drawable, null, null, null);
    }

    private float downY;
    private String courseId;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(batteryChangeReceiver);
        unregisterReceiver(mVolumeReceiver);
        EventBus.getDefault().unregister(this);
        getApplication();
        getApplicationContext();
    }

    private class BatteryChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // level：表示系统电量等级，0-100
            int level = intent.getIntExtra("level", 0);
            updateBatteryBg(level);
        }
    }

    private class MyGestureLitener extends SimpleOnGestureListener {

        /**
         * 滑动 改变
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();

            int max = Math.max(screenHeight, screenWidth);

            if (mOldX > screenWidth * 4.0 / 5)// 右边滑动
                onVolumeSlide((mOldY - y) / max);
            else if (mOldX < screenWidth / 5.0)// 左边滑动
                // Log.e("sen","左边");
                onBrightnessSlide((mOldY - y) / max);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            processClick(btn_play);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            processClick(btn_screen);
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isShowContol) {
                // 隐藏操作
                hideControlLayout();
            } else {
                // 显示操作
                showControlLayout();
            }
            return super.onSingleTapConfirmed(e);
        }


    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.03f)
            lpa.screenBrightness = 0.03f;
        getWindow().setAttributes(lpa);


    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {


        int index = (int) ((percent * maxVolume * 0.5) + currentVolume);
        if (index > maxVolume)
            index = maxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        currentVolume = index;
        updataVolumeTextImg(tv_volume, index);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int ori = newConfig.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {


        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            //无效
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lp.addRule(RelativeLayout.BELOW,  video_view.getId());//chlidView1的下面
//            ll_bottom_control.setLayoutParams(lp);
        }
    }

    /**
     * 显示控制面板
     */
    private void showControlLayout() {

//        ViewPropertyAnimator.animate(ll_top_control).translationY(0)
//                .setDuration(100);
//        ViewPropertyAnimator.animate(ll_bottom_control).translationY(0)
//                .setDuration(100);
        ll_top_control.setVisibility(View.VISIBLE);
        ll_bottom_control.setVisibility(View.VISIBLE);
        isShowContol = true;

        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, 5000);
    }

    /**
     * 隐藏控制面板
     */
    private void hideControlLayout() {
//        ViewPropertyAnimator.animate(ll_top_control)
//                .translationY(-ll_top_control.getHeight()).setDuration(100);
//        ViewPropertyAnimator.animate(ll_bottom_control)
//                .translationY(ll_bottom_control.getHeight()).setDuration(100);
        ll_top_control.setVisibility(View.GONE);
        ll_bottom_control.setVisibility(View.GONE);
        isShowContol = false;

    }

    /**
     * 根据系统电量等级去设置对应的图片
     *
     * @param level
     */
    private void updateBatteryBg(int level) {
        if (level <= 0) {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_0);
        } else if (level > 0 && level <= 10) {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_10);
        } else if (level > 10 && level <= 20) {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_20);
        } else if (level > 20 && level <= 40) {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_40);
        } else if (level > 40 && level <= 60) {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_60);
        } else if (level > 60 && level <= 80) {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_80);
        } else {
            iv_battery.setBackgroundResource(R.mipmap.ic_battery_100);
        }
    }

    /**
     * 更新播放按钮的背景图片
     */
    private void updatePlayBtnBg() {
        Drawable drawable = ContextCompat.getDrawable(this, video_view.isPlaying() ? R.drawable.btn_pause_video : R.drawable.btn_play_video);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        btn_play.setCompoundDrawables(drawable, null, null, null);

    }

    /**
     * 学习进度更新失败! 学习总时长更新失败! （这两个就要冲重传记录） 该课程不存在，无法记录学时!(既不重传，也不记录)
     *
     * @param endTimes
     * @param startTimes
     */
    // 计算并且上传
    protected void calculateAndUpload(long startTimes, long endTimes) {
        useTime = (int) ((endTimes - startTimes) / 1000);

        // 小于两秒不提交
        if (useTime >= 1) {
            Log.e("sentime", useTime + "看视频时间");
            int dbTime = StudyProgressManager.getTimeById(AcountManager.getAcountId(), courseId);
            alltime = useTime + dbTime;

            Log.e("sen", alltime + "alltime");
            String url = Constants.PATH + Constants.PATH_LEARNINGPROGRESS;
            OkHttpUtils.post()
                    .url(url)
                    .addParams("userid", AcountManager.getAcountId())
                    .addParams("leID", courseId)
                    .addParams("learningtimes", alltime + "")
                    .build()
                    .execute(new Callback<StudyProgressBean>() {
                        @Override
                        public void onBefore(Request request) {
                            super.onBefore(request);
                        }

                        @Override
                        public StudyProgressBean parseNetworkResponse(Response response) throws Exception {

                            String string = response.body().string();
                            Log.e("sen", string);
                            StudyProgressBean lesssonBean = JSON.parseObject(string, StudyProgressBean.class);
                            return lesssonBean;
                        }

                        @Override
                        public void onError(Call call, Exception e) {
                            //网络出错，保存记录
                            StudyProgressManager.insertTimeById(AcountManager.getAcountId(), courseId, alltime);
                        }

                        @Override
                        public void onResponse(StudyProgressBean homeBeam) {
                            Message message = Message.obtain();
                            message.obj = homeBeam;
                            message.what = STUDY_PROGRESS_RESUTLS;
                            handler.sendMessage(message);

                        }
                    });

        }


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && eventBusPost) {
            EventBus.getDefault().post(new EventUpateStudyProgress());
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
