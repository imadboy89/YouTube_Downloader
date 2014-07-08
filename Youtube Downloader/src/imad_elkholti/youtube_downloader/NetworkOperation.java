package imad_elkholti.youtube_downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

class NetworkOperation extends AsyncTask<Void, Void, String> {
	
	public String YT_url;
	public String output;
	private YT_Downloader YT;
	private String params;
	public int task = 1;
	public String un=null;
	public String ps=null;
	public String api=null;
	private String deviceInf = getDeviceInfos();
	
	private String   getDeviceInfos(){
		int flags = Base64.NO_WRAP | Base64.URL_SAFE;
		String devicename="";
		try {
			devicename = Base64.encodeToString(getDeviceName().getBytes("UTF-8"), flags);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String deviceInf = "&device="+devicename+"&os="+android.os.Build.VERSION.RELEASE;
		
		return deviceInf;
	}
	public void onPreExecute() {
		// do something
	}
	private void getCnt(){
		runRequest(YT_url);
	}
	public void setAct(YT_Downloader yt) {
		this.YT = yt;
		//deviceInf += "&imei="+getIMEI();
	}
	@Override
	protected String doInBackground(Void... params) {
		output = "";
		/*
		if(task==1){
			doSynch();
		}else if(task==2){
			doCheckVersion();
		}else if(task==3){
			doSignUp();
		}
*/    getCnt();
		return "";
	}
	private void runRequest(String strUrl){//mact.i.i("url",strUrl);
		try {
			URL url = new URL(strUrl);
			BufferedReader in = null;
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				output += inputLine;
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getOutPut() {
		return output;
	}
	
	public void onPostExecute(String msg) {
		JSONArray Object = null;
		try {
			Object = new JSONArray(output);
			JSONObject infos = Object.getJSONObject(0);
			//JSONObject det = Object.getJSONObject(1);
			String title = java.net.URLDecoder.decode(infos.getString("title"), "UTF-8") ;
			String img_url = java.net.URLDecoder.decode(infos.getString("iurlmq"), "UTF-8") ;
			YT.title = title;
			((TextView)YT.findViewById(R.id.title)).setText(title);
			/*
			GetImg gtimage = new GetImg();
			gtimage.url = img_url ;
			gtimage.execute();
			*/
			JSONArray links = Object.getJSONArray(1);
			for ( int i = 0; i < links.length() ; i++)
			{
				JSONObject object = links.getJSONObject(i);
				YT.i.i("links",object.getString("type"));
				String vid_type = object.getString("type") ;
				String vid_url = object.getString("url") ;
				//String vid_url = object.getString("url") ;
				YT.addType(vid_url, vid_type, i);
				
			}
			//YT.i.i("title",);
			
		} catch (JSONException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//YT.toast(output);
		// do something
		/*
		if(task==1){
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
				mact.synchItem.getActionView().clearAnimation();
				mact.synchItem.setActionView(null);
			}
			if(mact.nts.Synch(output)){
				mact.synchItem.setIcon(android.R.drawable.checkbox_on_background);
				mact.toast("Synchronazed successfuly :)");
			}else{
				//mact.toast("Didn't synchronazed properly :(");
			}
		}else if(task==2){
			getUpdate(output);
		}else if(task==3){
			getSignUp(output);
		}
		*/

	}
	long enqueue;
	DownloadManager dm;
	BroadcastReceiver receiver;
	private void getUpdate(String output){
		/*
		JSONObject errObject = null;
		try {
			errObject = new JSONObject(output);
			this.mact.toast(errObject.getString("error"));
		} catch (JSONException e) {
		
		try {
			
			String name = errObject.getString("url").split("/")[errObject.getString("url").split("/").length -1];
			askToDownload(errObject);
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		}
		*/
	}
	private void DoDownload(JSONObject errObject){
		String name;
		try {
			name = errObject.getString("url").split("/")[errObject.getString("url").split("/").length -1];
			deleteExisted(name);
			Uri uri=Uri.parse(errObject.getString("url"));
			DownloadManager.Request r = new DownloadManager.Request(uri);
			// This put the download in the same Download dir the browser uses
			r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
			r.setTitle(name);
			if (android.os.Build.VERSION.SDK_INT >= 11)
			    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			else
			    r.setShowRunningNotification(true);
		
			// Start download
			dm = (DownloadManager) YT.getSystemService(YT.DOWNLOAD_SERVICE);
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
								openFile(uriString);
								YT.i.i("downloadfile", "downloaded file " + uriString);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			                                    
			            } else {
			            	getReason(c,c.getInt(columnIndex));
			                YT.i.i("downloadfile", "download failed " +c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
			                //mact.toast("Downloading the updates failed !"+DownloadManager.STATUS_SUCCESSFUL );
			            }
			        }
			    }
			};
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		YT.getApplicationContext().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	private void askToDownload(final JSONObject errObject) {
		new AlertDialog.Builder(this.YT)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Download Updates.")
				.setMessage("There is Update for MaNotes.\nStart downloading it ?")
				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,int which) {
						DoDownload(errObject);
						// finish();
					}

				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}

				}).show();
	}
	protected void openFile(String fileName) {
	    Intent install = new Intent(Intent.ACTION_VIEW);
	    install.setDataAndType(Uri.parse(fileName),"application/vnd.android.package-archive");
	    install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    YT.startActivity(install);
	}
	private void getSignUp(String output){
		JSONObject errObject;
		try {
			errObject = new JSONObject(output);
			//this.YT.toast(errObject.getString("error"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		String toReturn = null;
		if (model.startsWith(manufacturer)) {
			toReturn = capitalize(model);
		} else {
			toReturn = capitalize(manufacturer) + " " + model;
		}
		try {
			return URLEncoder.encode(toReturn, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
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
		  YT.toast(failedReason);
		return failedReason;
	}
	private void deleteExisted(String filename){
	    File folder1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/"+ filename);
	    if( folder1.exists())
	    	folder1.delete();
	}
	public String getIMEI(){
	    TelephonyManager mngr = (TelephonyManager) YT.getSystemService(YT.TELEPHONY_SERVICE); 
	    String imei = mngr.getDeviceId();
	    return imei;
	}

}

class GetImg extends AsyncTask<String, Void, String> {
	
	private YT_Downloader yt;
	public String url;
	public void setAct(YT_Downloader yt){
		this.yt =yt;
	}
    @Override
    protected String doInBackground(String... params) {
		Bitmap temp=null;
		try {
			temp = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
    	ImageView txt = (ImageView) yt.findViewById(R.id.yt_image);
    	//((ImageView )yt.findViewById(R.id.yt_image)).setImageBitmap(temp);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

}