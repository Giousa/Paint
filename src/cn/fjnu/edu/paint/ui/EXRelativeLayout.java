package cn.fjnu.edu.paint.ui;
import cn.fjnu.edu.paint.engine.DrawView;
import cn.fjnu.edu.ui.activity.PaintMainActivity;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
public class EXRelativeLayout  extends RelativeLayout {

	//������ĺ�����
//	private float rX;
	private float mX;
	private boolean isContinue;//�ж�ActionDown֮���Ƿ����ִ��ActionMove
	public EXRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}


	public EXRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}


	public EXRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}


	//���ش����¼�
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		//Toast.makeText(getContext(), "love", Toast.LENGTH_SHORT).show();
	//	rX=ev.getX();
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			//�õ���Ļ���
		
			/*if(Math.abs(rX-getWidth())<10){
				
				isContinue=true;
				DrawView.listenMode=DrawView.SLIDE_MODE;
			}
			
			else
				isContinue=false;
*/
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if(PaintMainActivity.isReduce)
				DrawView.listenMode=DrawView.PAINT_MODE;
			break;
		default:
			break;
		}
		//touch�¼�����view
		return false;
	}


	//MianView�����¼�
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mX=event.getX();
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			//�õ���Ļ���
			if(Math.abs(mX-getWidth())<10){
				isContinue=true;
				DrawView.listenMode=DrawView.SLIDE_MODE;
			}
				
			else
				isContinue=false;
			break;
		
		case MotionEvent.ACTION_MOVE:
			if(isContinue){
				if(Math.abs(mX-getWidth())>20){
				//	MainActivity.MActivity.showSlide();
				
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(PaintMainActivity.isReduce)
				DrawView.listenMode=DrawView.PAINT_MODE;
			break;
		default:
			break;
		}
	  return true;
	}
	
}
