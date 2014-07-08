package imad_elkholti.youtube_downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.webkit.MimeTypeMap;

public class ShareCrtl {
	String title = "title";
	String url = "url";
	public ShareCrtl(Intent intent,Context context) {
		// Get intent, action and MIME type
		// Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();

		if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				if(!handleSendText(intent)) // Handle text being sent
					handleSendText_file(intent);
				
			}
		}
		
	}
	private boolean handleSendText(Intent intent) {
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		String sharedsubjectt = intent.getStringExtra(Intent.EXTRA_SUBJECT);
		if (sharedText != null) {
			url = sharedText;
			title = (sharedsubjectt!=null) ? sharedsubjectt : "";
		}else{
			return false;
		}
		return true;
	}

	private void handleSendText_file(Intent intent) {
		String filePath = intent.getParcelableExtra(Intent.EXTRA_STREAM).toString();
		if (!getMimeType(filePath).equals("text/plain"))
			return;
		if (filePath != null) {
			getContentFromFile(filePath);
		}
	}

	private void getContentFromFile(String filePath) {
		FileInputStream fis;
		filePath = filePath.replace("file://","");
		title = filePath.split("/")[filePath.split("/").length-1];
		try {
        	File file = new File(filePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			url = sb.toString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	public String read_file(Context context, String filename) {
        try {
            //FileInputStream fis = context.openFileInput(filename);
            //InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        	File file = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
            return "";
        } catch (IOException e) {
        	e.printStackTrace();
            return "";
        }
    }
*/
	private static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}
}
