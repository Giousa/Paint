package cn.fjnu.edu.paint.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import cn.fjnu.edu.paint.R;
import cn.fjnu.edu.paint.data.BitmapType;
import cn.fjnu.edu.paint.data.Shape_Type;
import cn.fjnu.edu.paint.domain.BrokenPoint;
import cn.fjnu.edu.paint.domain.DrawPath;
import cn.fjnu.edu.paint.ui.ColorPicker;
import cn.fjnu.edu.paint.ui.OpacityBar;
import cn.fjnu.edu.ui.activity.PaintMainActivity;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;


@SuppressLint("ClickableViewAccessibility")
public class DrawView extends ImageView {
	
	public static final int COMMON_MODE=0;//ͨ��ģʽ
	public static final int  ERASER_MODE=1;//��Ƥ��ģʽ
	public static final int CUT_MODE=2;//ͼƬ�ü�ģʽ
	public static final int COPY_MODE=3;//λͼ����ģʽ
	public static final int FILLCOLOR_MODE=4;//��ɫģʽ
	public static final int PASTETEXT_MODE=5;//�������ģʽ
	public static final int SAVE_MODE=1;//����ģʽ
	public static final int PAINT_MODE=0;//����ģʽ
	public static final int SLIDE_MODE=1;//����ģʽ
	public static final int DRAW_MODE=0;//����ģʽ
	public static final int BITMAP_MODE=1;//��λͼģʽ
	public static final int DRAW_TEXT_MODE=2;//��������ģʽ
	public static boolean isIntercept=false;//�Ƿ�����DrawView�Ĵ����¼�
	public static boolean isMove=false;//�жϻ����Ƿ��ƶ�
	private int fillcolor;//�����ɫ
	private int cutFillColor;//�������ģʽ
	public static int listenMode=PAINT_MODE;//����Ϊ����ģʽ
	private int paintMode=COMMON_MODE;//����Ϊͨ��ģʽ
	private  int shapeMode=Shape_Type.FREE;//����Ϊ�����ֻ�
	private int currentShape=shapeMode;//��¼��ǰͼ������
	private float currentPenSize=5;//��¼��ǰ���ʿ��
	private BrokenPoint brokenPoint;//����ת�۵�
	private float mulX;//���������ĵ������
	private float mulY;//���������ĵ�������
	private float widthScale;//�������ű���
	private float heightScale;//�������ű���
	private Matrix matrix;//����/��ת����
	private Bitmap resizeBitmap;//����֮���bitmap
	private Bitmap roatBitmap;//��ת֮���bitmap
	private Bitmap copyBitmap;//Ҫ���Ƶ�bitmap
	private Bitmap tempBitmap;
	private boolean isInitDrawBroken=true;
	private boolean isInitZoom=true;//�Ƿ��������
	private boolean isInitRote=true;//�Ƿ������ת
	private boolean isshapeSave=false;//currentShape�Ƿ񱣴�
	public static boolean isFirstDraw=true;//�Ƿ���λ滭
	private RectF rectF;//����
	private Bitmap mBitmap;
	private Paint translatePaint;//͸������
	private Canvas mCanvas;
	private Canvas tempCanvas;//��ʱ����
	private Path mPath;
	public  Paint mBitmapPaint;// �����Ļ���
	public  Paint mPaint;// ��ʵ�Ļ���
	//public static Paint exPaint;//��������ʹ��
	private float mX, mY;// ��ʱ������
	private float endX,endY;//��ָ�뿪��Ļ������
	private static final float TOUCH_TOLERANCE = 4;
	// ����Path·���ļ���,��List������ģ��ջ�����ں��˲���
	private  List<DrawPath> savePath;
	// ����Path·���ļ���,��List������ģ��ջ,����ǰ������
	private  List<DrawPath> canclePath;
	// ��¼Path·���Ķ���
	private DrawPath dp;
	private DrawPath bmpDrawPath;
	private DrawPath lastDrawPath;
	private SeekBar zoomSeekBar;//����ͼƬ������
	private SeekBar roatSeekBar;//��תͼƬ������
	//������ɫ
	public  int color =Color.BLACK;
	public  float srokeWidth = 5;
	//�����Ƶ�����
	private String paintText=null;
	//���ڻ������ֵĻ���
	private Paint textPaint=null;
		public DrawView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		
		public DrawView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public Paint getPaint() {
			return mPaint;
		}
		public int getColor(){
			return  color;
		}
		public void setPaint(Paint mPaint) {
			this.mPaint = mPaint;
		}
		public void setBitPaint(Paint paint){
			this.mBitmapPaint=paint;
		}
		//���û�����ɫ
		public void setColor(int ex_color){
			
			color=ex_color;
			
		}
		//���û��ʴ�ϸ
		public void setPenSize(float size){
			srokeWidth=size;
			currentPenSize=size;
		}
		//��û��ʴ�ϸ
		public float getPenSize(){
			return srokeWidth;
		}
		//��ջ���
		public void clear(){
			mBitmap = Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
					Bitmap.Config.ARGB_8888);
			if(mCanvas!=null)
				mCanvas.setBitmap(mBitmap);// �������û������൱����ջ���
			if(canclePath!=null&&canclePath.size()>0){
				canclePath.clear();
			}
			if(savePath!=null&&savePath.size()>0){
				savePath.clear();
			}
		//	onDraw(mCanvas);
			invalidate();
		}
		
		//����ͼƬ
		public void saveImage(String path,int mode){

			File saveFile=new File(path);
			if(saveFile.exists())
				saveFile.delete();
			int photoWidth;
			int photoHeight;
			try {
				
				FileOutputStream fileOutputStream=new FileOutputStream(saveFile);
				//�Դ���500��ͼƬѹ��
				if(getWidth()>480||getHeight()>800){
					//����ѹ������
					float scale=getWidth()/480f;
					if(getHeight()/scale<=800){
						photoWidth=480;
						photoHeight=(int)(getHeight()/scale);
					}else{
						scale=getHeight()/800;
						photoHeight=800;
						photoWidth=(int)(getWidth()/scale);
					}
					
				}else{
					photoWidth=getWidth();
					photoHeight=getHeight();
				}
	     		Bitmap bitmap=Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.RGB_565);
				draw(new Canvas(bitmap));
				Bitmap saveBitmap=Bitmap.createScaledBitmap(bitmap,photoWidth,photoHeight, true);
				//ѹ����ԭͼ������50%
				saveBitmap.compress(CompressFormat.PNG, 50, fileOutputStream);
				fileOutputStream.flush();
				fileOutputStream.close();
				if(mode==SAVE_MODE)
					Toast.makeText(getContext(), "�ļ�������"+path,Toast.LENGTH_SHORT).show();
				
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
			}
		}
		
		//���û�ͼ����
		public void setShape(int shape){
			shapeMode=shape;
			
		}
		
		//���浱ǰ��ͼ����
		public  void setCurrentShape(){
			currentShape=getShape();
			currentPenSize=getPenSize();
		}
		//�õ���ͼ����
		public int getShape(){
			return shapeMode;
		}
		//��ȡ��ǰ��ͼ����
		public int getCurrentShape(){
			return currentShape;
		}
		//���û�ͼģʽ
		public void setPaintMode(int ex_paindMode){
			paintMode=ex_paindMode;
			switch (ex_paindMode) {
			case COMMON_MODE:
				setShape(currentShape);
				setPenSize(currentPenSize);
				break;
			case CUT_MODE:
				//�����ͼ����
				//���滭�ʴ�ϸ
				if(!isshapeSave){
						currentShape=getShape();    
						currentPenSize=getPenSize();
						isshapeSave=true;
				}
				setShape(Shape_Type.RECT);
				break;
			case ERASER_MODE:
				if(!isshapeSave){
					currentShape=getShape();
					currentPenSize=getPenSize();
					isshapeSave=true;
				}
				setShape(Shape_Type.FREE);
				break;
			case FILLCOLOR_MODE:
				break;
			case PASTETEXT_MODE:
				//initTextPaint();
				break;
			default:
				break;
			}
		}
		//��ȡ��ǰ��ͼģʽ
		public int getPaintMode(){
			return paintMode;
		}
	
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
	//		measure(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY);
			measure(View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY), 
					View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY));
			mCanvas.drawLine(0, getMeasuredHeight()/2, getMeasuredWidth(),getMeasuredHeight()/2, mPaint);
			invalidate();
		}
		//������ɫģʽ
		public void setFillColor(int color ){
			fillcolor=color;
			paintMode=FILLCOLOR_MODE;
		}
		//���ü�����ɫģʽ
		public void setCutFillColor(int color){
			cutFillColor=color;
		}
		//���ñ���ͼ
		public void setBackImage(int res){
			setImageResource(res);
		}
		
		//���ü���ģʽ
		public void setListentMode(int mode){
			listenMode=mode;
		}

		private void initPaint(){
				if(isFirstDraw){
					mBitmap = Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
							Bitmap.Config.ARGB_8888);
					// ����һ��һ�λ��Ƴ�����ͼ��
					mCanvas = new Canvas(mBitmap);
					mBitmapPaint = new Paint(Paint.DITHER_FLAG);
					mPaint = new Paint();
					mPaint.setAntiAlias(true);
					mPaint.setStyle(Paint.Style.STROKE);
					mPaint.setStrokeJoin(Paint.Join.ROUND);// �������Ե
					mPaint.setStrokeCap(Paint.Cap.ROUND);// ��״
					mPaint.setStrokeWidth(srokeWidth);// ���ʿ��
					mPaint.setColor(color);
					savePath = new ArrayList<DrawPath>();
					canclePath = new ArrayList<DrawPath>();
					isFirstDraw=false;
				}
				mPaint = new Paint();
				mPaint.setAntiAlias(true);
				mPaint.setStyle(Paint.Style.STROKE);
				mPaint.setStrokeJoin(Paint.Join.ROUND);// �������Ե
				mPaint.setStrokeCap(Paint.Cap.ROUND);// ��״
				mPaint.setStrokeWidth(srokeWidth);// ���ʿ��
				mPaint.setColor(color);
			
			if(paintMode==ERASER_MODE){
				mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
				mPaint.setAlpha(0);
	//			mPaint.setColor(Color.RED);
			}
			if(paintMode==CUT_MODE){
				mPaint.setColor(Color.WHITE);
				mPaint.setStrokeWidth(5);
		//		setShape(Shape_Type.RECT);
			}
			
		}
		//����Ҫ���Ƶ�Bitmap
	public void setCopyBitmap(Bitmap ex_bitBitmap){
		copyBitmap=ex_bitBitmap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
	//	Toast.makeText(getContext(), "����onDraw�¼�", Toast.LENGTH_SHORT).show();
		super.onDraw(canvas);
		if(mBitmap!=null)
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		
		if (mPath != null&&paintMode==COMMON_MODE) {
			    canvas.drawPath(mPath, mPaint);
		}
		if(mPath!=null&&paintMode==CUT_MODE){
			 canvas.drawPath(mPath, mPaint);
		}
		if(PaintMainActivity.isLoad){
			PaintMainActivity.drawWidth=getWidth();
			PaintMainActivity.drawHeight=getHeight();
			clear();
			PaintMainActivity.isLoad=false;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
	//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int measureHeight=measureLength(heightMeasureSpec);
		int measureWidth=measureLength(widthMeasureSpec);
		setMeasuredDimension(measureWidth, measureHeight);
	}
	private int measureLength(int length){
		//int specMode=MeasureSpec.getMode(length);
		int specSize=MeasureSpec.getSize(length);
		int result=specSize;
		return result;
	}
	private void touch_start(float x, float y) {
		
		switch(paintMode){
		case FILLCOLOR_MODE:
			FloodFill(mBitmap,new Point((int)x, (int)y), Color.TRANSPARENT, fillcolor);
			break;
		case ERASER_MODE:
//			setShape(Shape_Type.FREE);
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
			break;
		case COPY_MODE:
			mCanvas.drawBitmap(copyBitmap, x-0.5f*copyBitmap.getWidth(),y-0.5f*copyBitmap.getHeight(), mBitmapPaint);
			DrawPath bitmapDrawPath=new DrawPath();
			bitmapDrawPath.bitmap=copyBitmap;
			bitmapDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
			bitmapDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
			bitmapDrawPath.bx=x-0.5f*copyBitmap.getWidth();
			bitmapDrawPath.by=y-0.5f*copyBitmap.getHeight();
			bitmapDrawPath.mode=BITMAP_MODE;
			savePath.add(bitmapDrawPath);
			break;
		case CUT_MODE:
		//	setShape(Shape_Type.RECT);
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
			break;
		case COMMON_MODE:
			switch(shapeMode){
			case Shape_Type.FREE:case Shape_Type.OVAL:
			case Shape_Type.RECT:case Shape_Type.STRAIGIT:
				//�������ó�ʼ������
				isInitDrawBroken=true;
				mPath.moveTo(x, y);
				mX = x;
				mY = y;
				break;
			case Shape_Type.BROKEN:
			
				//��һ�λ���
				if(isInitDrawBroken){
					mPath.addCircle(x, y, 0.5f, Path.Direction.CW);
					 brokenPoint=new BrokenPoint();
					isInitDrawBroken=false;
					brokenPoint.x=x;
					brokenPoint.y=y;
				//	countPoint++;
				}
				else{
					
					mPath.moveTo(brokenPoint.x, brokenPoint.y);
					mPath.lineTo(x, y);
					brokenPoint.x=x;
					brokenPoint.y=y;
				}
				break;
			case Shape_Type.MUTIL:
				//�������ó�ʼ������
				isInitDrawBroken=true;
				//��ȡ�����γ�ʼ������
				mulX=x;
				mulY=y;
				break;
			}
			break;
		case PASTETEXT_MODE:
			initTextPaint();
			//textPaint.setTextSize(20);
			//float paintTextSize=textPaint.measureText(paintText);
			float textX=x;
			float textY=y;
			String drawText=new String(paintText);
			mCanvas.drawText(paintText,textX, textY, textPaint);
			DrawPath textDrawPath=new DrawPath();
			textDrawPath.mode=DRAW_TEXT_MODE;
			textDrawPath.bx=textX;
			textDrawPath.by=textY;
			textDrawPath.paint=textPaint;
			textDrawPath.drawText=drawText;
			savePath.add(textDrawPath);
			break;
		default:
				break;		
		}
		
	
	}

	private void touch_move(float x, float y) {
		switch(shapeMode){
		case Shape_Type.STRAIGIT:
				mPath.reset();
				mPath.moveTo(mX, mY);
				mPath.lineTo(x, y);
			break;
		case Shape_Type.BROKEN:
			break;
		case Shape_Type.RECT:
				mPath.reset();
				if(x>mX&&y>mY)
					mPath.addRect(mX, mY, x, y, Path.Direction.CW);
				else if(x>mX&&y<mY)
					mPath.addRect(mX, y,x, mY, Path.Direction.CW);
				else if(x<mX&&y<mY)
					mPath.addRect(x,y, mX, mY,Path.Direction.CW);
				else
					mPath.addRect(x,mY,mX,y,Path.Direction.CW);
				mPath.moveTo(mX, mY);
			break;
		case Shape_Type.MUTIL:
			mPath.reset();
			float r=(float)Math.sqrt((mulX-x)*(mulX-x)+(mulY-y)*(mulY-y));//��ȡ�뾶
			mPath.moveTo(mulX+r, mulY);
			mPath.lineTo((float)(mulX+0.5*r), (float)(mulY+0.86603*r));
			mPath.lineTo((float)(mulX-0.5*r), (float)(mulY+0.86603*r));
			mPath.lineTo((float)(mulX-r), mulY);
			mPath.lineTo((float)(mulX-0.5*r), (float)(mulY-0.86603*r));
			mPath.lineTo((float)(mulX+0.5*r), (float)(mulY-0.86603*r));
			mPath.close();
			break;
		case Shape_Type.OVAL:
				mPath.reset();
				mPath.addOval(new RectF(mX, mY, x, y), Path.Direction.CW);
				mPath.moveTo(mX, mY);
			break;
		case Shape_Type.FREE:
			if(paintMode==ERASER_MODE)
				mCanvas.drawPath(mPath, mPaint);
			float dx = Math.abs(x - mX);
			float dy = Math.abs(mY - y);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// ��x1,y1��x2,y2��һ�����������ߣ���ƽ��(ֱ����mPath.lineToҲ�ǿ��Ե�)
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
		
			}
			break;
		default:
				break;
		}
	
	}

	private void touch_up() {
		switch(paintMode){
		case COMMON_MODE:
			switch(shapeMode){
			case Shape_Type.FREE:case Shape_Type.OVAL:case Shape_Type.RECT:
			case Shape_Type.STRAIGIT:
				mPath.lineTo(mX, mY);
				mCanvas.drawPath(mPath, mPaint);
				// ��һ��������·����������(�൱����ջ����)
				dp.path = mPath;
				dp.paint = mPaint;
				savePath.add(dp);
				mPath = null;// �����ÿ�
				break;
			case Shape_Type.BROKEN:
				mCanvas.drawPath(mPath, mPaint);
				// ��һ��������·����������(�൱����ջ����)
				dp.path = mPath;
				dp.paint = mPaint;
				savePath.add(dp);
				mPath = null;// �����ÿ�
				break;
			case Shape_Type.MUTIL:
				mCanvas.drawPath(mPath, mPaint);
				// ��һ��������·����������(�൱����ջ����)
				dp.path = mPath;
				dp.paint = mPaint;
				savePath.add(dp);
				mPath = null;// �����ÿ�
				break;
			default:
					break;
			}
			break;
		case ERASER_MODE:
			mPath.lineTo(mX, mY);
			mCanvas.drawPath(mPath, mPaint);
			// ��һ��������·����������(�൱����ջ����)
			dp.path = mPath;
			dp.paint = mPaint;
			savePath.add(dp);
			mPath = null;// �����ÿ�
			break;
		case CUT_MODE:
			new AlertDialog.Builder(getContext())
			.setTitle("������ʽ")
			.setItems(new String[]{"����","����","����","ɾ��","��ת","��ɫ"}, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(mX==endX||mY==endY)
						return;
					if(PaintMainActivity.isReduce){
						if(getWidth()*getScaleX()<endX)
							endX=getWidth()*getScaleX();
						if(getHeight()*getScaleY()<endY)
							endY=getHeight()*getScaleY();
					} 
					switch(which){
					case 0:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int)mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						paintMode=COPY_MODE;
						break;
					case 1:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
						bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						savePath.add(bmpDrawPath);
						translatePaint=new Paint();
						translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
						mCanvas.drawRect(mX, mY, endX, endY, translatePaint);
						paintMode=COPY_MODE;
						invalidate();
						break;
					case 2:
					
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						bmpDrawPath.oreignBitmapTyep=BitmapType.ZOOM_BITMAP;
						bmpDrawPath.bitmapType=BitmapType.ZOOM_BITMAP;
						savePath.add(bmpDrawPath);
						zoomSeekBar=(SeekBar)PaintMainActivity.MActivity.findViewById(R.id.zoom_seekbar);
						zoomSeekBar.setVisibility(VISIBLE);
						zoomSeekBar.setProgress(50);
						zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							
							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								translatePaint=new Paint();
								translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
								int roatProcess=seekBar.getProgress();
								if(roatProcess!=0)
									widthScale=heightScale=roatProcess/50f;
								matrix=new Matrix();
								matrix.postScale(widthScale, heightScale);
								if(isInitZoom){
								mCanvas.drawRect(mX, mY, mX+copyBitmap.getWidth(),mY+copyBitmap.getHeight(),translatePaint);
								resizeBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
								mCanvas.drawBitmap(resizeBitmap, mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth(),
										mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight(), mBitmapPaint);
								bmpDrawPath=new DrawPath();
								bmpDrawPath.bitmap=resizeBitmap;
								bmpDrawPath.bx=mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth();
								bmpDrawPath.by=mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight();
								bmpDrawPath.mode=BITMAP_MODE;
								bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
								savePath.add(bmpDrawPath);
								isInitZoom=false;
								invalidate();
							}
							else{
								
								lastDrawPath= savePath.get(savePath.size()-1);
								lastDrawPath.oreignBitmapTyep=BitmapType.ZOOM_BITMAP;
								lastDrawPath.bitmapType=BitmapType.ZOOM_BITMAP;
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by,
										lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
										lastDrawPath.by+lastDrawPath.bitmap.getHeight(), translatePaint);
								resizeBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
								mCanvas.drawBitmap(resizeBitmap, mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth(),
										mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight(), mBitmapPaint);
								bmpDrawPath=new DrawPath();
								bmpDrawPath.bitmap=resizeBitmap;
								bmpDrawPath.bx=mX+0.5f*copyBitmap.getWidth()-0.5f*resizeBitmap.getWidth();
								bmpDrawPath.by=mY+0.5f*copyBitmap.getHeight()-0.5f*resizeBitmap.getHeight();
								bmpDrawPath.mode=BITMAP_MODE;
								bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
								savePath.add(bmpDrawPath);
								invalidate();
							}
								
							}
							
							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								
					
							}
							
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress,
									boolean fromUser) {
								// TODO Auto-generated method stub
					
				
							}
						});
				//		paintMode=COPY_MODE;
						//��һ���ž���
						//�������ű���
				//		mCanvas.scale(2f, 2f);
						break;
					case 3:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						bmpDrawPath.oreignBitmapTyep=BitmapType.DELETE_BITMAP;
						bmpDrawPath.bitmapType=BitmapType.DELETE_BITMAP;
						savePath.add(bmpDrawPath);
						translatePaint=new Paint();
						translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
						rectF=new RectF(mX, mY, endX, endY);
						mCanvas.drawRect(rectF, translatePaint);
						invalidate();
						break;
					case 4:
						copyBitmap=Bitmap.createBitmap
						(mBitmap, (int)mX,(int) mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
						bmpDrawPath=new DrawPath();
						bmpDrawPath.bitmap=copyBitmap;
						bmpDrawPath.oreignBitmapTyep=BitmapType.ROAT_BITMAP;
						bmpDrawPath.bitmapType=BitmapType.ROAT_BITMAP;
						bmpDrawPath.bx=mX;
						bmpDrawPath.by=mY;
						bmpDrawPath.mode=BITMAP_MODE;
						savePath.add(bmpDrawPath);
					    roatSeekBar=(SeekBar)PaintMainActivity.MActivity.findViewById(R.id.roat_seekbar);
					    roatSeekBar.setVisibility(VISIBLE);
					    roatSeekBar.setProgress(0);
					    roatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
							
							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								//��ǰһ��ͼƬ����ΪRoatBitmap(��ת����)
								lastDrawPath=savePath.get(savePath.size()-1);
								lastDrawPath.oreignBitmapTyep=BitmapType.ROAT_BITMAP;
								lastDrawPath.bitmapType=BitmapType.ROAT_BITMAP;
								bmpDrawPath=new DrawPath();
								bmpDrawPath.bitmap=roatBitmap;
								bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
								bmpDrawPath.bx=mX;
								bmpDrawPath.by=mY;
								bmpDrawPath.mode=BITMAP_MODE;
								savePath.add(bmpDrawPath);
							
							}
							
							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								translatePaint=new Paint();
								translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
								mCanvas.drawRect(mX, mY, endX, endY, translatePaint);
								invalidate();
								
							}
							
							@Override
							public void onProgressChanged(SeekBar seekBar, int progress,
									boolean fromUser) {
								// TODO Auto-generated method stub
								//������ת����
								matrix=new Matrix();
								matrix.postRotate(progress, (mX+copyBitmap.getWidth())/2,(mY+copyBitmap.getHeight())/2);
								translatePaint=new Paint();
								translatePaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_OUT));
								//�Ƿ��ʼ��ת
								if(isInitRote){
									roatBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
									mCanvas.drawBitmap(roatBitmap, mX, mY, mBitmapPaint);
									isInitRote=false;
									invalidate();
								}
								else{
									mCanvas.drawRect(mX, mY, mX+roatBitmap.getWidth(), mY+roatBitmap.getHeight(), translatePaint);
									roatBitmap=Bitmap.createBitmap(copyBitmap,0, 0, copyBitmap.getWidth(), copyBitmap.getHeight(),matrix, true);
									mCanvas.drawBitmap(roatBitmap, mX, mY, mBitmapPaint);
									invalidate();
								}
							}
						});
					//	matrix=new Matrix();
					//	matrix.postRotate(Integer.parseInt(roatEditText.getText().toString()), mX+copyBitmap.getWidth()/2, mY+copyBitmap.getHeight()/2);
						
						break;
					case 5:
						 //����������ɫ�Ի���
					 final Dialog colorDialog=new Dialog(getContext());
						colorDialog.setTitle("��ɫ��ɫ");
						colorDialog.setContentView(R.layout.dialog_for_selectcolor);
						final ColorPicker colorPicker=(ColorPicker)colorDialog.findViewById(R.id.picker);
						colorPicker.setColor(getColor());
						final OpacityBar opacityBar=(OpacityBar)colorDialog.findViewById(R.id.opacitybar);
						colorPicker.addOpacityBar(opacityBar);
						opacityBar.setOpacity(((PaintMainActivity)getContext()).getOpacity());
						Button colorOKButton=(Button)colorDialog.findViewById(R.id.colorok);
						Button colorCancelButton=(Button)colorDialog.findViewById(R.id.colorcancel);
						colorOKButton.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								setCutFillColor(colorPicker.getColor());
								copyBitmap=Bitmap.createBitmap
										(mBitmap, (int)mX,(int)mY, (int)Math.abs(endX-mX),(int) Math.abs(endY-mY));
										tempCanvas=new Canvas(copyBitmap);
										tempCanvas.drawColor(cutFillColor);
										mCanvas.drawBitmap(copyBitmap, (int)mX, (int)mY, mBitmapPaint);
										DrawPath drawPath=new DrawPath();
										drawPath.bitmap=copyBitmap;
										drawPath.bx=mX;
										drawPath.by=mY;
										drawPath.mode=BITMAP_MODE;
										savePath.add(drawPath);
										invalidate();
										
								colorDialog.dismiss();
							}
						});
						colorCancelButton.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								colorDialog.dismiss();
							}
						});
					//	colorDialog.findViewById(R.id.sv)
						colorDialog.show();
						 
						break;
					default:
							break;
					}
					dialog.dismiss();
				}
			})
			.show();
			mPath.reset();
			mPath=null;
			break;
		case COPY_MODE:
			mPath=null;
			break;
		default:
				break;
		}
		
	}
	
	public int undo() {
		
		mBitmap =Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
				Bitmap.Config.ARGB_8888);
		if(mCanvas!=null)
			mCanvas.setBitmap(mBitmap);// �������û������൱����ջ���
		// ��ջ������������ͼƬ�б����Ļ�����ʹ����������³�ʼ���ķ������ø÷����Ὣ������յ���
		if (savePath != null && savePath.size() > 0) {
			
			DrawPath dPath =savePath.get(savePath.size() - 1);
			canclePath.add(dPath);
			// �Ƴ����һ��path,�൱�ڳ�ջ����
			savePath.remove(savePath.size() - 1);
			if(savePath.size()>0){
				if(savePath.get(savePath.size()-1).bitmapType==BitmapType.ZOOM_BITMAP){
					savePath.get(savePath.size()-1).bitmapType=BitmapType.COMMON_BITMAP;
			//		cancelType=BitmapType.ZOOM_BITMAP;
				}
				else if(savePath.get(savePath.size()-1).bitmapType==BitmapType.ROAT_BITMAP){
					savePath.get(savePath.size()-1).bitmapType=BitmapType.COMMON_BITMAP;
			//		cancelType=BitmapType.ROAT_BITMAP;
				}
			}
			
			Iterator<DrawPath> iter = savePath.iterator();
			while(iter.hasNext()){
				DrawPath drawPath = iter.next();
				if(drawPath.mode==DRAW_MODE){
					Log.i("mCanvas is null?",""+(mCanvas==null));
					Log.i("drawPath.path is null?",""+(drawPath.path==null));
					Log.i("drawPath.paint is null?",""+(drawPath.paint==null));
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}else if(drawPath.mode==DRAW_TEXT_MODE){
					mCanvas.drawText(drawPath.drawText, drawPath.bx, drawPath.by, drawPath.paint);
				}
				else{
					Paint drawPaint=new Paint();
					drawPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
					if(drawPath.bitmapType==BitmapType.DELETE_BITMAP)
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
					//�״γ���ZOOM_BITMAP
					else if(drawPath.bitmapType==BitmapType.ZOOM_BITMAP){
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.bitmapType==BitmapType.ZOOM_BITMAP){
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
					//			mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
								invalidate();
								lastDrawPath=drawPath;
								drawPath=iter.next();
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
					}
					//��ת֮���λͼ
					else if(drawPath.bitmapType==BitmapType.ROAT_BITMAP){
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.bitmapType==BitmapType.ROAT_BITMAP){
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
								invalidate();
								lastDrawPath=drawPath;
								drawPath=iter.next();
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
					}
					else
					{
					//	Toast.makeText(getContext(), ""+savePath.size(), Toast.LENGTH_SHORT).show();
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by,mBitmapPaint);
					//	invalidate();
					}
						
						
				}
			}
			invalidate();// ˢ��
			
		}else{
			return -1;
		}
		return savePath.size();
	}
	/**
	 * �����ĺ���˼����ǽ�������·�����浽����һ����������(ջ)�� Ȼ���redo�ļ�������ȡ����˶��� ���ڻ������漴�ɡ�
	 */
	public int redo() {
		// ��������㶮�˵Ļ����Ǿ����������ɡ�
		if(canclePath.size()<1)
			return canclePath.size();
		
		mBitmap = Bitmap.createBitmap(PaintMainActivity.drawWidth, PaintMainActivity.drawHeight,
				Bitmap.Config.ARGB_8888);
		mCanvas.setBitmap(mBitmap);// �������û������൱����ջ���
		// ��ջ������������ͼƬ�б����Ļ�����ʹ����������³�ʼ���ķ������ø÷����Ὣ������յ���
		if (canclePath != null && canclePath.size() > 0) {
			// �Ƴ����һ��path,�൱�ڳ�ջ����
			DrawPath  dPath = canclePath.get(canclePath.size() - 1);
			savePath.add(dPath);
	//		Toast.makeText(getContext(), ""+canclePath.size() +"   "+savePath.size(), Toast.LENGTH_SHORT).show();
			canclePath.remove(canclePath.size() - 1);
			Iterator<DrawPath> iter = savePath.iterator();
			while(iter.hasNext()){
				DrawPath drawPath = iter.next();
				if(drawPath.mode==DRAW_MODE){
					mCanvas.drawPath(drawPath.path, drawPath.paint);
				}else if(drawPath.mode==DRAW_TEXT_MODE){
					mCanvas.drawText(drawPath.drawText, drawPath.bx, drawPath.by, drawPath.paint);
				}
				else{
					
					Paint drawPaint=new Paint();
					drawPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
					if(drawPath.bitmapType==BitmapType.DELETE_BITMAP)
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
					//�״γ���ZOOM_BITMAP
					else if(drawPath.oreignBitmapTyep==BitmapType.ZOOM_BITMAP){
						drawPath.bitmapType=BitmapType.ZOOM_BITMAP;
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.oreignBitmapTyep==BitmapType.ZOOM_BITMAP){
								drawPath.bitmapType=BitmapType.ZOOM_BITMAP;
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
					//			mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
								invalidate();
								lastDrawPath=drawPath;
								if(iter.hasNext())
									drawPath=iter.next();
								else 
									break;
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
						
					}
					//��ת֮���λͼ
					else if(drawPath.oreignBitmapTyep==BitmapType.ROAT_BITMAP){
						drawPath.bitmapType=BitmapType.ROAT_BITMAP;
						mCanvas.drawRect(drawPath.bx, drawPath.by, drawPath.bx+drawPath.bitmap.getWidth(),
								drawPath.by+drawPath.bitmap.getHeight(), drawPaint);
						invalidate();
						lastDrawPath=drawPath;
						if(iter.hasNext())
							drawPath=iter.next();
						while(drawPath.oreignBitmapTyep==BitmapType.ROAT_BITMAP){
								drawPath.bitmapType=BitmapType.ROAT_BITMAP;
								mCanvas.drawRect(lastDrawPath.bx, lastDrawPath.by, lastDrawPath.bx+lastDrawPath.bitmap.getWidth(),
								lastDrawPath.by+lastDrawPath.bitmap.getHeight(), drawPaint);
								invalidate();
								lastDrawPath=drawPath;
								if(iter.hasNext())
									drawPath=iter.next();
								else 
									break;
							}
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by, mBitmapPaint);
						invalidate();
					}
					else
						mCanvas.drawBitmap(drawPath.bitmap, drawPath.bx, drawPath.by,mBitmapPaint);
						
				}
			}
			invalidate();// ˢ��
			
		}else{
			return -1;
		}
		
		invalidate();// ˢ��
		//}
		return canclePath.size();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//	getParent().requestDisallowInterceptTouchEvent(true);
		float x = event.getX();
		float y = event.getY();
		//��߻�������
		switch (event.getAction()) {
		
		case MotionEvent.ACTION_DOWN:

			if(zoomSeekBar!=null&&zoomSeekBar.getVisibility()==VISIBLE){
					zoomSeekBar.setVisibility(INVISIBLE);
					//isInitInZoom=true;
			}
			if(roatSeekBar!=null&&roatSeekBar.getVisibility()==VISIBLE)
			{
				roatSeekBar.setVisibility(INVISIBLE);
				isInitRote=true;
			}
			if(PaintMainActivity.MActivity.getZoomCanvans().getVisibility()==View.VISIBLE)
				PaintMainActivity.MActivity.getZoomCanvans().setVisibility(View.INVISIBLE);	
			if(listenMode==PAINT_MODE){
				initPaint();
				//������һ������,ǰ������
				canclePath = new ArrayList<DrawPath>();
				// ÿ��down��ȥ����newһ��Path
				mPath = new Path();
				// ÿһ�μ�¼��·�������ǲ�һ����
				dp = new DrawPath();
				dp.mode=DRAW_MODE;
				touch_start(x, y);
				invalidate();
				
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(listenMode==PAINT_MODE){
				touch_move(x, y);
				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			if(listenMode==PAINT_MODE){
				endX=event.getX();
				endY=event.getY();
				if(mPath!=null)
					touch_up();
				invalidate();
			}
			else{
				listenMode=PAINT_MODE;
		}
			break;
			}
		return true;
	}
	//��ʼ�����ֻ��ƻ���
	public void initTextPaint(){
		textPaint=new Paint();
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setStrokeWidth(getPenSize());
		textPaint.setTextSize(20*getPenSize());
		textPaint.setAntiAlias(true);
		textPaint.setStrokeCap(Paint.Cap.ROUND);
		textPaint.setStrokeJoin(Paint.Join.ROUND);
	//	textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(getColor());
		//textPaint.
	}
	
	public void setPaintText(String paintText){
		this.paintText=paintText;
	}
	/*Flood Fill Algorithm*/
	private void FloodFill(Bitmap bmp, Point pt, int targetColor, int replacementColor) 
	{
	    FloodFillAlgorithm floodFillAlgorithm=new FloodFillAlgorithm(bmp);
	    floodFillAlgorithm.floodFillScanLineWithStack(pt.x, pt.y, replacementColor, targetColor);
	    tempBitmap=Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight());
	    //����ɫ֮���Bitmap����savePath��
	    if(floodFillAlgorithm.isFill()){
	    	bmpDrawPath=new DrawPath();
	  	    bmpDrawPath.mode=BITMAP_MODE;
	  	    bmpDrawPath.bitmap=tempBitmap;
	  	    bmpDrawPath.bitmapType=BitmapType.COMMON_BITMAP;
	  	    bmpDrawPath.oreignBitmapTyep=BitmapType.COMMON_BITMAP;
	  	    bmpDrawPath.bx=0;
	  	    bmpDrawPath.by=0;
	  	    savePath.add(bmpDrawPath);
	    }
	  
	}
	
	//FloodFill��
	class FloodFillAlgorithm {

		private Bitmap fillBitmap;
	//	private int[] inPixels;
		private int width;
		private int height;
		private boolean isFillColor=true;
		// 	stack data structure
		private int maxStackSize = 500; // will be increased as needed
		private int[] xstack = new int[maxStackSize];
		private int[] ystack = new int[maxStackSize];
		private int stackSize;

		public FloodFillAlgorithm(Bitmap fillBitmap) {
			this.fillBitmap = fillBitmap;
			width = fillBitmap.getWidth();
	        height = fillBitmap.getHeight();
	   //     inPixels = new int[width*height];
	   //     getColor(point.x, point.y);
		}

		public int getColor(int x, int y)
		{
			
			return fillBitmap.getPixel(x, y);
		}
		
		public void setColor(int x, int y, int newColor)
		{
			fillBitmap.setPixel(x, y, newColor);
		}
		/**
		 * �ж��Ƿ�������Ƿ����
		 * @return 
		 */
		public boolean isFill(){
			return isFillColor;
		}
		public void floodFillScanLineWithStack(int x, int y, int newColor, int oldColor)
		{
			if(oldColor!=Color.TRANSPARENT){
				isFillColor=false;
				return ;
			}
				
			
			if(oldColor == newColor) {
				System.out.println("do nothing !!!, filled area!!");
				return;
			}
		    emptyStack();
		    
		    int y1; 
		    boolean spanLeft, spanRight;
		    push(x, y);
		    
		    while(true)
		    {    
		    	x = popx();
		    	if(x == -1) return;
		    	y = popy();
		        y1 = y;
		        while(y1 >= 0 && getColor(x, y1) == oldColor) y1--; // go to line top/bottom
		        y1++; // start from line starting point pixel
		        spanLeft = spanRight = false;
		        while(y1 < height && getColor(x, y1) == oldColor)
		        {
		        	setColor(x, y1, newColor);
		            if(!spanLeft && x > 0 && getColor(x - 1, y1) == oldColor)// just keep left line once in the stack
		            {
		                push(x - 1, y1);
		                spanLeft = true;
		            }
		            else if(spanLeft && x > 0 && getColor(x - 1, y1) != oldColor)
		            {
		                spanLeft = false;
		            }
		            if(!spanRight && x < width - 1 && getColor(x + 1, y1) == oldColor) // just keep right line once in the stack
		            {
		                push(x + 1, y1);
		                spanRight = true;
		            }
		            else if(spanRight && x < width - 1 && getColor(x + 1, y1) != oldColor)
		            {
		                spanRight = false;
		            } 
		            y1++;
		        }
		    }
			
		}
		
		private void emptyStack() {
			while(popx() != - 1) {
				popy();
			}
			stackSize = 0;
		}

		final void push(int x, int y) {
			stackSize++;
			if (stackSize==maxStackSize) {
				int[] newXStack = new int[maxStackSize*2];
				int[] newYStack = new int[maxStackSize*2];
				System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
				System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
				xstack = newXStack;
				ystack = newYStack;
				maxStackSize *= 2;
			}
			xstack[stackSize-1] = x;
			ystack[stackSize-1] = y;
		}
		
		final int popx() {
			if (stackSize==0)
				return -1;
			else
	            return xstack[stackSize-1];
		}

		final int popy() {
	        int value = ystack[stackSize-1];
	        stackSize--;
	        return value;
		}

	}

}
