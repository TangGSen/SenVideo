package player;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

public abstract class VideoBaseActivity extends FragmentActivity implements OnClickListener{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
		initView();
		initListener();
		initData();
	}
	
	protected abstract void initView();
	protected abstract void initListener();
	protected abstract void initData();
	
	/**
	 * 可以处理共同点击事件的按钮
	 * @param v
	 */
	protected abstract void processClick(View v);
	
	@Override
	public void onClick(View v) {
		processClick(v);
	}
}
