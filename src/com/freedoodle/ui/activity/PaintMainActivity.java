package com.freedoodle.ui.activity;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.freedoodle.adapter.PastePhotoAdapter;
import com.freedoodle.config.Config;
import com.freedoodle.config.Const;
import com.freedoodle.data.Shape_Type;
import com.freedoodle.engine.Background;
import com.freedoodle.engine.DrawView;
import com.freedoodle.ui.ColorPicker;
import com.freedoodle.ui.DisplayPenSizeView;
import com.freedoodle.ui.OpacityBar;
import com.gfinsert.YManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import cn.edu.fjnu.utils.DownloadUtils;
import cn.edu.fjnu.utils.OPUtils;
import cn.edu.fjnu.utils.ResourceUtils;
import cn.fjnu.edu.paint.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

@SuppressLint("SimpleDateFormat")
public class PaintMainActivity extends Activity {

	private static final String TAG = "PaintMainActivity";
	public static final int SHARE_MODE = 0;// ����ģʽ
	public static final int SAVE_MODE = 1;// ����ģʽ
	public static final int CUT_MODE = 2;// ͼƬ����ģʽ
	public static final int BACK_MODE = 3;// ����ͼģʽ
	public static int saveType = SAVE_MODE;// ͼƬ��������
	public static String photopath;// ��Ƭ֮���ȡ��·��
	public static PaintMainActivity MActivity;// �õ�MainAcitivity������
	public static int[] backID = new int[46];
	public static int[] bigBackId = new int[46];
	public static boolean isReduce = false;// �жϻ����Ƿ���С
	public static boolean isLoad = true;// �Ƿ��������
	public boolean isMeasure = true;// �Ƿ���β�����Ļ�Ĵ�С
	private final int DEFAULT_WIDTH = 480;
	private final int DEFAULT_HEIGHT = 800;
	private int opacity = 0xff;// ������ɫ͸����
	private DrawView canvansImageView;// ���ڻ�ͼ��imageview
	private ImageView main_shapeImageView;// ���ڻ��ʴ�ϸ��ʾ
	private ImageView main_clearImageView;// ��ջ���
	private ImageView main_eraserImageView;// ��Ƥ��
	private ImageView main_backgroundImageView;// ����ͼ
	private ImageView main_drawtypeImageView;// ��ͼ����
	private ImageView main_newcreateImageView;// �½�
	private ImageView main_penImageView;// ����
	private ImageView main_colorImageView;// ������ɫ
	private SeekBar penSeekBar;// �϶������ƻ��ʴ�ϸ
	private TextView penTextView;// ָʾ��ǰ���ʴ�ϸ��ֵ
	private DisplayPenSizeView processImageView;// ָʾ��ǰ���ʴ�ϸ
	private int penSize;// ��ʾ�Ļ��ʴ�ϸ��С
	private Button process_okButton;// ���ʴ�ϸ���õ�ȷ����ť
	private Button process_cancelButton;// ���ʴ�ϸ���õ�ȡ����ť
	private Dialog setsizeDialog;// ���û��ʴ�ϸ�ĶԻ���
	private View mainView;// RelativeLayout����
	private ZoomControls zoomCanvas;// ���ƻ�����С
	private float oreignWidthScalex;
	private float oreignHeightScalex;
	private int penProcess = 10;// ���ʴ�С���õĽ�����ֵ
	private int orignPenProcess;
	public static int drawWidth;// �滭�Ŀ��
	public static int drawHeight;// �滭�ĸ߶�
	public static int screenWidth;// ��Ļ���
	public static int screenHeight;// ��Ļ�߶�
	public static DrawView canvasView;// ���ڻ��Ƶ�DrawView
	private int createWidth;
	private int createHeight;
	private android.widget.LinearLayout.LayoutParams createLayoutParams;
	private boolean isBlackColor=false;
	//�����Ƶ�����
	private String paintText=null;
	//���ڻ������ֵĻ���
	private TextPaint textPaint=null;
	/**�����ʾ*/
	private YManager myPPCC;
	/**������ʾ*/
	private String updateTip = "";
	/**���µİ汾*/
	private String updateVersion = "";
	/**�°汾����·��*/
	private String downloadPath = "";
	private  Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg.what==1){
				myPPCC.showInsert(-1, -1, null, 1);
				
				
			}else if(msg.what==2){
				myPPCC.showGfSlider();
				myPPCC.enableGfBackground();
			}else if(msg.what==3){
				//��ȡ��ǰʱ��,�������װ����ʱ��Ա�,5��֮��Ż����
				long currentTime = new Date().getTime();
				long installTime = Long.parseLong(OPUtils.getValFromSharedpreferences(Const.Key.APP_INSTALL_TIME));
				if(installTime - currentTime<5*Const.ONE_DAY_MILL){
					
					return ;
				}
				
				String updateTipResult = OPUtils.getValFromSharedpreferences(Const.Key.UPDATE_TIP);
				if("true".equals(updateTipResult)){
					
					return ;
				}
				//if(OPUtils.isEmpty(updateTipResult))
				//��ʾ�汾����
				new AlertDialog.Builder(PaintMainActivity.this)
				.setTitle("�汾����v"+updateVersion).setMessage(updateTip)
				.setPositiveButton("��������",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						File downloadDirFile = new File(Environment.getExternalStorageDirectory(),"Paint/download");
						if(!downloadDirFile.exists())
							downloadDirFile.mkdirs();
						File nameFile = new File(downloadDirFile,"Ϳѻ��ͼv"+updateVersion+".apk");
						try {
							if(!nameFile.exists())
								nameFile.createNewFile();
							
						} catch (Exception e) {
							
							Log.i(TAG,""+e);
						}
						
						Log.i(TAG,"����·��:"+downloadPath);
						long downloadId = DownloadUtils.downloadFile(downloadPath,DownloadManager.Request.VISIBILITY_VISIBLE,ResourceUtils.getString(R.string.app_name)+"v"+updateVersion,"�汾����", nameFile);
						OPUtils.saveValToSharedpreferences(Const.Key.DOWNLOAD_ID,""+downloadId);
					}
				}).setNegativeButton("ȡ��",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						dialog.dismiss();
						
					}
				}).setNeutralButton("������ʾ", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						OPUtils.saveValToSharedpreferences(Const.Key.UPDATE_TIP, "true");
						dialog.dismiss();
						
					}
				}).show();
				
				
				
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_paint);
		initAD();
		init();
		initMainImage();
		initIMageLoader();
		updateApp();
		canvansImageView.setImageResource(R.drawable.app_rm);
	}
	
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stubei
		super.onWindowFocusChanged(hasFocus);
		if (isLoad) {
			drawWidth = mainView.getWidth();
			drawHeight = mainView.getHeight();
			canvansImageView
					.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
							mainView.getWidth(), mainView.getHeight()));
			canvansImageView.setImageResource(R.drawable.whitebackground);
			isLoad = false;
			if (isMeasure) {
				screenWidth = mainView.getWidth();
				screenHeight = mainView.getHeight();
				isMeasure = false;
			}
		}

	}

	public void init() {
		MActivity = PaintMainActivity.this;
		// ����Ŀ¼
		String saveDir = Environment.getExternalStorageDirectory()
				+ "/drawphoto";
		File saveDirFile = new File(saveDir);
		if (!saveDirFile.exists())
			saveDirFile.mkdirs();
		for (int i = 0; i < 46; i++) {
			int resID = getResources().getIdentifier("b" + i, "drawable",
					getPackageName());
			if (resID != 0) {
				backID[i] = resID;
			}
			resID = getResources().getIdentifier("b" + i, "drawable",
					getPackageName());
			if (resID != 0) {
				bigBackId[i] = resID;
			}

		}
		canvansImageView = (DrawView) findViewById(R.id.img_canvans);
		canvasView = canvansImageView;
		mainView = findViewById(R.id.rlay);
		zoomCanvas = (ZoomControls) findViewById(R.id.zoom_control);
		zoomCanvas.setVisibility(View.INVISIBLE);
		oreignWidthScalex = canvansImageView.getScaleX();
		oreignHeightScalex = canvansImageView.getScaleY();
		// �Ŵ󻭲�����
		zoomCanvas.setOnZoomInClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float widthScalex = canvansImageView.getScaleX();
				float heightScalex = canvansImageView.getScaleY();
				if (widthScalex + 0.2 < 5 && heightScalex + 0.2 < 5) {
					canvansImageView.setScaleX((float) (widthScalex + 0.2));
					canvansImageView.setScaleY((float) (heightScalex + 0.2));
				}
				if (oreignHeightScalex < heightScalex
						|| oreignWidthScalex < widthScalex) {
					isReduce = false;
				}
			}
		});
		// ��С��������
		zoomCanvas.setOnZoomOutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float widthScalex = canvansImageView.getScaleX();
				float heightScalex = canvansImageView.getScaleY();
				if (widthScalex - 0.2 > 0 && heightScalex - 0.2 > 0) {
					canvansImageView.setScaleX((float) (widthScalex - 0.2));
					canvansImageView.setScaleY((float) (heightScalex - 0.2));
				}
				if (oreignHeightScalex > heightScalex
						|| oreignWidthScalex > widthScalex) {
					isReduce = true;
				}
			}
		});

	}

	public void initIMageLoader() {

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplication().getBaseContext())
				.threadPoolSize(3)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(1500000)
				// 1.5 Mb
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.enableLogging() // Not necessary in common
				.build();
		ImageLoader.getInstance().init(config);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (RESULT_OK == resultCode) {
			if (requestCode == 1) {
				try {

					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(photopath, options);
					int scaleX, scaleY, imageWidth, imageHeight;
					imageWidth = options.outWidth;
					imageHeight = options.outHeight;
					scaleX = imageWidth / DEFAULT_WIDTH;
					scaleY = imageHeight / DEFAULT_HEIGHT;
					options.inSampleSize = Math.max(scaleX, scaleY);
					options.inJustDecodeBounds = false;
					options.inPurgeable=true;
					// options.inPurgeable=true;
					Bitmap btp = BitmapFactory.decodeFile(photopath, options);
					canvansImageView.setImageBitmap(btp);
				} catch (Exception e) {
					Toast.makeText(this, "��������,����Լ���...", Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				}

			} else if (requestCode == 2) {
				try {
					Uri uri = data.getData();
					String[] projStrings = { MediaStore.Images.Media.DATA };
					Cursor cursor = managedQuery(uri, projStrings, null, null,
							null);
					int cloum_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String pathString = cursor.getString(cloum_index);
					/* cursor.close(); */
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(pathString, options);
					int imageWidth, imageHeight;
					imageWidth = options.outWidth;
					imageHeight = options.outHeight;
					int scaleX, scaleY;
					scaleX = imageWidth / DEFAULT_WIDTH;
					scaleY = imageHeight / DEFAULT_HEIGHT;
					options.inSampleSize = Math.max(scaleX, scaleY);
					options.inJustDecodeBounds = false;
					Bitmap bm = BitmapFactory.decodeFile(pathString, options);
					canvansImageView.setImageBitmap(bm);
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(this, "��������,����Լ���...", Toast.LENGTH_SHORT)
							.show();
					e.printStackTrace();
				}

				// startPhotoZoom(uri);

			} else if (requestCode == 3) {
				// startPhotoZoom �������������ʱ��ᵽ�������֧ �ü�������ʾ��imageView����
				if (data != null) {
					Bundle extras = data.getExtras();
					// ���ʵ�ʼ��õ������bitmapͼ��
					Bitmap thePic = extras.getParcelable("data");
					// ���imageview�ؼ�������

					// ��imageview�ؼ�����ʾͼƬ
					// backgroundImageView.setImageBitmap(thePic);
					// MainActivity.this.view.pickerBackground(thePic);
					if (saveType == CUT_MODE) {

						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy_MM_dd_kk_mm_ss");
						String date = formatter.format(new java.util.Date());
						String pathString = Environment
								.getExternalStorageDirectory()
								+ "/drawphoto/"
								+ date + ".png";
						// String
						// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
						try {
							FileOutputStream fileOutputStream = new FileOutputStream(
									pathString);
							thePic.compress(CompressFormat.PNG, 90,
									fileOutputStream);
							fileOutputStream.flush();
							fileOutputStream.close();
							Toast.makeText(this, "�ļ�������" + pathString,
									Toast.LENGTH_SHORT).show();

						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(this, "�����ļ�����", Toast.LENGTH_SHORT)
									.show();
						}
					} else
						canvansImageView.setImageBitmap(thePic);
				}

			} else if (requestCode == 4) {
				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				Log.v("1", matches.get(0));
				// System.out.println(matches.get(0));

				if (matches.get(0).equals("����") || matches.get(0).equals("���")) {
					takepicture();
				} else if (matches.get(0).equals("��ͼƬ")) {
					openpicture();
				} else if (matches.get(0).equals("����ͼƬ")) {
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy_MM_dd_kk_mm_ss");
					String date = formatter.format(new java.util.Date());
					String pathString = Environment
							.getExternalStorageDirectory()
							+ "/drawphoto/"
							+ date + ".png";
					// String
					// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
					canvansImageView.saveImage(pathString, SAVE_MODE);
				} else if (matches.get(0).equals("����ͼƬ")) {
					SimpleDateFormat formatter = new SimpleDateFormat(
							"yyyy_MM_dd_kk_mm_ss");
					String date = formatter.format(new java.util.Date());
					String pathString = Environment
							.getExternalStorageDirectory()
							+ "/drawphoto/"
							+ date + ".png";
					// String
					// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
					canvansImageView.saveImage(pathString, CUT_MODE);
					File file = new File(pathString);
					Uri uri = Uri.fromFile(file);
					saveType = CUT_MODE;
					startPhotoZoom(uri);
				} else {
					Toast toast = Toast.makeText(PaintMainActivity.this,
							"��������ȷ�����޶�Ӧ����������", Toast.LENGTH_LONG);
					toast.show();
				}

			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(PaintMainActivity.this)
					.setTitle("��ܰ��ʾ")
					.setMessage("�Ƿ񱣴�?")
					.setPositiveButton("��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									SimpleDateFormat formatter = new SimpleDateFormat(
											"yyyy_MM_dd_kk_mm_ss");
									String date = formatter
											.format(new java.util.Date());
									String pathString = Environment
											.getExternalStorageDirectory()
											+ "/drawphoto/" + date + ".png";
									// String
									// pathString=Environment.getExternalStorageDirectory()+"/"+date+".png";
									canvansImageView.saveImage(pathString,
											SAVE_MODE);
									dialog.dismiss();
									/*
									 * if(adThread!=null&&adThread.isAlive())
									 * adThread.destroy();
									 * msp.r2(MainActivity.this,false ,false ,0
									 * );
									 */
									finish();
									System.exit(0);
									// �˳�������ʾ
									// msp.exit(MainActivity.this);

								}
							})
					.setNegativeButton("��",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.dismiss();
									/*
									 * if(adThread!=null&&adThread.isAlive())
									 * adThread.destroy();
									 * msp.r2(MainActivity.this,false ,false ,0
									 * );
									 */
									finish();
									System.exit(0);
									// �˳�������ʾss
									// msp.exit(MainActivity.this);
									// System.exit(0);
								}
							}).setNeutralButton("ȡ��", null).show();

		}
		return super.onKeyDown(keyCode, event);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.undo:
			canvansImageView.undo();
			break;
		case R.id.reply:
			canvansImageView.redo();
			break;
		case R.id.share:
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			shareIntent.setType("image/*");
			try {
				
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy_MM_dd_kk_mm_ss");
				String date = formatter.format(new java.util.Date());
				String pathString = Environment.getExternalStorageDirectory()
						+ "/drawphoto/" + date + ".png";
				canvansImageView.saveImage(pathString, SHARE_MODE);
				Uri uri = Uri.fromFile(new File(pathString));
				shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
				PaintMainActivity.this.startActivity(Intent.createChooser(
						shareIntent, "��ѡ��"));
			} catch (Exception e) {
				
				Toast.makeText(PaintMainActivity.this, "��������", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.save:
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy_MM_dd_kk_mm_ss");
			String date = formatter.format(new java.util.Date());
			String pathString = Environment.getExternalStorageDirectory()
					+ "/drawphoto/" + date + ".png";
			canvansImageView.saveImage(pathString, SAVE_MODE);
			break;
		/*
		 * case R.id.about: Intent intent=new
		 * Intent(MainActivity.this,DisplayAbout.class);
		 * MainActivity.this.startActivity(intent); break;
		 */
		case R.id.zoom_canvans:
			zoomCanvas.setVisibility(View.VISIBLE);
			break;
		case R.id.move_canvans:
			if (DrawView.isMove) {
				DrawView.isMove = false;
				item.setTitle("�ƶ�����");
			} else {
				DrawView.isMove = true;
				item.setTitle("ֹͣ�ƶ�");
			}
			break;
		case R.id.pastephoto:
			// ��ʾ�Ի���
			final Dialog displayPPDialog = new Dialog(this);
			displayPPDialog.setTitle("ѡ����ͼ");
			//WindowManager.LayoutParams layoutParams=new WindowManager.LayoutParams((int)(0.8f*screenWidth), (int)(0.5f*screenHeight));
			//LayoutInflater layoutInflater=getLayoutInflater();
			//displayPPDialog.setContentView(layoutInflater.inflate(R.layout.pastephoto_layout,),layoutParams);
			displayPPDialog.setContentView(R.layout.pastephoto_layout);
			GridView photoGridView = (GridView) displayPPDialog.findViewById(R.id.paste_grid);
			//photoGridView.remove
			PastePhotoAdapter adapter = new PastePhotoAdapter(PaintMainActivity.this);
			photoGridView.setAdapter(adapter);
			photoGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(
								AdapterView<?> parent,View view, int position, long id) {
							// TODO Auto-generated method stub
							// canvansImageView.setImageResource((int)id);
							// ����copyģʽ
							canvansImageView.setPaintMode(DrawView.COPY_MODE);
							// ���ø���bitmap
							Bitmap copyBitmap = BitmapFactory.decodeResource(getResources(),(int) id);
							canvansImageView.setCopyBitmap(copyBitmap);
							// ����,��Ƥ��������Ϊ͸��
							main_penImageView.setBackgroundColor(Color.TRANSPARENT);
							main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
							displayPPDialog.dismiss();
						}

					});
			displayPPDialog.show();
			break;
		case R.id.closecolor:
			// ��ʾ��ɫ��ɫѡ��Ի���
			disClsColDialog();
			break;
		case R.id.areaselect:
			canvansImageView.setPaintMode(DrawView.CUT_MODE);
			main_penImageView.setBackgroundColor(Color.TRANSPARENT);
			main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
			break;
		case R.id.pastetext:
			final EditText textEditText=new EditText(PaintMainActivity.this);
			textEditText.setHint("�����Զ�������");
			new AlertDialog.Builder(PaintMainActivity.this)
			.setTitle("�Զ�������").setView(textEditText).
			setPositiveButton("ȷ��",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					paintText=textEditText.getText().toString();
					if(paintText.equals("")){
						Toast.makeText(PaintMainActivity.this,"�������ֲ���Ϊ��",Toast.LENGTH_SHORT).show();
						return;
					}
					main_penImageView.setBackgroundColor(Color.TRANSPARENT);
					canvansImageView.setPaintMode(DrawView.PASTETEXT_MODE);
					canvansImageView.setPaintText(paintText);
					dialog.dismiss();
				}
			}).setNegativeButton("ȡ��",null).show();
			
			break;
		case R.id.moreapp:
			OPUtils.startActivity(this, RecomActivity.class);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// ��ȡzoomCanvas����
	public ZoomControls getZoomCanvans() {
		return zoomCanvas;
	}

	// ��ȡ͸����
	public int getOpacity() {
		return opacity;
	}

	// �ü�ͼƬ
	public void startPhotoZoom(Uri uri) {
		// �ü�ͼƬ
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// �������crop=true�������ڿ�����Intent��������ʾ��VIEW�ɲü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 256);
		intent.putExtra("outputY", 256);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 3);
	}

	public void takepicture() {
		try {
			File file = new File(Environment.getExternalStorageDirectory(),
					"test.jpg");
			PaintMainActivity.photopath = file.getAbsolutePath();
			Uri outputFileUri = Uri.fromFile(file);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// ����intent�ķ������࣬����Ч�����ã�����ֱ�ӵ���Ӳ������ͷcamera
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			PaintMainActivity.MActivity.startActivityForResult(intent, 1);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openpicture() {
		Intent intent = new Intent();
		intent.setType("image/*");// ��ͼƬ��ʽ
		intent.setAction(Intent.ACTION_GET_CONTENT);

		startActivityForResult(intent, 2);
	}

	public void cutpicture() {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// ���ü��ü�����
			cropIntent.putExtra("crop", "true");
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// ���������
			cropIntent.putExtra("outputX", 256);
			cropIntent.putExtra("outputY", 256);
			// ���ؼ��õ�ͼƬ����
			cropIntent.putExtra("return-data", true);
			startActivityForResult(cropIntent, 3);
		} catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "Whoops - your device doesn't support the crop action!";
			Toast.makeText(PaintMainActivity.this, errorMessage, Toast.LENGTH_SHORT)
					.show();

		}
	}

	// �����ɫ�Ի���
	public void disClsColDialog() {
		final Dialog colorDialog = new Dialog(PaintMainActivity.this);
		colorDialog.setTitle("��ɫ��ɫ");
		colorDialog.setContentView(R.layout.dialog_for_selectcolor);
		final ColorPicker colorPicker = (ColorPicker) colorDialog
				.findViewById(R.id.picker);
		colorPicker.setColor(canvansImageView.getColor());
		final OpacityBar opacityBar = (OpacityBar) colorDialog
				.findViewById(R.id.opacitybar);
		colorPicker.addOpacityBar(opacityBar);
		opacityBar.setOpacity(opacity);
		Button colorOKButton = (Button) colorDialog.findViewById(R.id.colorok);
		Button colorCancelButton = (Button) colorDialog
				.findViewById(R.id.colorcancel);
		colorOKButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// colorPicker.getColor()
				// opacity=opacityBar.getOpacity();
				// canvansImageView.setColor(colorPicker.getColor());
				canvansImageView.setFillColor(colorPicker.getColor());
				canvansImageView.setPaintMode(DrawView.FILLCOLOR_MODE);
				main_penImageView.setBackgroundColor(Color.TRANSPARENT);
				main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
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
		// colorDialog.findViewById(R.id.sv)
		colorDialog.show();
	}

	@SuppressLint({ "ResourceAsColor", "InflateParams" })
	public void initMainImage() {
		
		// ��ȡ��Ļ�Ŀ��
		int measureScreenWidth = getWindowManager().getDefaultDisplay()
				.getWidth();
		// ��ȡÿ��ͼ��Ŀ�Ⱥ͸߶�
		int singleLength = measureScreenWidth / 8;
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				singleLength, singleLength);
		main_newcreateImageView = (ImageView) findViewById(R.id.main_newcreate);
		main_newcreateImageView.setBackgroundResource(R.drawable.img_state);
		
		main_drawtypeImageView = (ImageView) findViewById(R.id.main_drawfree);
		main_drawtypeImageView.setBackgroundResource(R.drawable.img_state);
		
		
		main_shapeImageView = (ImageView) findViewById(R.id.main_pensize);
		main_shapeImageView.setBackgroundResource(R.drawable.img_state);
		
		main_penImageView = (ImageView) findViewById(R.id.main_pen);
		main_penImageView.setBackgroundColor(R.color.selectColor);
		
		
		main_colorImageView = (ImageView) findViewById(R.id.main_pencolor);
		main_colorImageView.setBackgroundResource(R.drawable.img_state);
		
		main_backgroundImageView = (ImageView) findViewById(R.id.main_background);
		main_backgroundImageView.setBackgroundResource(R.drawable.img_state);
		
		
		main_eraserImageView = (ImageView) findViewById(R.id.main_eraser);
		
		main_clearImageView = (ImageView) findViewById(R.id.main_empty);
		main_clearImageView.setBackgroundResource(R.drawable.img_state);
		
		main_newcreateImageView.setLayoutParams(layoutParams);
		main_newcreateImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(PaintMainActivity.this)
						.setTitle("��ܰ��ʾ")
						.setMessage("�Ƿ񱣴浱ǰҳ��")
						.setPositiveButton("ȷ��",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										SimpleDateFormat formatter = new SimpleDateFormat(
												"yyyy_MM_dd_kk_mm_ss");
										String date = formatter
												.format(new java.util.Date());
										String pathString = Environment
												.getExternalStorageDirectory()
												+ "/drawphoto"
												+ "/"
												+ date
												+ ".png";
										canvansImageView.saveImage(pathString,
												SAVE_MODE);
										dialog.dismiss();
										final Dialog createDialog = new Dialog(
												PaintMainActivity.this,
												android.R.style.Theme_Holo_Light_Dialog);
										createDialog.setTitle("�½�����");
										createDialog
												.setContentView(R.layout.new_create_layout);
										createDialog.show();
										Button customCanvasButton = (Button) createDialog
												.getWindow().findViewById(
														R.id.canvas_ok);
										customCanvasButton
												.setOnClickListener(new View.OnClickListener() {

													@Override
													public void onClick(View v) {
														// TODO Auto-generated
														// method stub
														// dismissDialog(id);
														// ���ø߶�
														EditText widthEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_width);
														EditText heightEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_height);
														if (heightEditText
																.getText()
																.toString()
																.isEmpty())
															createHeight = mainView
																	.getHeight();
														else
															createHeight = Integer
																	.parseInt(heightEditText
																			.getText()
																			.toString());// ��ȡ����ĸ߶�

														if (widthEditText
																.getText()
																.toString()
																.isEmpty())
															createWidth = mainView
																	.getWidth();
														else
															createWidth = Integer
																	.parseInt(widthEditText
																			.getText()
																			.toString());// ��ȡ����Ŀ��

														createLayoutParams = new android.widget.LinearLayout.LayoutParams(
																createWidth,
																createHeight);
														canvansImageView
																.setLayoutParams(createLayoutParams);
														canvansImageView
																.setImageResource(R.drawable.whitebackground);
														isLoad = true;
														DrawView.isFirstDraw = true;
														createDialog.dismiss();
													}
												});

									}
								})
						.setNegativeButton("ȡ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
										final Dialog createDialog = new Dialog(
												PaintMainActivity.this,
												android.R.style.Theme_Holo_Light_Dialog);
										createDialog.setTitle("�½�����");
										createDialog
												.setContentView(R.layout.new_create_layout);
										createDialog.show();
										Button customCanvasButton = (Button) createDialog
												.getWindow().findViewById(
														R.id.canvas_ok);
										customCanvasButton
												.setOnClickListener(new View.OnClickListener() {

													@Override
													public void onClick(View v) {
														// TODO Auto-generated
														// method stub
														// dismissDialog(id);
														// ���ø߶�
														EditText widthEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_width);
														EditText heightEditText = (EditText) createDialog
																.getWindow()
																.findViewById(
																		R.id.canvas_height);
														if (heightEditText
																.getText()
																.toString()
																.isEmpty())
															createHeight = mainView
																	.getHeight();
														else
															createHeight = Integer
																	.parseInt(heightEditText
																			.getText()
																			.toString());// ��ȡ����ĸ߶�

														if (widthEditText
																.getText()
																.toString()
																.isEmpty())
															createWidth = mainView
																	.getWidth();
														else
															createWidth = Integer
																	.parseInt(widthEditText
																			.getText()
																			.toString());// ��ȡ����Ŀ��

														createLayoutParams = new android.widget.LinearLayout.LayoutParams(
																createWidth,
																createHeight);
														canvansImageView
																.setLayoutParams(createLayoutParams);
														canvansImageView
																.setImageResource(R.drawable.whitebackground);
														isLoad = true;
														DrawView.isFirstDraw = true;
														createDialog.dismiss();
													}
												});
									}
								}).show();

			}
		});
		main_drawtypeImageView.setLayoutParams(layoutParams);
		main_drawtypeImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(PaintMainActivity.this)
						.setTitle("ѡ���ͼ����")
						.setSingleChoiceItems(
								new String[] { "ֱ��", "����", "����",
										"������", "��Բ", "�����ֻ�" },
								canvansImageView.getCurrentShape(),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										switch (which) {

										case Shape_Type.STRAIGIT:
											canvansImageView
													.setShape(Shape_Type.STRAIGIT);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_line);
											break;
										case Shape_Type.BROKEN:
											canvansImageView
													.setShape(Shape_Type.BROKEN);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_broken);
											break;
										case Shape_Type.RECT:
											canvansImageView
													.setShape(Shape_Type.RECT);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_rect);
											break;
										case Shape_Type.MUTIL:
											canvansImageView
													.setShape(Shape_Type.MUTIL);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_mul);
											break;
										case Shape_Type.OVAL:
											canvansImageView
													.setShape(Shape_Type.OVAL);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_oval);
											break;
										case Shape_Type.FREE:
											canvansImageView
													.setShape(Shape_Type.FREE);
											main_drawtypeImageView
													.setImageResource(R.drawable.draw_free);
											break;
										default:
											break;
										}
										canvansImageView.setCurrentShape();
										main_penImageView
												.setBackgroundColor(R.color.selectColor);
										if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
											main_eraserImageView
													.setBackgroundColor(Color.TRANSPARENT);
											// fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
											// cutImageView.setBackgroundColor(Color.TRANSPARENT);
											canvansImageView
													.setPaintMode(DrawView.COMMON_MODE);
										}

										dialog.dismiss();
									}

								}).show();
			}
		});
		main_shapeImageView.setLayoutParams(layoutParams);
		main_shapeImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setsizeDialog = new Dialog(PaintMainActivity.this);
				setsizeDialog.setTitle("���ʴ�ϸ");
				setsizeDialog.setContentView(getLayoutInflater().inflate(
						R.layout.shape_paint, null));
				setsizeDialog
						.setOnShowListener(new DialogInterface.OnShowListener() {

							@Override
							public void onShow(DialogInterface dialog) {
								// TODO Auto-generated method stub
								processImageView.displayPenSize(penSize);
							}
						});
				penTextView = (TextView) setsizeDialog.getWindow()
						.findViewById(R.id.process_text);
				processImageView = (DisplayPenSizeView) setsizeDialog
						.getWindow().findViewById(R.id.pen_shape);
				// Toast.makeText(MainActivity.this,
				// ""+processImageView.getWidth(),Toast.LENGTH_SHORT).show();
				process_okButton = (Button) setsizeDialog.getWindow()
						.findViewById(R.id.pen_ok);
				process_cancelButton = (Button) setsizeDialog.getWindow()
						.findViewById(R.id.pen_cancel);
				process_okButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						penSize = (int) (0.3 * penProcess + 2);
						canvansImageView.setPenSize(penSize);
						setsizeDialog.dismiss();
					}
				});
				process_cancelButton
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								//penSize=orignPenSize;
								penProcess=orignPenProcess;
								setsizeDialog.dismiss();
							}
						});
				penSeekBar = (SeekBar) setsizeDialog.getWindow().findViewById(
						R.id.pen_seekbar);
				penSeekBar.setProgress(penProcess);
				penTextView.setText("" + penProcess);
				penSize = (int) (0.3 * penProcess + 2);
				penSeekBar
						.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								// processImageView.clear();
								penProcess = seekBar.getProgress();
								penSize = (int) (0.3 * penProcess + 2);
								// Toast.makeText(MainActivity.this,
								// ""+processImageView.getWidth(),
								// Toast.LENGTH_SHORT).show();
								processImageView.displayPenSize(penSize);
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								orignPenProcess=penProcess;
							}

							@Override
							public void onProgressChanged(SeekBar seekBar,
									int progress, boolean fromUser) {
								// TODO Auto-generated method stub
								// processImageView.draw(canvas);
								penTextView.setText("" + progress);
								// processImageView.set
								penSize = (int) (0.3 * progress + 2);
								processImageView.displayPenSize(penSize);

							}
						});
				setsizeDialog.show();
			}
		});
		main_penImageView.setLayoutParams(layoutParams);
		main_penImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				main_penImageView.setBackgroundColor(R.color.selectColor);
				if (canvansImageView.getPaintMode() != DrawView.COMMON_MODE) {
					// cutImageView.setBackgroundColor(Color.TRANSPARENT);
					main_eraserImageView.setBackgroundColor(Color.TRANSPARENT);
					// fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
					canvansImageView.setPaintMode(DrawView.COMMON_MODE);
				}
			}
		});
		main_colorImageView.setLayoutParams(layoutParams);
		main_colorImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				final Dialog colorDialog = new Dialog(PaintMainActivity.this);
				colorDialog.setTitle("��ɫѡ��");
				colorDialog.setContentView(R.layout.dialog_for_selectcolor);
				//colorDialog.getWindow().setLayout(200, 320);
				final ColorPicker colorPicker = (ColorPicker) colorDialog
						.findViewById(R.id.picker);
				//colorPicker.set
				colorPicker.setOldCenterColor(canvansImageView.getColor());
				colorPicker.setColor(canvansImageView.getColor());
				//colorPicker.setAlpha(alpha)
				final OpacityBar opacityBar = (OpacityBar) colorDialog
						.findViewById(R.id.opacitybar);
				final CheckBox blackCheckBox=(CheckBox)colorDialog.findViewById(R.id.black_checkbox);
				blackCheckBox.setChecked(isBlackColor);
				colorPicker.addOpacityBar(opacityBar);
				opacityBar.setOpacity(opacity);
				//colorPicker.set
				//colorPicker.setAlpha(penAlpha);
				Button colorOKButton = (Button) colorDialog
						.findViewById(R.id.colorok);
				Button colorCancelButton = (Button) colorDialog
						.findViewById(R.id.colorcancel);
				colorOKButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						// colorPicker.getColor()
						if(blackCheckBox.isChecked()){
							isBlackColor=true;
							canvansImageView.setColor(Color.BLACK);
						}else{
							isBlackColor=false;
							opacity = opacityBar.getOpacity();
							canvansImageView.setColor(colorPicker.getColor());
						}
						//penAlpha=colorPicker.getAlpha();
						colorDialog.dismiss();
					}
				});
				colorCancelButton
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {
								// TODO Auto-generated method stub
								colorDialog.dismiss();
							}
						});
				// colorDialog.findViewById(R.id.sv)
				colorDialog.show();
			}
		});
		main_backgroundImageView.setLayoutParams(layoutParams);
		main_backgroundImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Background backDialog = new Background(PaintMainActivity.this,
						R.style.CneterTitleHolo);
				backDialog.setTitle("��������ͼ");
				backDialog.show();
			}
		});
		main_eraserImageView.setLayoutParams(layoutParams);
		main_eraserImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (canvansImageView.getPaintMode() != DrawView.ERASER_MODE) {
					main_penImageView.setBackgroundColor(Color.TRANSPARENT);
					// cutImageView.setBackgroundColor(Color.TRANSPARENT);
					// fillcolorImageView.setBackgroundColor(Color.TRANSPARENT);
					canvansImageView.setPaintMode(DrawView.ERASER_MODE);
					main_eraserImageView
							.setBackgroundColor(R.color.selectColor);
				}
			}
		});
		main_clearImageView.setLayoutParams(layoutParams);
		main_clearImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				canvansImageView.clear();
			}
		});
	}
	
	public void initAD(){
		myPPCC=YManager.getInsertInstance(PaintMainActivity.this, "ed211db6f83545be84f251915bb124af","360-12");
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					if((Const.currentTime-Const.lastTime)>Const.dateNum*Const.ONE_DAY_MILL){
						Config.saveValue(Const.Key.DATE_NUM,String.valueOf(Const.dateNum+2));
						Config.saveValue(Const.Key.LAST_TIME, String.valueOf(Const.currentTime));
						Thread.sleep(30000);
						handler.sendEmptyMessage(1);
					}
				
					handler.sendEmptyMessage(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	
	
	/**
	 * GaoFei Note
	 * Save a newPhoto from originPhoto path
	 * @param rawPath      ԭ��ͼƬ��·��
	 * @param newPhoto     ��ͼƬ��·��
	 * @param quality      ѹ������
	 * @param targetWidth  ��ͼƬ�Ŀ��
	 * @param targetHeight ��ͼƬ�ĸ߶�
	 * @return
	 */
	
	public static boolean savePhotoFromRaw(String rawPath,String newPhotoPath,int quality,
			int targetWidth,int targetHeight){
		Log.i("rawPath",rawPath);
		
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(rawPath, options);
		/**ԭʼͼƬ�Ŀ�͸�*/
		int originWidth=options.outWidth;
		int originHeight=options.outHeight;
		/**
		 * ѹ������
		 */
		int scaleX=originWidth/targetWidth;
		int scaleY=originHeight/targetHeight;
		int scale=Math.min(scaleX, scaleY);
		scale*=2;
		options.inSampleSize=scale;
		options.inJustDecodeBounds=false;
		Bitmap originBitmap=BitmapFactory.decodeFile(rawPath,options);
		FileOutputStream newFileOutputStream;
		try {
			newFileOutputStream = new FileOutputStream(rawPath);
			originBitmap.compress(CompressFormat.JPEG, quality, newFileOutputStream);
			newFileOutputStream.flush();
			newFileOutputStream.close();
			OPUtils.showToast("�ɹ�ѹ��ͼƬ", Toast.LENGTH_SHORT);
		//	Toast.makeText(ISPApplication.getApplication().getApplicationContext(),"�ɹ�ѹ��ͼƬ", Toast.LENGTH_SHORT).show();
			return true;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OPUtils.showToast(e.getMessage(), Toast.LENGTH_SHORT);
		//	Toast.makeText(ISPApplication.getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			//Util.addToast(ISPApplication.getApplication().getApplicationContext(), e.getMessage());
			return false;
		}
		//return true;
	}
	
	/**
	 * GaoFei Note
	 * load bitmap from local path with special width and height
	 * @param photoPath     ���ش洢��ͼƬ·��
	 * @param targetWidth   �����ڴ�֮��Bitmap�Ŀ��
	 * @param targetHeight  �����ڴ�֮��Bitmap�ĸ߶�
	 * @return
	 */
	public static Bitmap loadBitmapFromPath(String photoPath,int targetWidth,int targetHeight){
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeFile(photoPath, options);
		/**ԭʼͼƬ�Ŀ�͸�*/
		int originWidth=options.outWidth;
		int originHeight=options.outHeight;
		/**
		 * ѹ������
		 */
		int scaleX=originWidth/targetWidth;
		int scaleY=originHeight/targetHeight;
		int scale=Math.min(scaleX, scaleY);
		scale*=2;
		options.inSampleSize=scale;
		options.inJustDecodeBounds=false;
		options.inPurgeable=true;
		Bitmap originBitmap=BitmapFactory.decodeFile(photoPath, options);
		return originBitmap;
	}
	
	
	/**
	 *������Ӧ��
	 */
	public void updateApp(){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();  
		            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); 
		            Document doc = dBuilder.parse("http://120.24.210.186:8080/PaintService/app_config.xml");
					if (doc != null) {
						
						NodeList codeList = doc.getElementsByTagName("version_code");
						if(codeList!=null&&codeList.getLength()>0){
							
							Element codeElement = (Element)codeList.item(0);
							String  codeContent = codeElement.getTextContent();
							int nViersionCode = Integer.parseInt(codeContent);
							int currVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
							NodeList updateNodeList = (NodeList)doc.getElementsByTagName("update_descrip");
							updateTip = updateNodeList.item(0).getTextContent();
							updateTip = Html.fromHtml(updateTip).toString();
							NodeList versionNameNodeList = (NodeList)doc.getElementsByTagName("version_name");
							updateVersion = versionNameNodeList.item(0).getTextContent();
							NodeList updateUrList = (NodeList)doc.getElementsByTagName("download_path");
							downloadPath = updateUrList.item(0).getTextContent();
							if(currVersionCode<nViersionCode){
								
								handler.sendEmptyMessage(3);
								
							}else{
								
								Log.i(TAG,"currversionCode:"+currVersionCode);
								Log.i(TAG,"nVersionCode:"+nViersionCode);
								Log.i(TAG,"updateTip:"+updateTip);
								Log.i(TAG,"updateVersion:"+updateVersion);
							}
						}
						
						
					}
					
				} catch (Exception e) {
				
					Log.i(TAG,""+e);
					
				}
				
			}
		}).start();
		
	   
	}
}
