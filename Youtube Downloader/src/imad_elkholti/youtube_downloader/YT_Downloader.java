package imad_elkholti.youtube_downloader;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class YT_Downloader extends Activity {
	private String api_url = "http://yt.coding-labs.com/ytAPI.php?isAPI=1&url=";
	private String url_base = "https://www.youtube.com/get_video_info?video_id=";
	Log i;
	private Toast toastMesage;
	Hashtable<Integer,String> btns_urls=new Hashtable<Integer,String>();
	public String title ="";
	long enqueue;
	DownloadManager dm;
	BroadcastReceiver receiver;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yt_downloader);
		
		
		
		Intent intent = getIntent();
		if(!intent.getAction().equals(Intent.ACTION_MAIN)){
			Bundle extras = this.getIntent().getExtras();
			//String[] recipients = (String[]) extras.get(intent.EXTRA_SUBJECT);
			ShareCrtl sc = new ShareCrtl(intent,this);
			start(sc);
		}
	}
	
	private int start(ShareCrtl sc) {
		//showinValideUrlMsg(url)
		String title = sc.title;
		String url = sc.url;
		url = url.split(" ")[url.split(" ").length-1];
		//getJson(url);
		if(!Patterns.WEB_URL.matcher(url).matches())
			return showinValideUrlMsg(url);
		
		getJson(url);
		return 0;
	}
	private String getJson(String url){
		NetworkOperation ntw = new NetworkOperation();
		ntw.setAct(this);
		ntw.task =3 ;
		//String[] result = s.split("v=");
		String vid = getVID(url) ;
		if(vid=="") return "";
		ntw.YT_url=url_base+vid;
		ntw.execute();
		return ntw.output;
	}
	private String getVID(String url){
		try {
			Pattern p  = Pattern.compile("watch\\?v=([^\\s&]+)");
			Pattern p2 = Pattern.compile("youtube\\.[^\\/]+\\/([^&\\/\\s^?]+)\\/?");
			Matcher m = p.matcher(url);
			Matcher m2 = p2.matcher(url);
			String VID = ""; 
			if(m.find())
				VID = m.group(1) ;
			else if(m2.find())
				VID = m2.group(1) ;
			else VID = "";
			return VID;
		} catch (Exception e) {
			return "" ;
		}

	}
	public void setImg(Bitmap img,String title) {
		//BitmapDrawable ob = new BitmapDrawable(bitmap);
//		((ImageView )findViewById(R.id.yt_image)).setImageBitmap(img);
		
	}
	
	public void addType(String url,String name,int id){
		
		String ext = name.split(";")[0].split("/")[1];
		 
		LinearLayout links = (LinearLayout) findViewById(R.id.links);
		Button Download_btn = new Button(this);
		Download_btn.setText("download "+ext);
		Download_btn.setId(id);
		links.addView(Download_btn);
		btns_urls.put(id,url+"_||_"+ext);
		
		Download_btn.setOnClickListener(dowloadBtnHandler);
	}
	View.OnClickListener dowloadBtnHandler = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			String[] url_ext = btns_urls.get(id).split("_\\|\\|_");
			String url = url_ext[0];
			String ext = url_ext[1];
			//i.i("url","tst:"+url);
			DoDownload(url, title+"."+ext);
		}
		
	};
	
	private void DoDownload(String url,String name){
		deleteExisted(name);
		Uri uri=Uri.parse(url);
		
		DownloadManager.Request r = new DownloadManager.Request(uri);
		// This put the download in the same Download dir the browser uses
		String dest = createDirIfNotExists(Environment.DIRECTORY_DOWNLOADS+"_YT/");
		r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
		r.setTitle(name);
		if (android.os.Build.VERSION.SDK_INT >= 11)
		    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		else
		    r.setShowRunningNotification(true);

		// Start download
		dm = (DownloadManager) getSystemService(this.DOWNLOAD_SERVICE);
		enqueue =dm.enqueue(r);

		receiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (!DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
		            return;
		        }
		        try {
		        	context.getApplicationContext().unregisterReceiver(receiver);
				} catch (Exception e2) {
					// TODO: handle exception
				}
		        
		        Query query = new Query();
		        query.setFilterById(enqueue);
		        Cursor c = dm.query(query);
		        if (c.moveToFirst()) {
		            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
		            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

		                String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
		                try {
							dm.openDownloadedFile(enqueue);
							toast("Video downloaded succesfaly !!");
							//openFile(uriString);
							i.i("downloadfile", "downloaded file " + uriString);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		                                    
		            } else {
		            	getReason(c,c.getInt(columnIndex));
		                i.i("downloadfile", "download failed " +c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
		                //mact.toast("Downloading the updates failed !"+DownloadManager.STATUS_SUCCESSFUL );
		            }
		        }
		    }
		};
		getApplicationContext().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}
	private void deleteExisted(String filename){
	    File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/"+ filename);
	    if( folder1.exists())
	    	folder1.delete();
	}
	private String getReason(Cursor c,int status){
		int columnReason = c.getColumnIndex(DownloadManager.COLUMN_REASON);  
		int reason = c.getInt(columnReason);
		String failedReason ="";

		   switch(reason){
		   case DownloadManager.ERROR_CANNOT_RESUME:
		    failedReason = "ERROR_CANNOT_RESUME";
		    break;
		   case DownloadManager.ERROR_DEVICE_NOT_FOUND:
		    failedReason = "ERROR_DEVICE_NOT_FOUND";
		    break;
		   case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
		    failedReason = "ERROR_FILE_ALREADY_EXISTS";
		    break;
		   case DownloadManager.ERROR_FILE_ERROR:
		    failedReason = "ERROR_FILE_ERROR";
		    break;
		   case DownloadManager.ERROR_HTTP_DATA_ERROR:
		    failedReason = "ERROR_HTTP_DATA_ERROR";
		    break;
		   case DownloadManager.ERROR_INSUFFICIENT_SPACE:
		    failedReason = "ERROR_INSUFFICIENT_SPACE";
		    break;
		   case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
		    failedReason = "ERROR_TOO_MANY_REDIRECTS";
		    break;
		   case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
		    failedReason = "ERROR_UNHANDLED_HTTP_CODE";
		    break;
		   case DownloadManager.ERROR_UNKNOWN:
		    failedReason = "ERROR_UNKNOWN";
		    break;
		   }
		  toast(failedReason);
		return failedReason;
	}
	public void toast(String msg) {

		if (toastMesage != null) {
			toastMesage.setText(msg);
			toastMesage.show();
		} else {
			toastMesage = Toast.makeText(this, msg, Toast.LENGTH_LONG);
			toastMesage.show();
		}
	}
	
	
	public int showinValideUrlMsg(String url){
		AlertDialog alertDialog = new AlertDialog.Builder(
                this).create();
			
			// Setting Dialog Title
			alertDialog.setTitle("Alert Dialog");
			
			// Setting Dialog Message
			alertDialog.setMessage("Invalide url :\n "+url);
			
			// Setting Icon to Dialog
			alertDialog.setIcon(android.R.drawable.alert_dark_frame);
			
			// Setting OK Button
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			        // Write your code here to execute after dialog closed
			        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
			        }
			});
			
			// Showing Alert Message
			alertDialog.show();
		return 0;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.yt__downloader, menu);
		return true;
	}
	public static String createDirIfNotExists(String path) {

	    File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	            Log.e("TravellerLog :: ", "Problem creating Image folder");
	            return "0";
	        }
	    }
	    return path;
	}

}
