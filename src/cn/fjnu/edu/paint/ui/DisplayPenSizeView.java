package cn.fjnu.edu.paint.ui;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;
public class DisplayPenSizeView extends ImageView {
	
	public DisplayPenSizeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public DisplayPenSizeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	private Bitmap mBitmap;
	private Canvas mCanvas;
	public  Paint mBitmapPaint;// �����Ļ���
	public  Paint mPaint;// ��ʵ�Ļ���
	//public static Paint exPaint;//��������ʹ��
	// ��¼Path·���Ķ���
	//������ɫ
	public   int color =Color.GREEN;
	public  int srokeWidth = 5;

		//���ʴ�ϸ��ʾ
		public void displayPenSize(int size){
			
			mBitmap=Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
			if(mCanvas==null)
				mCanvas=new Canvas();
			mCanvas.setBitmap(mBitmap);
			if(mPaint==null){
				mPaint=new Paint();
				mPaint.setAntiAlias(true);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.ROUND);// �������Ե
				mPaint.setStrokeCap(Paint.Cap.ROUND);// ��״
			}
			mPaint.setColor(Color.BLACK);
			mPaint.setStrokeWidth((float)size);
			mCanvas.drawLine(0, getHeight()/2, getWidth(),getHeight()/2, mPaint);
			//Log.i("displaypensize",""+0);
			invalidate();
			
		}
		
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stubm
		super.onDraw(canvas);
		if(mBitmap!=null)
			canvas.drawBitmap(mBitmap, 0, 0, null);
	}

}
